package org.eclipselabs.real.gui.e4swt.handlers;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;

public class HandlerAbout {
    private static final Logger log = LogManager.getLogger(HandlerAbout.class);
    protected static final String CONTEXT_VARIABLE_NAME = "AboutWindowVisible"; 
    @Inject
    EModelService modelService;
    
    @Inject
    MApplication application;
    
    @Execute
    public void execute(MWindow mainWindow) {
        Boolean aboutWindowVisible = (Boolean)application.getContext().get(CONTEXT_VARIABLE_NAME);
        if ((aboutWindowVisible == null) || (!aboutWindowVisible)) {
            log.debug("Showing About window");
            MTrimmedWindow aboutWindowModel = (MTrimmedWindow) modelService.find(IEclipse4Constants.APP_MODEL_WINDOW_ABOUT, application);
            MTrimmedWindow aboutWindow = (MTrimmedWindow) modelService.cloneElement(aboutWindowModel, mainWindow);
            aboutWindow.setElementId("ShownAboutWindow");
            aboutWindow.setVisible(true);
            aboutWindow.setToBeRendered(true);
            //aboutWindow.setOnTop(true);
            application.getChildren().add(aboutWindow);
            if (aboutWindowVisible == null) {
                application.getContext().set(CONTEXT_VARIABLE_NAME, true);
                application.getContext().declareModifiable(CONTEXT_VARIABLE_NAME);
            } else {
                application.getContext().modify(CONTEXT_VARIABLE_NAME, true);
            }
        } else {
            log.debug("About window already open");
        }
    }

}