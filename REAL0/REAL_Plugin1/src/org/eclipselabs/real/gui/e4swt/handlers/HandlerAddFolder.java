package org.eclipselabs.real.gui.e4swt.handlers;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.core.logfile.LogFileController;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.dialogs.LogFilesInfoDialog;

public class HandlerAddFolder {
    private static final Logger log = LogManager.getLogger(HandlerAddFolder.class);
    // the static variable for the previously opened file
    // if the handler is recreated the value will not be changed

    @Inject
    UISynchronize uiSynch;

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) final Shell parent,
            final IEclipseContext ctxt, final MApplication application, MWindow window) {
        DirectoryDialog openDialog = new DirectoryDialog (parent, SWT.OPEN);
        String prevPath = (String)ctxt.get(IEclipse4Constants.CONTEXT_ADD_FOLDER_PREV_PATH);
        if (prevPath == null) {
            if (System.getProperty("REAL.work.folder") != null) {
                prevPath = System.getProperty("REAL.work.folder");
            } else {
                prevPath = Platform.getInstallLocation().getURL().getFile();
                if (prevPath.startsWith("/")) {
                    prevPath = prevPath.substring(1);
                }
                prevPath = prevPath.replace("/", File.separator);
            }
            window.getContext().set(IEclipse4Constants.CONTEXT_ADD_FOLDER_PREV_PATH, prevPath);
            window.getContext().declareModifiable(IEclipse4Constants.CONTEXT_ADD_FOLDER_PREV_PATH);
        }
        openDialog.setFilterPath(prevPath);
        openDialog.setMessage("Add folder");
        final String newFld = openDialog.open();
        if (newFld != null) {
            window.getContext().modify(IEclipse4Constants.CONTEXT_ADD_FOLDER_PREV_PATH, newFld);
            LogFileController.ReloadFoldersResult addResult = LogFileController.INSTANCE.addFolder(newFld);
            if (addResult != null) {
                uiSynch.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        LogFilesInfoDialog rfDialog = application.getContext().get(LogFilesInfoDialog.class);
                        if (rfDialog != null) {
                            rfDialog.close();
                        }
                        rfDialog = ContextInjectionFactory.make(LogFilesInfoDialog.class, ctxt);
                        rfDialog.setSingleScopeContext(application.getContext(), LogFilesInfoDialog.class, rfDialog);
                        rfDialog.initWithAccumulator(addResult.getAccumulator(), addResult.getFuture());
                        rfDialog.open();
                    }
                });
            } else {
                log.error("Null result returned");
                MessageBox errorBox = new MessageBox(parent, SWT.CLOSE | SWT.BORDER | SWT.OK | SWT.ICON_ERROR);
                errorBox.setMessage("No files have been added!");
                errorBox.open();
            }
        }
    }

}