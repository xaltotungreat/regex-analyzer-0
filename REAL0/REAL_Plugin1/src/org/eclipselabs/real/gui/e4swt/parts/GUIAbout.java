package org.eclipselabs.real.gui.e4swt.parts;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.osgi.framework.Bundle;

public class GUIAbout {
    private static final Logger log = LogManager.getLogger(GUIAbout.class);
    
    private Label lblDeepThought;
    private Label lblImage;
    protected static final String CONTEXT_VARIABLE_NAME = "AboutWindowVisible"; 
    protected Image aboutStarsImage;
    
    @Inject
    MApplication application; 
    
    public GUIAbout() {
    }

    /**
     * Create contents of the view part.
     */
    @PostConstruct
    public void createControls(Composite parent) {
        parent.setLayout(new FormLayout());
        
        lblDeepThought = new Label(parent, SWT.WRAP | SWT.CENTER);
        lblDeepThought.setFont(SWTResourceManager.getFont("Tahoma", 12, SWT.NORMAL));
        FormData fd_lblDeepThought = new FormData();
        fd_lblDeepThought.bottom = new FormAttachment(0, 50);
        fd_lblDeepThought.right = new FormAttachment(100, -5);
        fd_lblDeepThought.top = new FormAttachment(0, 5);
        fd_lblDeepThought.left = new FormAttachment(0, 5);
        lblDeepThought.setLayoutData(fd_lblDeepThought);
        lblDeepThought.setText("The person who created this program dreamt about flying to the stars. But how can one fly when he is burdened with logs? This program sets you free.");
        
        lblImage = new Label(parent, SWT.NONE);
        IPath pt = new Path(IEclipse4Constants.APP_MODEL_IMAGE_ABOUTSTARS);
        Bundle plugBundle = Platform.getBundle(IEclipse4Constants.DEFINING_PLUGIN_NAME);
        if (plugBundle != null) {
            try (InputStream aboutStarsImageIS = FileLocator.openStream(plugBundle, pt, false);) {
                aboutStarsImage = new Image(parent.getDisplay(), aboutStarsImageIS);
                lblImage.setImage(aboutStarsImage);
            } catch (IOException e) {
                log.error("IO Exception", e);
            }
        } else {
            log.error("Null bundle for " + IEclipse4Constants.DEFINING_PLUGIN_NAME);
        }
        
        FormData fd_lblImage = new FormData();
        fd_lblImage.bottom = new FormAttachment(100, -3);
        fd_lblImage.right = new FormAttachment(100, -3);
        fd_lblImage.top = new FormAttachment(0, 55);
        fd_lblImage.left = new FormAttachment(0, 3);
        lblImage.setLayoutData(fd_lblImage);
    }

    @PreDestroy
    public void dispose() {
        application.getContext().modify(CONTEXT_VARIABLE_NAME, false);
        if (aboutStarsImage != null) {
            aboutStarsImage.dispose();
        }
    }

    @Focus
    public void setFocus() {
        // TODO	Set the focus to control
    }
}
