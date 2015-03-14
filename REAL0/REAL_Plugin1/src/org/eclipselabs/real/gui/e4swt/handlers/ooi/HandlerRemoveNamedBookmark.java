package org.eclipselabs.real.gui.e4swt.handlers.ooi;

import java.util.UUID;

import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerRemoveNamedBookmark {
    private static final Logger log = LogManager.getLogger(HandlerRemoveNamedBookmark.class);
    
    @Execute
    public void execute(IEclipseContext ctxt, EModelService modelService, MWindow mainWindow,
            @Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_SEL_BOOKMARK_UUID) Object selectedUUID) {
        log.debug("selectedBookmark UUID " + selectedUUID);
        if (selectedUUID != null) {
            MPartStack srPartStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, mainWindow);
            MPart activePart = (MPart)srPartStack.getSelectedElement();
            GUISearchResult guiSR = null;
            if (activePart != null) {
                guiSR = (GUISearchResult)activePart.getObject();
            }
            if (guiSR != null) {
                guiSR.removeLocalBookmark((UUID)selectedUUID, true);
            }
        }
    }

    @CanExecute
    public boolean canExecute(@Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_SEL_BOOKMARK_UUID) Object selectedUUID) {
        Boolean returnValue = false;
        if (selectedUUID != null) {
            returnValue = true;
        }
        return returnValue;
    }

}