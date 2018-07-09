package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.List;

import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.core.logfile.LogFileAggregateInfo;
import org.eclipselabs.real.core.logfile.LogFileController;
import org.eclipselabs.real.gui.e4swt.dialogs.LogFilesInfoDialog;

public class HandlerLogFilesInfo {
    private static final Logger log = LogManager.getLogger(HandlerLogFilesInfo.class);

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) final Shell parent,
            IEclipseContext ctxt, MApplication application) {
        List<LogFileAggregateInfo> infoList = LogFileController.INSTANCE.getAggregateInfos();
        if (infoList != null) {
            LogFilesInfoDialog rfDialog = application.getContext().get(LogFilesInfoDialog.class);
            if (rfDialog == null) {
                rfDialog = ContextInjectionFactory.make(LogFilesInfoDialog.class, ctxt);
                rfDialog.setSingleScopeContext(application.getContext(), LogFilesInfoDialog.class, rfDialog);
                rfDialog.initInfoList(infoList);
                rfDialog.open();
            } else {
                if (!rfDialog.makeActive()) {
                    log.warn("A log files info dialog is already active");
                }
            }
        } else {
            log.error("Null list of infos returned");
            MessageBox errorBox = new MessageBox(parent, SWT.CLOSE | SWT.BORDER | SWT.OK | SWT.ICON_ERROR);
            errorBox.setMessage("Internal error");
            errorBox.open();
        }
    }

    @CanExecute
    public boolean canExecute() {
        // TODO Your code goes here
        return true;
    }

}