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
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerAddNamedBookmark {
    private static final Logger log = LogManager.getLogger(HandlerAddNamedBookmark.class);

    @Execute
    public void execute(IEclipseContext ctxt) {
        String bkmName;
        InputTextDialog stringKeyDialog = ContextInjectionFactory.make(InputTextDialog.class, ctxt);
        stringKeyDialog.init("Enter String Key");
        DialogResult<String> stringKeyResult = stringKeyDialog.open();
        if (stringKeyResult.getAction() == SWT.OK) {
            bkmName = stringKeyResult.getResult();
            MPart activePart = ctxt.get(MPart.class);
            if ((activePart != null) && (activePart.getObject() != null)) {
                final GUISearchResult srObj = (GUISearchResult) activePart.getObject();
                log.info("Adding to local bookmarks " + bkmName);
                srObj.addSelectionToLocalBookmarks(bkmName, true);
            }
        }

    }

    @CanExecute
    public boolean canExecute() {
        // TODO Your code goes here
        return true;
    }

}