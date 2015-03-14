package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import java.io.File;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerSaveToTextFile {
    @Execute
    public void execute(MWindow mainWindow, IEclipseContext execCtxt, EModelService modelService) {
        FileDialog fd = new FileDialog((Shell) mainWindow.getContext().get(IServiceConstants.ACTIVE_SHELL), SWT.SAVE);
        String[] filterExt = new String[] {"*.txt"};
        fd.setFilterExtensions(filterExt);
        String wsFilePath = fd.open();
        File wsFile = null;
        int confirmOverwrite = SWT.OK;
        if (wsFilePath != null) {
            wsFile = new File(wsFilePath);
            if (wsFile.exists()) {
                MessageBox confirmOverwriteBox = new MessageBox((Shell) mainWindow.getContext().get(IServiceConstants.ACTIVE_SHELL),
                        SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                confirmOverwriteBox.setText("Confirmaion");
                confirmOverwriteBox.setMessage("The file already exists. Do you want to overwrite?");
                confirmOverwrite = confirmOverwriteBox.open();
            }
        }
        if ((wsFile != null) && (confirmOverwrite == SWT.OK)) {
            MPartStack srPartStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, mainWindow);
            MPart activePart = (MPart)srPartStack.getSelectedElement();
            GUISearchResult guiSR = null;
            if (activePart != null) {
                guiSR = (GUISearchResult)activePart.getObject();
            }
            if (guiSR != null) {
                guiSR.saveActiveTextToFile(wsFile);
            }
        }
    }

    @CanExecute
    public boolean canExecute() {
        // TODO Your code goes here
        return true;
    }

}