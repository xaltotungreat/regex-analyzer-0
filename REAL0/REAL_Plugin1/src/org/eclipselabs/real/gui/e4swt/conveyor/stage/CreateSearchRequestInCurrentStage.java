package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.core.searchobject.ISearchProgressMonitor;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class CreateSearchRequestInCurrentStage extends ConveyorStageBase {

    public CreateSearchRequestInCurrentStage() {
        super(StageID.CREATE_SEARCH_REQUEST);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        CompletableFuture<Void> returnFuture = CompletableFuture.completedFuture(null);
        final ISearchProgressMonitor newMonitor = req.getDso().getSearchObject().getSearchProgressMonitor();
        final GUISearchResult partSRObj = (GUISearchResult) params.getSearchPart().getObject();

        final List<String> tmpList = new ArrayList<String>();
        req.getUiSynch().syncExec(new Runnable() {

            @Override
            public void run() {
                tmpList.add(partSRObj.getText());
            }
        });
        if ((tmpList.isEmpty()) || (tmpList.get(0) == null)) {
            params.setProceed(false);
            params.setAbortMessage("No text found in the current tab");
            return returnFuture;
        }

        newMonitor.setTotalSOFiles(1);
        newMonitor.setTotalWork(1);
        PerformSearchRequest searchReq = new PerformSearchRequest(tmpList.get(0), newMonitor, params.getCurrentParamMap());

        params.setSearchRequest(searchReq);
        return returnFuture;
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
