package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.gui.e4swt.NamedBookmark;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class InstallBookmarksStage extends ConveyorStageBase {
    private static final Logger log = LogManager.getLogger(InstallBookmarksStage.class);

    public InstallBookmarksStage() {
        super(StageID.INSTALL_BOOKMARKS);
    }

    @Override
    protected CompletableFuture<Void> executeInternal(ConvSearchRequest req, ConvProductContext params) {
        GUISearchResult guiObj = params.getGuiObjectRef();
        if (guiObj == null) {
            log.error("executeInternal GUI Object is null");
            return CompletableFuture.completedFuture(null);
        }
        if (req.getNamedBookmarkList() == null) {
            log.debug("executeInternal Bookmark List is null");
            return CompletableFuture.completedFuture(null);
        }
        for (NamedBookmark nmBk : req.getNamedBookmarkList()) {
            guiObj.addToLocalBookmarks(nmBk, true);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void cancelInternal(boolean mayInterrupt) {
        // do nothing
    }

}
