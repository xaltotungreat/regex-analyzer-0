package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public interface IConveyorStage {

    boolean isExecuteAsync();
    void setExecuteAsync(boolean executeAsync);
    CompletableFuture<Void> execute(ConvSearchRequest req, ConvProductContext params);
    void cancel(boolean mayInterrupt);

    IConveyorStage addChild(IConveyorStage st);
    void setParent(IConveyorStage pt);

}
