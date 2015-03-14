package org.eclipselabs.real.gui.e4swt.handlers;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.dialogs.FindDialog;

public class HandlerFindDialog {
    
    @Execute
    public void execute(IEclipseContext ctxt, MApplication application) {
        FindDialog fdDialog = application.getContext().get(FindDialog.class);
        if (fdDialog == null) {
            fdDialog = ContextInjectionFactory.make(FindDialog.class, ctxt);
            fdDialog.setSingleScopeContext(application.getContext(), FindDialog.class, fdDialog);
        }
        if (ctxt.get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED) != null) {
            String selText = (String)ctxt.get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED);
            fdDialog.setInitialText(selText);
        }
        if (fdDialog.isClosed()) {
            fdDialog.init();
        }
        fdDialog.open();
    }

}