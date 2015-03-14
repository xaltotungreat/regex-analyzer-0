package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.gui.e4swt.OOIInfo;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class InstallOOIStage extends ConveyorStageBase {

    private static final Logger log = LogManager.getLogger(InstallOOIStage.class);

    public InstallOOIStage() {
        super(StageID.INSTALL_OOI);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        GUISearchResult guiObj = params.getGuiObjectRef();
        if (guiObj == null) {
            log.error("executeInternal GUI Object is null");
            return CompletableFuture.completedFuture(null);
        }
        if (req.getLocalOoiList() == null) {
            log.debug("executeInternal Local OOI List is null");
            return CompletableFuture.completedFuture(null);
        }
        for (OOIInfo localOOI : req.getLocalOoiList()) {
            guiObj.setStyleForPattern(localOOI.getTextPattern(), localOOI.getStyle(), true);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
