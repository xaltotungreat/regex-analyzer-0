package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerRemoveAllColors {
    @Execute
    public void execute(MWindow mainWindow, IEclipseContext ctxt) {
        MPart activePart = ctxt.get(MPart.class);
        GUISearchResult srObj = (GUISearchResult) activePart.getObject();
        srObj.removeAllColors();
    }

}