package org.eclipselabs.real.gui.e4swt.handlers.ooi;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.parts.GUIOOI;

public class HandlerRemoveStringKey {
    private static final Logger log = LogManager.getLogger(HandlerRemoveStringKey.class);
    
    @Execute
    public void execute(IEclipseContext ctxt) {
        TreeItem[] selItems = (TreeItem[])ctxt.get(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM);
        MPart activePart = ctxt.get(MPart.class);
        GUIOOI globObjGUI = (GUIOOI)activePart.getObject();
        if ((selItems != null) && (selItems.length > 0) && (globObjGUI != null)) {
            if (selItems[0].getData(IEclipse4Constants.DATA_GLOBAL_OOI_STRING_KEY) != null) { 
                globObjGUI.removeSelectedGlobalOOIStringKey();
            } else if (selItems[0].getData(IEclipse4Constants.DATA_NAMED_BOOKMARK_STRING_KEY) != null) {
                globObjGUI.removeSelectedBookmarkStringKey();
            }
        } else {
            log.info("execute Command not executed for items " + Arrays.toString(selItems));
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext ctxt) {
        Boolean returnValue = false;
        TreeItem[] selItems = (TreeItem[])ctxt.get(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM);
        if ((selItems != null) && (selItems.length > 0)) {
            if ((selItems[0].getData(IEclipse4Constants.DATA_GLOBAL_OOI_STRING_KEY) != null) 
                    || (selItems[0].getData(IEclipse4Constants.DATA_NAMED_BOOKMARK_STRING_KEY) != null)) {
                returnValue = true;
            }
        } else {
            log.info("canExecute Command not executed for items " + Arrays.toString(selItems));
        }
        return returnValue;
    }

}