package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.core.searchobject.ISOSearchScript;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISRSearchScript;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class SubmitSearchStageSS extends ConveyorStageBase {

    public SubmitSearchStageSS() {
        super(StageID.SUBMIT_SEARCH);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        final ISOSearchScript currSO = (ISOSearchScript) req.getDso().getSearchObject();
        ISRSearchScript searchScrResult = currSO.performSearch(params.getSearchRequest());
        if (searchScrResult != null) {
            Map<String,IKeyedComplexSearchResult<? extends IComplexSearchResultObject<
                        ? extends ISearchResult<? extends ISearchResultObject>,? extends ISearchResultObject,String>,
                    ? extends ISearchResult<? extends ISearchResultObject>,
                    ? extends ISearchResultObject, String>> tmpMap = new HashMap<>();
            tmpMap.put("SearchScript", searchScrResult);
            params.setResult(tmpMap);
        }

        params.getSearchRequest().getProgressMonitor().setComplete(true);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing the search is performed in one thread
    }

}
