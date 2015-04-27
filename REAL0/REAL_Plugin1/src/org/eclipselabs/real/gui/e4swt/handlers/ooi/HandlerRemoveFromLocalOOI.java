package org.eclipselabs.real.gui.e4swt.handlers.ooi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.OOIInfo;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerRemoveFromLocalOOI {
    private static final Logger log = LogManager.getLogger(HandlerRemoveFromLocalOOI.class);
    
    @Execute
    public void execute(MWindow execWindow, MPart execPart, EModelService modelService) {
        //Map<String,OOIInfo> localOOIList = null;
        if ((execWindow.getContext().get(IEclipse4Constants.CONTEXT_LOCAL_OOI_LIST) != null) && (execPart != null)) {
            //localOOIList = (Map<String,OOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_LOCAL_OOI_LIST);
            OOIInfo selLocalOOI = null;
            TreeItem[] selItems = (TreeItem[])execPart.getContext().get(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM);
            if ((selItems != null) && (selItems.length > 0)) {
                if (selItems[0].getData(IEclipse4Constants.DATA_LOCAL_OOI_KEY) != null) {
                    selLocalOOI = (OOIInfo)selItems[0].getData(IEclipse4Constants.DATA_LOCAL_OOI_KEY);
                }
            } else {
                log.warn("execute no tree items selected");
            }
            if (selLocalOOI != null) {
                MPartStack srPartStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, execWindow);
                MPart activePart = (MPart)srPartStack.getSelectedElement();
                GUISearchResult guiSR = null;
                if (activePart != null) {
                    guiSR = (GUISearchResult)activePart.getObject();
                }
                if (guiSR != null) {
                    guiSR.removeStyleForPattern(selLocalOOI.getTextPattern());
                } else {
                    log.warn("execute not executed GUISR is null");
                }
            } else {
                log.warn("execute no local OOI is selected");
            }
        } else {
            log.error("execute no local OOI list or the part is empty");
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext ctxt) {
        boolean returnValue = false;
        TreeItem[] selItems = (TreeItem[])ctxt.get(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM);
        if ((selItems != null) && (selItems.length > 0)) {
            if (selItems[0].getData(IEclipse4Constants.DATA_LOCAL_OOI_KEY) != null) {
                returnValue = true;
            }
        }
        return returnValue;
    }

}