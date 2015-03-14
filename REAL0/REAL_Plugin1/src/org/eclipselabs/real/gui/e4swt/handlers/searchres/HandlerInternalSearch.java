package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.ConveyorMain;

public class HandlerInternalSearch {
    private static final Logger log = LogManager.getLogger(HandlerInternalSearch.class);

    @Execute
    public void execute(IEclipseContext ctxt,
            @Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_INTSEARCH_REQ_KEY) String reqKey) {
        ConvSearchRequest req = null;
        if (reqKey != null) {
            req = (ConvSearchRequest)ctxt.get(reqKey);
        } else {
            log.error("execute null request key");
        }
        if (req != null) {
            ContextInjectionFactory.inject(req, ctxt);
            ConveyorMain.INSTANCE.submitRequest(req);
        } else {
            log.error("canExecute the search request is not found in the context");
        }
    }

    @CanExecute
    public boolean canExecute() {
        return true;
    }

}