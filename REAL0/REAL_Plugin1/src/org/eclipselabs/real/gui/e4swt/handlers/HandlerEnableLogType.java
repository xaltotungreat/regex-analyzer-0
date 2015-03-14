package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.Map;

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
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.gui.e4swt.dialogs.DialogResult;
import org.eclipselabs.real.gui.e4swt.dialogs.EnableLogTypesDialog;

public class HandlerEnableLogType {

    private static final Logger log = LogManager.getLogger(HandlerEnableLogType.class);

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent,
            IEclipseContext ctxt, MApplication application) {

        EnableLogTypesDialog enableTypeDialog = application.getContext().get(EnableLogTypesDialog.class);
        if (enableTypeDialog == null) {
            enableTypeDialog = ContextInjectionFactory.make(EnableLogTypesDialog.class, ctxt);
            enableTypeDialog.setSingleScopeContext(application.getContext(), EnableLogTypesDialog.class, enableTypeDialog);
            enableTypeDialog.setInitialValues(LogFileTypes.INSTANCE.getEnableMap());
            //remFld.setAllFoldersList(LogFileControllerImpl.INSTANCE.getAllFolders());
            DialogResult<Map<String, Boolean>> dialogResult = enableTypeDialog.open();
            if (dialogResult.getAction() == SWT.OK) {
                LogFileTypes.INSTANCE.setLogTypeEnableStates(dialogResult.getResult());
            }
        } else {
            if (!enableTypeDialog.makeActive()) {
                log.warn("The enable log type dialog is disposed but left in the context");
            }
        }
    }

}