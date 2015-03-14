package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.OOIHelper;

public class HandlerRemoveAllGlobalOOI {
    private static final Logger log = LogManager.getLogger(HandlerRemoveAllGlobalOOI.class);
    
    @Inject
    UISynchronize uiSynch;
    
    @Execute
    public void execute(final MWindow execWindow, EModelService modelService) {
        MessageBox noTextSelected = new MessageBox((Shell) execWindow.getContext().get(IServiceConstants.ACTIVE_SHELL),
                SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
        noTextSelected.setText("Confirmation");
        noTextSelected.setMessage("Are you sure you want to delete all Global OOI?");
        int userConfirm = noTextSelected.open();
        
        if ((execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) && (userConfirm == SWT.OK)) {
            log.info("Removing al global OOI");
            final Map<String, GlobalOOIInfo> globalObjList = (Map<String, GlobalOOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
            
            if (globalObjList.isEmpty()) {
                return;
            }
            MPartStack resultsStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, execWindow);
            OOIHelper.removeGlobalOOI(new ArrayList<GlobalOOIInfo>(globalObjList.values()), execWindow.getContext(), uiSynch, resultsStack);
        } else {
            if (execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) == null) {
                MessageBox emptyGlobalOOI = new MessageBox((Shell) execWindow.getContext().get(IServiceConstants.ACTIVE_SHELL),
                        SWT.ICON_ERROR | SWT.OK);
                emptyGlobalOOI.setText("Error");
                emptyGlobalOOI.setMessage("The list of Global OOI is empty");
                emptyGlobalOOI.open();
            }
        }
    }

    @CanExecute
    public boolean canExecute(MWindow execWindow) {
        Map<String, GlobalOOIInfo> globalObjList = null;
        boolean canExec = false;
        if (execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) {
            globalObjList = (Map<String, GlobalOOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
            if (!globalObjList.isEmpty()) {
                canExec = true;
            }
        }
        return canExec;
    }

}