package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.List;

import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.core.logfile.LogFileControllerImpl;
import org.eclipselabs.real.gui.e4swt.dialogs.DialogResult;
import org.eclipselabs.real.gui.e4swt.dialogs.RemoveFoldersDialog;

public class HandlerRemoveFolders {

    private static final Logger log = LogManager.getLogger(HandlerRemoveFolders.class);
    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
            IEclipseContext ctxt, MApplication application) {

        RemoveFoldersDialog remFld = application.getContext().get(RemoveFoldersDialog.class);
        if (remFld == null) {
            remFld = ContextInjectionFactory.make(RemoveFoldersDialog.class, ctxt);
            remFld.setSingleScopeContext(application.getContext(), RemoveFoldersDialog.class, remFld);
            remFld.setAllFoldersList(LogFileControllerImpl.INSTANCE.getAllFolders());
            DialogResult<List<String>> dialogResult = remFld.open();
            if (dialogResult.getAction() == SWT.OK) {
                for (String dirStr : dialogResult.getResult()) {
                    LogFileControllerImpl.INSTANCE.removeFolder(dirStr);
                }
            }
        } else {
            if (!remFld.makeActive()) {
                log.warn("The remove folders dialog is disposed by left in the context");
            }
        }
    }

}