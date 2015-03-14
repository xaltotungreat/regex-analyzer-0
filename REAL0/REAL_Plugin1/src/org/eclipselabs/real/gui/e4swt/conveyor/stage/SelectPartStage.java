package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class SelectPartStage extends ConveyorStageBase {

    public SelectPartStage() {
        super(StageID.SELECT_PART);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        final MPart selPart = (MPart)req.getSearchTabsStack().getSelectedElement();
        if (selPart == null) {
            params.setProceed(false);
            params.setAbortMessage("Search in current no current tab available");
        }
        params.setSearchPart(selPart);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
