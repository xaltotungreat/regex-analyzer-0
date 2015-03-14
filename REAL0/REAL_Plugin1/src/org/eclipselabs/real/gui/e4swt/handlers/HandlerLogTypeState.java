package org.eclipselabs.real.gui.e4swt.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;

public class HandlerLogTypeState {
    private static final Logger log = LogManager.getLogger(HandlerLogTypeState.class);

    @Inject
    EModelService modelService;

    @Execute
    public void execute(IEclipseContext ctxt,
            @Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_LOG_TYPE_NAME) String logTypeName,
            MWindow activeWindow) {
        LogFileType currType = LogFileTypes.INSTANCE.getLogFileType(logTypeName);
        if (currType != null) {
            currType.setEnabled(!currType.isEnabled());
            log.info(logTypeName + " set to " + currType.isEnabled());
        } else {
            log.error("LogFileType not found for name " + logTypeName);
        }
    }

}