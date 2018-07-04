package org.eclipselabs.real.core.dlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipselabs.real.core.distrib.GenericError;
import org.eclipselabs.real.core.distrib.IDistribAccumulator;
import org.eclipselabs.real.core.distrib.IDistribTaskResultWrapper;
import org.eclipselabs.real.core.searchresult.ISearchResult;

public class DAccumulatorSearchResult<R extends ISearchResult<?>> implements IDistribAccumulator<R, List<IDistribTaskResultWrapper<R>>, GenericError> {

    private List<IDistribTaskResultWrapper<R>> accumResults = Collections.synchronizedList(new ArrayList<>());

    private List<GenericError> accumErrors = Collections.synchronizedList(new ArrayList<>());

    public DAccumulatorSearchResult() {
        // empty constructor
    }

    @Override
    public void addResult(IDistribTaskResultWrapper<R> newResult) {
        accumResults.add(newResult);
    }

    @Override
    public List<IDistribTaskResultWrapper<R>> getAllResults() {
        return accumResults;
    }

    @Override
    public List<IDistribTaskResultWrapper<R>> getResult() {
        return accumResults;
    }

    @Override
    public void addError(GenericError newError) {
        accumErrors.add(newError);

    }

    @Override
    public List<GenericError> getErrors() {
        return accumErrors;
    }

}
