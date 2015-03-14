package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class RootStage extends ConveyorStageBase {

    public RootStage() {
        super(StageID.ROOT);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        return CompletableFuture.<Void>completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
