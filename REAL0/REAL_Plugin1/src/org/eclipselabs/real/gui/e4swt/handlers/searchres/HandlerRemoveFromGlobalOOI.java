package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
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
        if ((execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) && (execPart != null)) {
            Map<String, GlobalOOIInfo> globalObjList = (Map<String, GlobalOOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
            String selectedText = (String)execPart.getContext().get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED);
            if (globalObjList.containsKey(Pattern.quote(selectedText))) {
                GlobalOOIInfo rmInfo = globalObjList.get(Pattern.quote(selectedText));
                MPartStack resultsStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, execWindow);
                OOIHelper.removeGlobalOOI(rmInfo, execWindow.getContext(), uiSynch, resultsStack);
            } else {
                log.error("execute selectedText not in the GlobOOI map. selectedText=" + selectedText + " globalObjList=" + globalObjList);
            }
        }
    }

    @CanExecute
    public boolean canExecute(MWindow execWindow, MPart execPart) {
        boolean canRemoveFromGlobaList = false;
        if (execPart != null) {
            String selectedText = (String)execPart.getContext().get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED);
            if (execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) {
                Map<String, GlobalOOIInfo> globalObjList = (Map<String, GlobalOOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
                if (globalObjList.containsKey(Pattern.quote(selectedText))) {
                    canRemoveFromGlobaList = true;
                }
            }
        }
        return canRemoveFromGlobaList;
    }

}