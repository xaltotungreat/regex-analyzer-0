package org.eclipselabs.real.gui.e4swt.handlers;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.core.logfile.LogFileAggregateInfo;
import org.eclipselabs.real.core.logfile.LogFileControllerImpl;
import org.eclipselabs.real.gui.e4swt.dialogs.LogFilesInfoDialog;

public class HandlerAddFolder {
    private static final Logger log = LogManager.getLogger(HandlerAddFolder.class);
    // the static variable for the previously opened file
    // if the handler is recreated the value will not be changed
    private static String prevPath;

    @Inject
    UISynchronize uiSynch;

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) final Shell parent,
            final IEclipseContext ctxt, final MApplication application) {
        DirectoryDialog openDialog = new DirectoryDialog (parent, SWT.OPEN);
        if (prevPath == null) {
            prevPath = Platform.getInstallLocation().getURL().getFile();
            if (prevPath.startsWith("/")) {
                prevPath = prevPath.substring(1);
            }
            prevPath = prevPath.replace("/", File.separator);

        }
        openDialog.setFilterPath(prevPath);
        openDialog.setMessage("Add folder");
        final String newFld = openDialog.open();
        if (newFld != null) {
            prevPath = newFld;
            CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> settableFutureList = LogFileControllerImpl.INSTANCE.addFolderListFutures(newFld);
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
                        log.error("Error receiving a list of futures", t);
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
                errorBox.setMessage("No files have been added!");
                errorBox.open();
            }
        }
    }

}