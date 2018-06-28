package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class SubmitSearchCurrentStage extends ConveyorStageBase {

    private static final Logger log = LogManager.getLogger(SubmitSearchCurrentStage.class);
    private volatile ConvProductContext context;

    public SubmitSearchCurrentStage() {
        super(StageID.SUBMIT_SEARCH_CURRENT);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        context = params;
        return runWithParameters(req, params);
    }

    private <R extends IKeyedComplexSearchResult<O, W, X, String>,
                O extends IComplexSearchResultObject<W, X, String>,
                V extends ISearchObject<W, X>, W extends ISearchResult<X>,
                X extends ISearchResultObject>
            CompletableFuture<Void> runWithParameters(ConvSearchRequest req, ConvProductContext params) {
        final IKeyedComplexSearchObject<R,O,V,W,X,String> currSO = (IKeyedComplexSearchObject<R,O,V,W,X,String>)req.getDso().getSearchObject();
        try {
            R res = currSO.performSearch(params.getSearchRequest());
            params.getSearchRequest().getProgressMonitor().incrementCompletedSOFiles();
            params.getSearchRequest().getProgressMonitor().incrementCompletedWork();
            if (res != null) {
                Map<String, R> tmpMap = new HashMap<>();
                tmpMap.put("SearchInCurrent", res);
                params.setResult((Map<String, IKeyedComplexSearchResult<? extends
                        IComplexSearchResultObject<? extends ISearchResult<? extends ISearchResultObject>,
                                ? extends ISearchResultObject, String>,
                        ? extends ISearchResult<? extends ISearchResultObject>,
                        ? extends ISearchResultObject, String>>) tmpMap);
            }
            params.getSearchInfo().setFoundObjects(params.getSearchRequest().getProgressMonitor().getObjectsFound());
            params.getSearchInfo().setCustomProgressKeys(params.getSearchRequest().getProgressMonitor().getAllCustomNVPs());

            params.getSearchRequest().getProgressMonitor().setComplete(true);
        } catch (IncorrectPatternException e) {
            log.error("Exception in submitSearch stage ", e);
            params.setProceed(false);
            params.setAbortMessage(e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        if (context != null) {
            context.getSearchRequest().getProgressMonitor().setCancelled(true);
        }
    }

}
