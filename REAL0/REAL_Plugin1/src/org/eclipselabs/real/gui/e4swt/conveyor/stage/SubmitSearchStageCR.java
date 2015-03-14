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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

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
        final CompletableFuture<Void> methodResult = new CompletableFuture<Void>();
        final IKeyedComplexSearchObject<R,O,V,W,X,String> currSO = (IKeyedComplexSearchObject<R,O,V,W,X,String>)req.getDso().getSearchObject();
        final GUISearchResult partSRObj = (GUISearchResult) params.getSearchPart().getObject();

        final ListenableFuture<? extends Map<String, R>> future = LogFileControllerImpl.INSTANCE.getLogAggregate(currSO.getLogFileType()).submitSearch(currSO, params.getSearchRequest());
        Futures.addCallback(future, new FutureCallback<Map<String, R>>() {
            @Override
            public void onSuccess(Map<String, R> arg0) {
                if (SearchResultActiveState.DISPOSED.equals(partSRObj.getMainSearchState())) {
                    log.warn("onSuccess Part already closed not rendering");
                    return;
                }
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
                    params.setAbortMessage("No objects found for searchID=" + params.getSearchID());

                    req.getUiSynch().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            partSRObj.disposeViewResult(params.getSearchID());
                            log.info("No objects found for searchID=" + params.getSearchID());
                            System.gc();
                        }
                    });
                }
                params.getSearchRequest().getProgressMonitor().setComplete(true);
                methodResult.complete(null);
            }

            @Override
            public void onFailure(Throwable arg0) {
                log.error("Display Result Error ", arg0);
                params.setProceed(false);
                params.setAbortMessage("Search failure for searchID=" + params.getSearchID() + " " + arg0.getMessage());
                if (SearchResultActiveState.DISPOSED.equals(partSRObj.getMainSearchState())) {
                    log.warn("onSuccess Part already closed not rendering");
                    return;
                }
                params.getSearchRequest().getProgressMonitor().setComplete(true);
                methodResult.complete(null);
            }
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
