package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipselabs.real.gui.e4swt.dialogs.DialogResult;
import org.eclipselabs.real.gui.e4swt.dialogs.InputTextDialog;

public class HandlerSetTextTitle {
    private static final Logger log = LogManager.getLogger(HandlerSetTextTitle.class);
    @Execute
    public void execute(IEclipseContext ctxt) {
        MPart activePart = ctxt.get(MPart.class);
        if (activePart != null) {
            log.info("Part in context " + activePart.getElementId());
            InputTextDialog titleDialog = ContextInjectionFactory.make(InputTextDialog.class, ctxt);
            titleDialog.init("Enter view title");
            titleDialog.setInitialText(activePart.getLabel());
            DialogResult<String> titleRes = titleDialog.open();
            if (titleRes.getAction() == SWT.OK) {
                activePart.setLabel(titleRes.getResult());
            }
        } else {
            log.warn("Active part is null not setting the text title");
        }
    }

    @CanExecute
    public boolean canExecute() {
        return true;
    }

}