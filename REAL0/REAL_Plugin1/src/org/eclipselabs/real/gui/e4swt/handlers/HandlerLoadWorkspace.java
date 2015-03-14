package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.e4swt.Eclipse4GUIBridge;
import org.eclipselabs.real.gui.e4swt.persist.SavedWorkspace;
import org.eclipselabs.real.gui.e4swt.util.WorkspaceLoader;

public class HandlerLoadWorkspace {

    private static final Logger log = LogManager.getLogger(HandlerLoadWorkspace.class);

    @CanExecute
    public boolean canExecute() {
        // keep this method just in case
        return true;
    }

    @Execute
    public void execute(final MWindow mainWindow, final IEclipseContext execCtxt) {
        Shell activeShell = (Shell) mainWindow.getContext().get(IServiceConstants.ACTIVE_SHELL);
        FileDialog fd = new FileDialog(activeShell, SWT.OPEN);
        String[] filterExt = new String[] {"*.xml"};
        fd.setFilterExtensions(filterExt);
        final String wsFilePath = fd.open();
        if (wsFilePath != null) {
            SavedWorkspace wsToLoad = null;
            try {
                wsToLoad = WorkspaceLoader.loadFromXML(wsFilePath);
            } catch (JAXBException e) {
                log.error("execute",e);
                MessageBox errorBox = new MessageBox(activeShell, SWT.ICON_ERROR | SWT.OK);
                errorBox.setMessage("Incorrect workspace file format? \n" + e.getClass().getName());
                errorBox.setText("Open WS Error");
                errorBox.open();
                return;
            }
            if (wsToLoad != null) {
                WorkspaceLoader wsLoader = new WorkspaceLoader(wsToLoad, new TimeUnitWrapper((long) 30, TimeUnit.MINUTES));
                ContextInjectionFactory.inject(wsLoader, execCtxt);
                Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().execute(wsLoader);
            } else {
                log.error("Error unmarshalling workspace file " + wsFilePath);
            }
        } else {
            log.info("execute no file selected");
        }
    }


}