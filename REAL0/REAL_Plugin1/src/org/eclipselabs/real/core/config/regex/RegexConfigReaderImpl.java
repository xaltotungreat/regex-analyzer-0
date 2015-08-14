package org.eclipselabs.real.core.config.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.eclipselabs.real.core.config.ConstructionTask;
import org.eclipselabs.real.core.config.IConfigObjectConstructor;
import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.ref.IRefKeyedSOContainer;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.NamedLock;

/**
 * This abstract implementation handles the writing part of the configuration.
 * It means that this reader cannot read information - the read method is not implemented.
 * But with the methods in this class any descendant class can write the information to
 * the appropriate repositories.
 * All reading is delegated to the descendant classes.
 *
 * @author Vadim Korkin
 *
 * @param <U> the type of the source (InputStream for a file, JDBC conn for a DB etc.)
 */
public abstract class RegexConfigReaderImpl<U> implements IConfigReader<U> {

    protected List<NamedLock> modificationLocks = new ArrayList<NamedLock>();
    protected ExecutorService configReaderExecutor;

    public RegexConfigReaderImpl(ExecutorService executor) {
        configReaderExecutor = executor;
        modificationLocks.add(
                new NamedLock(SearchObjectController.INSTANCE.getSearchObjectRepository().getWriteLock(), "SearchObj repo write lock"));
        modificationLocks.add(
                new NamedLock(SearchObjectController.INSTANCE.getReplaceParamRepository().getWriteLock(), "ReplaceParam repo write lock"));
    }
    public class AddSOCallback implements IRegexConfigCompletionCallback {

        @Override
        public void addSearchObject(SearchObjectKey soKey, IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> param) {
            SearchObjectController.INSTANCE.getSearchObjectRepository().add(soKey, param);
        }

        @Override
        public void addReplaceParam(ReplaceableParamKey rpKey, IReplaceableParam<?> param) {
            SearchObjectController.INSTANCE.getReplaceParamRepository().add(rpKey, param);
        }

        @Override
        public void addAllSearchObject(Map<SearchObjectKey, IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> searchObjMap) {
            SearchObjectController.INSTANCE.getSearchObjectRepository().addAll(searchObjMap);
        }

        @Override
        public void addAllReplaceParam(Map<ReplaceableParamKey, IReplaceableParam<?>> replaceParamMap) {
            SearchObjectController.INSTANCE.getReplaceParamRepository().addAll(replaceParamMap);
        }

        @Override
        public void resolveAllRefs(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> refsList,
                List<IRefKeyedSOContainer> refContainersList) {
            // in case the one or both of the lists are unmodifiable
            // put them in new modifiable lists
            List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> copyRefsList = null;
            if (refsList != null) {
                copyRefsList = new ArrayList<>(refsList);
            }
            List<IRefKeyedSOContainer> copyContainersList = null;
            if (refContainersList != null) {
                copyContainersList = new ArrayList<>(refContainersList);
            }
            internalResolveAllRefs(copyRefsList, copyContainersList);
        }

        protected void internalResolveAllRefs(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> refsList,
                List<IRefKeyedSOContainer> refContainersList) {
            int resolveCount = 0;
            if (refsList != null) {
                // tmp list to store fully resolved containers
                // cannot remove inside the cycle because it leads to a ConcurrentModificationException
                List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> toRemove = new ArrayList<>();
                for (RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> currRef : refsList) {
                    IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> resolvedSO
                        = currRef.resolve(SearchObjectController.INSTANCE.getSearchObjectRepository());
                    if (resolvedSO != null) {
                        SearchObjectController.INSTANCE.getSearchObjectRepository().add(
                                new SearchObjectKey(resolvedSO.getSearchObjectName(), resolvedSO.getSearchObjectGroup(), resolvedSO.getSearchObjectTags()), resolvedSO);
                        toRemove.add(currRef);
                        resolveCount++;
                    }
                }
                for (RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> refKeyedSORemove : toRemove) {
                    refsList.remove(refKeyedSORemove);
                }
            }
            if (refContainersList != null) {
                // tmp list to store fully resolved containers
                // cannot remove inside the cycle because it leads to a ConcurrentModificationException
                List<IRefKeyedSOContainer> resolvedConts = new ArrayList<>();
                for (IRefKeyedSOContainer currCont : refContainersList) {
                    //boolean beforeContResolved = currCont.isAllRefResolved();
                    currCont.resolveRefs(SearchObjectController.INSTANCE.getSearchObjectRepository());
                    boolean afterContResolved = currCont.isAllRefResolved();
                    if (afterContResolved) {
                        resolvedConts.add(currCont);
                        //refContainersList.remove(currCont);
                        resolveCount++;
                    }
                }
                refContainersList.removeAll(resolvedConts);
            }
            if (resolveCount > 0) {
                internalResolveAllRefs(refsList, refContainersList);
            }
        }
    }

    protected <K, V> CompletableFuture<V> submitConstructionTask(
            IConfigObjectConstructor<K, V> coConstructor,
            IConstructionSource<K> aSource) {
        ConstructionTask<K, V> newTask = new ConstructionTask<K, V>(
                coConstructor, aSource);
        return CompletableFuture.supplyAsync(newTask, configReaderExecutor);//configReaderExecutor.submit(newTask);
    }

}
