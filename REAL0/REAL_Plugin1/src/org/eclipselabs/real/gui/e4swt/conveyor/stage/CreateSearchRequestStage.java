package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.core.searchobject.ISearchProgressMonitor;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class CreateSearchRequestStage extends ConveyorStageBase {

    public CreateSearchRequestStage() {
        super(StageID.CREATE_SEARCH_REQUEST);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        final ISearchProgressMonitor newMonitor = req.getDso().getSearchObject().getSearchProgressMonitor();
        newMonitor.setTotalWork(1);
        PerformSearchRequest searchReq = new PerformSearchRequest(null, newMonitor, params.getCurrentParamMap(),
                null, null);
        params.setSearchRequest(searchReq);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }
}
