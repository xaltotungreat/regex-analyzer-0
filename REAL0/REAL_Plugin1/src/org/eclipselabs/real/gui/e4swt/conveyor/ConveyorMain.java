package org.eclipselabs.real.gui.e4swt.conveyor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.build.ComplexRegexStageBuilder;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.build.IStageTreeBuilder;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.build.SearchScriptStageBuilder;

/**
 * The main entry point for the Conveyor system. Conveyor is responsible for the following:
 * 1. Submitting requests and executing the created products
 * 2. Keeping the list of stage builders sorted according to priorities
 * These priorities are important to select the correct builder
 *
 * @author Vadim Korkin
 *
 */
public enum ConveyorMain {

    INSTANCE;
    private static final Logger log = LogManager.getLogger(ConveyorMain.class);

    // performance property keys
    private static final String PERF_CONST_SIMULTANEOUS_OPERATIONS = "org.eclipselabs.real.gui.e4swt.conveyor.SimultaneousConveyorOperationsNumber";
    // the cache of past search operations
    private OperationsCache opCache = new OperationsCache();
    // the executor for search operations
    private ExecutorService convExecutor;
    // this semaphore limits the number of concurrently running requests
    private Semaphore convSemaphore;
    // builder list
    private List<StageBuilderSelection> orderedBuilderList = new ArrayList<StageBuilderSelection>();

    public static class StageBuilderSelection {

        private IStageTreeBuilder builder;
        private Predicate<ConvSearchRequest> dsoPredicate;
        public StageBuilderSelection() {}

        public StageBuilderSelection(IStageTreeBuilder bld, Predicate<ConvSearchRequest> pred) {
            builder = bld;
            dsoPredicate = pred;
        }

        public IStageTreeBuilder getBuilder() {
            return builder;
        }

        public void setBuilder(IStageTreeBuilder builder) {
            this.builder = builder;
        }

        public Predicate<ConvSearchRequest> getDsoPredicate() {
            return dsoPredicate;
        }

        public void setDsoPredicate(Predicate<ConvSearchRequest> dsoPredicate) {
            this.dsoPredicate = dsoPredicate;
        }
    }

    private ConveyorMain() {
        int threadsNum = PerformanceUtils.getIntProperty(PERF_CONST_SIMULTANEOUS_OPERATIONS, 3);
        convExecutor = Executors.newFixedThreadPool(threadsNum, new NamedThreadFactory("Conveyor"));
        convSemaphore = new Semaphore(threadsNum, true);

        // default stage builders
        orderedBuilderList.add(new StageBuilderSelection(new ComplexRegexStageBuilder(),
                (req) -> SearchObjectType.COMPLEX_REGEX.equals(req.getDso().getSearchObject().getType())));
        orderedBuilderList.add(new StageBuilderSelection(new SearchScriptStageBuilder(),
                (req) -> SearchObjectType.SEARCH_SCRIPT.equals(req.getDso().getSearchObject().getType())));

        orderedBuilderList.sort((a, b) -> -a.getBuilder().getDetailLevel().compareTo(b.getBuilder().getDetailLevel()));
    }

    /**
     * Returns the cache of executed products parameters. Actually the cache contains
     * a set of previously used parameters for every search object. The number of
     * parameters for a search object is limited.
     * @return the cache of executed products
     */
    public OperationsCache getOperationsCache() {
        return opCache;
    }

    /**
     * Returns the semaphore that limits the number of concurrently processed products.
     * All products must use this semaphore
     * @return the semaphore that limits the number of concurrently processed products
     */
    public Semaphore getConvSemaphore() {
        return convSemaphore;
    }

    /**
     * The main entry point for all search requests. The processing pipeline in this case the Conveyor
     * will take care of showing the parameters dialog, opening a new window etc.
     * This method is not blocking. This method doesn't use the semaphore to avoid
     * blocking the submitting thread. The semaphore is used in the product.
     * This method only creates a product and begins its execution in another thread.
     * @param req the search request that is submitted to the conveyor
     * @return The future that will contain the context (all the values filled in) once this operation is complete
     */
    public CompletableFuture<ConvSearchResult> submitRequest(ConvSearchRequest req) {
        CompletableFuture<ConvSearchResult> endResult = null;
        Optional<IStageTreeBuilder> optBld = getFirstMatchingBuilder(req);
        if (optBld.isPresent()) {
            IConveyorProduct product = new ConveyorProductImpl(optBld.get());
            CompletableFuture<ConvProductContext> productExec = CompletableFuture.supplyAsync(() -> product.executeRequest(req), convExecutor);
            endResult = productExec.thenApply(ConvSearchResult::getFromContext);
        } else {
            log.error("submitRequest stage tree builder not found for request - return null");
        }
        return endResult;
    }

    /**
     * This method allows to register a builder to construct a special stage tree for some requests
     * The predicate accepts the search request and must return true if this builder can be used for this request
     * The detail level of the builder defines its position in the builders list.
     * The higher the number the closer to the beginning of the list. Default builders have 0 detail level
     * The builders closer to the beginning of the list are checked for a match before the ones further down the list
     * For example if 3 builders are registered with detail levels 3, 2, 1 and both 3 and 1 match
     * the first matching builder is 3. The method that returns the list will return both in the order [3, 1]
     * The detail level is used instead of priority because the builders with more detailed predicates
     * (more conditions, more rare conditions) must be placed closed to the beginning of the list
     * this ensures they are not "overshadowed" by more general builders that match many requests
     * @param bld the stage tree builder to match
     * @param pred the predicate to check the search request for matching
     */
    public void addBuilder(IStageTreeBuilder bld, Predicate<ConvSearchRequest> pred) {
        if ((bld != null) && (pred != null)) {
            orderedBuilderList.add(new StageBuilderSelection(bld, pred));
            orderedBuilderList.sort((a, b) -> -a.getBuilder().getDetailLevel().compareTo(b.getBuilder().getDetailLevel()));
        }
    }

    /**
     * Returns the first matching builder for this request. The matching builder with the highest
     * detail level is returned if there are more than one matching.
     * @param req the search request for this operation
     * @return the optional containing the matching builder with the highest detail level or
     * an empty Optional if none have been found
     */
    public Optional<IStageTreeBuilder> getFirstMatchingBuilder(ConvSearchRequest req) {
        Optional<IStageTreeBuilder> result = Optional.empty();
        if (req != null) {
            result = orderedBuilderList.stream().
                    filter((bldSel) -> bldSel.getDsoPredicate().test(req)).
                    map((builderSel1) -> builderSel1.getBuilder()).
                    findFirst();
        }
        return result;
    }

    /**
     * Returns all matching builders for this search request
     * @param req the request to for this operation
     * @return the list of all matching builders
     */
    public List<IStageTreeBuilder> getMatchingBuilders(ConvSearchRequest req) {
        List<IStageTreeBuilder> builders = orderedBuilderList.stream().
                filter((bldSel) -> bldSel.getDsoPredicate().test(req)).
                map((builderSel) -> builderSel.getBuilder()).
                collect(Collectors.toList());
        return builders;
    }


}
