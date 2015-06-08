package org.eclipselabs.real.gui.e4swt.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

public class HandlerTest1 {
    @Execute
    public void execute(IEclipseContext ctxt) {
        //NattableDialog dlg = ContextInjectionFactory.make(NattableDialog.class, ctxt);
        //dlg.open();
    }

    @CanExecute
    public boolean canExecute() {
        // TODO Your code goes here
        return true;
    }

}