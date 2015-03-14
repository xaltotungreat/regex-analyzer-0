package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;

public class StartGUIUpdateStage extends ConveyorStageBase {

    private static final Logger log = LogManager.getLogger(StartGUIUpdateStage.class);

    private ExecutorService guiExecutor;

    private volatile GUIUpdater updater;

    private volatile GUIUpdater.IStatusFormatter statusFormatter;

    private long updateInterval;

    public StartGUIUpdateStage(ExecutorService exec, GUIUpdater.IStatusFormatter upd, long updIntervalMs) {
        super(StageID.START_UPDATE_GUI);
        guiExecutor = exec;
        statusFormatter = upd;
        updateInterval = updIntervalMs;
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        final CompletableFuture<Void> result = new CompletableFuture<Void>();
        updater = new GUIUpdater(result, params, req.getUiSynch(), updateInterval, statusFormatter);
        guiExecutor.execute(updater);
        return result;
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // TODO Auto-generated method stub

    }

}
