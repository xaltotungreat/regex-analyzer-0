package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class SetPositionsStage extends ConveyorStageBase {
    private static final Logger log = LogManager.getLogger(SetPositionsStage.class);

    public SetPositionsStage() {
        super(StageID.SET_POSITIONS);
    }

    public SetPositionsStage(Predicate<IConveyorStage> complPred) {
        super(StageID.SET_POSITIONS, complPred);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        final GUISearchResult guiObj = params.getGuiObjectRef();
        if (guiObj == null) {
            log.error("executeInternal GUI Object is null");
            return CompletableFuture.completedFuture(null);
        }
        if ((req.getCaretPosition() != null) && (req.getCaretPosition() > 0)
                && (req.getSelectedIndex() != null) && (req.getSelectedIndex() > 0)) {
            req.getUiSynch().syncExec(new Runnable() {
                @Override
                public void run() {
                    guiObj.setCaretPosition(req.getCaretPosition());
                    guiObj.setSelectedIndex(params.getSearchID(), req.getSelectedIndex());
                }
            });
        } else if ((req.getCaretPosition() != null) && (req.getCaretPosition() > 0)) {
            req.getUiSynch().syncExec(new Runnable() {
                @Override
                public void run() {
                    guiObj.setCaretPosition(req.getCaretPosition());
                }
            });
        } else if ((req.getSelectedIndex() != null) && (req.getSelectedIndex() > 0)) {
            req.getUiSynch().syncExec(new Runnable() {
                @Override
                public void run() {
                    guiObj.setSelectedIndex(params.getSearchID(), req.getSelectedIndex());
                }
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
