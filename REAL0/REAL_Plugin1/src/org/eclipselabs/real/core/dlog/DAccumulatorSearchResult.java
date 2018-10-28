package org.eclipselabs.real.core.dlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipselabs.real.core.distrib.GenericError;
import org.eclipselabs.real.core.distrib.IDistribAccumulator;
import org.eclipselabs.real.core.distrib.IDistribTaskResultWrapper;
import org.eclipselabs.real.core.searchresult.ISearchResult;

/**
 * This class accumulates the results from the distribution system that is searching in the log files.
 *
 * Note:
 *
 * This class uses not thread-safe collections but all methods are synchronized. This means only one
 * thread may add a result or an error at the same time. It could make more sense to synchronize on the collections
 * but the methods are small practically one add/retrieve no need to complicate the code.
 *
 * The methods that return results return unmodifiable views of the underlying collections
 * This allows some consumer to poll the accumulator for results and for example update the GUI.
 * @author Vadim Korkin
 *
 */
public class DAccumulatorSearchResult<R extends ISearchResult<?>> implements IDistribAccumulator<R, List<IDistribTaskResultWrapper<R>>, GenericError> {

    private List<IDistribTaskResultWrapper<R>> accumResults = new ArrayList<>();

    private List<GenericError> accumErrors = new ArrayList<>();

    public DAccumulatorSearchResult() {
        // empty constructor
    }

    /**
     * Adds a new result from a task to the accumulator
     * @param newResult the result to add to the accumulator
     */
    @Override
    public synchronized void addResult(IDistribTaskResultWrapper<R> newResult) {
        accumResults.add(newResult);
    }

    /**
     * This method returns all results in the initial form with possible NVPs added during execution.
     * Remember this method returns an unmodifiable collection
     */
    @Override
    public synchronized List<IDistribTaskResultWrapper<R>> getAllResults() {
        return Collections.unmodifiableList(accumResults);
    }

    /**
     * This method returns the accumulated result.
     * For this accumulator type the returned type is an unmodifiable collection search task results
     */
    @Override
    public synchronized List<IDistribTaskResultWrapper<R>> getResult() {
        return getAllResults();
    }

    /**
     * Adds a new error to this accumulator from a task
     * @param newError the new error to add to this accumulator
     */
    @Override
    public synchronized void addError(GenericError newError) {
        accumErrors.add(newError);
    }

    /**
     * This method returns a list of errors for this accumulator
     * Remember this method returns an unmodifiable collection
     */
    @Override
    public synchronized List<GenericError> getErrors() {
        return Collections.unmodifiableList(accumErrors);
    }

}
