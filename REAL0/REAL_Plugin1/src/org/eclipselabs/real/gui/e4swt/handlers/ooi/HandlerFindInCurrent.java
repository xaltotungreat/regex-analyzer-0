package org.eclipselabs.real.gui.e4swt.handlers.ooi;

import java.util.Arrays;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerFindInCurrent {
    private static final Logger log = LogManager.getLogger(HandlerFindInCurrent.class);

    @Execute
    public void execute(IEclipseContext ctxt, MWindow mainWindow, EModelService modelService,
            @Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_TEXT) String textToFind,
            @Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_DIRECTION) String directionStr,
            @Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_WRAP) String wrapFindStr,
            @Named(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_IS_REGEX) String isRegexStr) {
        if (textToFind != null) {
            MPartStack srPartStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, mainWindow);
            MPart activePart = (MPart)srPartStack.getSelectedElement();
            GUISearchResult guiSR = null;
            if (activePart != null) {
                guiSR = (GUISearchResult)activePart.getObject();
            }
            if (guiSR != null) {
                int direction = SWT.DOWN;
                if (directionStr != null) {
                    direction = Integer.parseInt(directionStr);
                }
                Boolean wrapFind = true;
                if (wrapFindStr != null) {
                    wrapFind = Boolean.parseBoolean(wrapFindStr);
                }
                Boolean isRegex = false;
                if (isRegexStr != null) {
                    isRegex = Boolean.parseBoolean(isRegexStr);
                }
                guiSR.findRegex(textToFind, direction,
                        wrapFind, isRegex);
            } else {
                log.warn("execute not executed GUISR is null");
            }
        } else {
            log.error("textToFind is null");
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext ctxt) {
        boolean returnValue = false;
        TreeItem[] selItems = (TreeItem[])ctxt.get(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM);
        if (ctxt.get(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_TEXT) != null) {
            returnValue = true;
        } else {
            log.info("Command not executed for items " + Arrays.toString(selItems));
        }
        return returnValue;
    }

}