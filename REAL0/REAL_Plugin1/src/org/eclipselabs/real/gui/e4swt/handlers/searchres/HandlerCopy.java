package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerCopy {
    private static final Logger log = LogManager.getLogger(HandlerCopy.class);
    @Execute
    public void execute(IEclipseContext ctxt) {
        MPart activePart = ctxt.get(MPart.class);
        log.info("Part in context " + activePart.getElementId());
        GUISearchResult srObj = (GUISearchResult) activePart.getObject();
        srObj.copyToClipboard();
    }
    
    @CanExecute
    public boolean canExecute(MWindow execWindow, MPart execPart) {
        boolean canCopy = false;
        if (execPart != null) {
            String selectedText = (String)execPart.getContext().get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED);
            if ((selectedText != null) && (!"".equals(selectedText))) {
                canCopy = true;
            }
        }
        return canCopy;
    }

}