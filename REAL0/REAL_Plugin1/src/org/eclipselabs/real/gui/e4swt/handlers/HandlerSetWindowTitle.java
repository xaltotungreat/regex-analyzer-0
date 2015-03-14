package org.eclipselabs.real.gui.e4swt.handlers;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.swt.SWT;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.dialogs.DialogResult;
import org.eclipselabs.real.gui.e4swt.dialogs.InputTextDialog;

public class HandlerSetWindowTitle {

    @Execute
    public void execute(MWindow window, IEclipseContext ctxt) {
        InputTextDialog titleDialog = ContextInjectionFactory.make(InputTextDialog.class, ctxt);
        titleDialog.init("Enter additional title");
        DialogResult<String> titleRes = titleDialog.open();
        if (titleRes.getAction() == SWT.OK) {
            window.setLabel(IEclipse4Constants.MAIN_WINDOW_TITLE + " - " + titleRes.getResult());
        }
    }

    @CanExecute
    public boolean canExecute() {
        // TODO Your code goes here
        return true;
    }

}