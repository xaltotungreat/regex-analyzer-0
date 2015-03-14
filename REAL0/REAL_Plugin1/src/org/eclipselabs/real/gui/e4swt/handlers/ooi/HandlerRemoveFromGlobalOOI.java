package org.eclipselabs.real.gui.e4swt.handlers.ooi;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.OOIHelper;

public class HandlerRemoveFromGlobalOOI {
    
    private static final Logger log = LogManager.getLogger(HandlerRemoveFromGlobalOOI.class);
    @Inject
    EModelService modelService;
    
    @Inject
    UISynchronize uiSynch;
    
    @Execute
    public void execute(final MWindow execWindow, MPart execPart) {
        Map<String, GlobalOOIInfo> globalObjList = null;
        if ((execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) && (execPart != null)) {
            globalObjList = (Map<String, GlobalOOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
            GlobalOOIInfo selGlobalOOI = null;
            TreeItem[] selItems = (TreeItem[])execPart.getContext().get(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM);
            if ((selItems != null) && (selItems.length > 0)) {
                if (selItems[0].getData(IEclipse4Constants.DATA_GLOBAL_OOI_KEY) != null) {
                    selGlobalOOI = (GlobalOOIInfo)selItems[0].getData(IEclipse4Constants.DATA_GLOBAL_OOI_KEY);
                }
            }
            if ((selGlobalOOI != null) && (globalObjList.containsKey(selGlobalOOI.getTextPattern().pattern()))) {
                MPartStack resultsStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, execWindow);
                OOIHelper.removeGlobalOOI(selGlobalOOI, execWindow.getContext(), uiSynch, resultsStack);
            } else {
                log.error("execute selGlobalOOI is null or not in the map. selGlobalOOI=" + selGlobalOOI + " globalObjList=" + globalObjList);
            }
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext execCtxt) {
        boolean canRemoveFromGlobaList = false;
        TreeItem[] selItems = (TreeItem[])execCtxt.get(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM);
        if ((selItems != null) && (selItems.length > 0)) {
            if (selItems[0].getData(IEclipse4Constants.DATA_GLOBAL_OOI_KEY) != null) {
                canRemoveFromGlobaList = true;
            }
        } else {
            log.info("Command not executed for items " + Arrays.toString(selItems));
        }
        return canRemoveFromGlobaList;
    }

}