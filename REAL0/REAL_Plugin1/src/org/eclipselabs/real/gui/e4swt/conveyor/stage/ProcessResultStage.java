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
import org.eclipselabs.real.gui.core.result.DisplayResultImpl;
import org.eclipselabs.real.gui.core.result.IComplexDisplayResult;
import org.eclipselabs.real.gui.core.result.IDisplayResult;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class ProcessResultStage extends ConveyorStageBase {
    private static final Logger log = LogManager.getLogger(ProcessResultStage.class);

    private boolean installGlobalObjectsSilently;

    public ProcessResultStage(boolean goSilently) {
        super(StageID.PROCESS_RESULT);
        installGlobalObjectsSilently = goSilently;
    }

    public ProcessResultStage(boolean goSilently, Predicate<IConveyorStage> complPred) {
        super(StageID.PROCESS_RESULT, complPred);
        installGlobalObjectsSilently = goSilently;
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
            params.setGuiObjectRef(partSRObj);
            // for scripts views may be added during the search
            R firstRes = (R)params.getResult().values().iterator().next();
            if (firstRes.viewsAvailable()) {
                final IComplexDisplayResult complRegResult = new ComplexDisplayResultImpl((Map<String, R>) params.getResult(), req.getDso());
                req.getUiSynch().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        partSRObj.setResult(complRegResult, params.getSearchInfo(), installGlobalObjectsSilently);
                        System.gc();
                    }
                });
            } else {
                final IDisplayResult dispResult = new DisplayResultImpl((Map<String, R>) params.getResult(), req.getDso());
                req.getUiSynch().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        partSRObj.setResult(dispResult, params.getSearchInfo(), installGlobalObjectsSilently);
                        System.gc();
                    }
                });
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
