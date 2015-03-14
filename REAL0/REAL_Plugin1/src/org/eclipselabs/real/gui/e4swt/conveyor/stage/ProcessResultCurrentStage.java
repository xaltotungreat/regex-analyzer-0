package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.gui.core.result.ComplexDisplayResultImpl;
import org.eclipselabs.real.gui.core.result.IComplexDisplayResult;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class ProcessResultCurrentStage extends ConveyorStageBase {
    private static final Logger log = LogManager.getLogger(ProcessResultCurrentStage.class);
    public ProcessResultCurrentStage(Predicate<IConveyorStage> complPred) {
        super(StageID.PROCESS_RESULT, complPred);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        return runWithTypeParams(req, params);
    }

    private <R extends IKeyedComplexSearchResult<O,W,X,String>,
                O extends IComplexSearchResultObject<W,X,String>,
                W extends ISearchResult<X>, X extends ISearchResultObject>
            CompletableFuture<Void> runWithTypeParams(ConvSearchRequest req, final ConvProductContext params) {
        if (params.getResult() == null) {
            log.warn("runWithTypeParams The result is null " + req.getDso().getDisplayName());
        } else {
            final GUISearchResult partSRObj = (GUISearchResult) params.getSearchPart().getObject();
            // for scripts views may be added during the search
            R firstRes = (R)params.getResult().values().iterator().next();
            if (firstRes.viewsAvailable()) {
                // for a search in current there is only one result in the map
                // the map is actually a fake map
                params.setGuiObjectRef(partSRObj);
                Map<String, R> resFakeMap = (Map<String, R>) params.getResult();
                final IComplexDisplayResult complRegResult = new ComplexDisplayResultImpl(resFakeMap.values().iterator().next(), req.getDso());
                req.getUiSynch().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        partSRObj.addViewResult(complRegResult, params.getSearchInfo());
                        System.gc();
                    }
                });
            } else {
                log.warn("runWithTypeParams the results contain no views. " + req.getDso().getDisplayName());
            }
        }
        // set complete via the special predicate
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
