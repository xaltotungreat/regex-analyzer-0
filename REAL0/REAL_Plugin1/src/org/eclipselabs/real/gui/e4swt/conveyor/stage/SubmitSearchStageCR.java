package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.LogFileControllerImpl;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult.SearchResultActiveState;

public class SubmitSearchStageCR extends ConveyorStageBase {

    private static final Logger log = LogManager.getLogger(SubmitSearchStageCR.class);

    private volatile ConvProductContext context;

    public SubmitSearchStageCR() {
        super(StageID.SUBMIT_SEARCH);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, final ConvProductContext params) {
        context = params;
        return runWithTypeParams(req, params);
    }

    private <R extends IKeyedComplexSearchResult<O,W,X,String>,
                O extends IComplexSearchResultObject<W,X,String>,
                V extends ISearchObject<W,X>,
                W extends ISearchResult<X>, X extends ISearchResultObject>
            CompletableFuture<Void> runWithTypeParams(ConvSearchRequest req, final ConvProductContext params) {
        final IKeyedComplexSearchObject<R,O,V,W,X,String> currSO = (IKeyedComplexSearchObject<R,O,V,W,X,String>)req.getDso().getSearchObject();
        final GUISearchResult partSRObj = (GUISearchResult) params.getSearchPart().getObject();

        CompletableFuture<? extends Map<String, R>> future = LogFileControllerImpl.INSTANCE.getLogAggregate(currSO.getLogFileType()).submitSearch(currSO, params.getSearchRequest());
        CompletableFuture<Void> methodResult = future.handle((Map<String, R> arg0, Throwable t) -> {
            if ((arg0 != null) && (!arg0.isEmpty())) {
                log.info("ComplexRegex Result name=" + currSO.getSearchObjectName() + " size=" + arg0.size());
                params.setResult((Map<String, IKeyedComplexSearchResult<? extends
                        IComplexSearchResultObject<? extends ISearchResult<? extends ISearchResultObject>,
                                ? extends ISearchResultObject, String>,
                        ? extends ISearchResult<? extends ISearchResultObject>,
                        ? extends ISearchResultObject, String>>) arg0);
                params.getSearchInfo().setFoundObjects(params.getSearchRequest().getProgressMonitor().getObjectsFound());
                params.getSearchInfo().setCustomProgressKeys(params.getSearchRequest().getProgressMonitor().getAllCustomNVPs());
            } else {
                params.setProceed(false);
                if (arg0 != null) {
                    params.setAbortMessage("No objects found for searchID=" + params.getSearchID());
                } else if (t != null) {
                    params.setAbortMessage("Search failure for searchID=" + params.getSearchID() + " " + t.getMessage());
                    log.error("Display Result Error ", t);
                }
                // dispose the part if the result is null or empty and the part is not already disposed
                if (!SearchResultActiveState.DISPOSED.equals(partSRObj.getMainSearchState())) {
                    req.getUiSynch().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            partSRObj.disposeViewResult(params.getSearchID());
                            log.info("No objects found for searchID=" + params.getSearchID());
                            System.gc();
                        }
                    });
                }
            }
            // complete the monitor anyway. The search has completed even if completed
            // exceptionally
            params.getSearchRequest().getProgressMonitor().setComplete(true);
            return null;
        });
        return methodResult;
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        if (context != null) {
            context.getSearchRequest().getProgressMonitor().setCancelled(true);
        }
    }

}
