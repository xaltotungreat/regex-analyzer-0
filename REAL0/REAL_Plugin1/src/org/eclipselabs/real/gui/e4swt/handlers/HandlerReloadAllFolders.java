package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.core.logfile.LogFileAggregateInfo;
import org.eclipselabs.real.core.logfile.LogFileControllerImpl;
import org.eclipselabs.real.gui.e4swt.dialogs.LogFilesInfoDialog;

public class HandlerReloadAllFolders {
    private static final Logger log = LogManager.getLogger(HandlerReloadAllFolders.class);

    @Inject
    UISynchronize uiSynch;

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) final Shell parent,
            final IEclipseContext ctxt, final MApplication application) {
        CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> settableFutureList = LogFileControllerImpl.INSTANCE.reloadCurrentFoldersListFutures();
        if (settableFutureList != null) {
            settableFutureList.handle((final List<CompletableFuture<LogFileAggregateInfo>> arg0, Throwable t) -> {
                if (arg0 != null) {
                    log.debug("Received a list of futures size=" + arg0.size());
                    uiSynch.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            LogFilesInfoDialog rfDialog = application.getContext().get(LogFilesInfoDialog.class);
                            if (rfDialog != null) {
                                rfDialog.close();
                            }
                            rfDialog = ContextInjectionFactory.make(LogFilesInfoDialog.class, ctxt);
                            rfDialog.setSingleScopeContext(application.getContext(), LogFilesInfoDialog.class, rfDialog);
                            rfDialog.initFuturesList(arg0);
                            rfDialog.open();
                        }
                    });
                }
                if (t != null) {
                    log.error("Error receiving a list of futures", arg0);
                }
                return null;
            });
            /*Futures.addCallback(settableFutureList, new FutureCallback<List<ListenableFuture<LogFileAggregateInfo>>>() {

                @Override
                public void onSuccess(final List<ListenableFuture<LogFileAggregateInfo>> arg0) {
                    log.debug("Received a list of futures size=" + arg0.size());
                    uiSynch.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            LogFilesInfoDialog rfDialog = application.getContext().get(LogFilesInfoDialog.class);
                            if (rfDialog != null) {
                                rfDialog.close();
                            }
                            rfDialog = ContextInjectionFactory.make(LogFilesInfoDialog.class, ctxt);
                            rfDialog.setSingleScopeContext(application.getContext(), LogFilesInfoDialog.class, rfDialog);
                            rfDialog.initFuturesList(arg0);
                            rfDialog.open();
                        }
                    });
                }

                @Override
                public void onFailure(Throwable arg0) {
                    log.error("Error receiving a list of futures", arg0);
                }
            });*/

        } else {
            log.error("Null settable future returned");
            MessageBox errorBox = new MessageBox(parent, SWT.CLOSE | SWT.BORDER | SWT.OK | SWT.ICON_ERROR);
            errorBox.setMessage("No files have been reloaded!");
            errorBox.open();
        }
    }

    @CanExecute
    public boolean canExecute() {
        // TODO Your code goes here
        return true;
    }

}