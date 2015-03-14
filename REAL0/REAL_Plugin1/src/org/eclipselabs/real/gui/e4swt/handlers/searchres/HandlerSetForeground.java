package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.gui.e4swt.Eclipse4GUIBridge;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.dialogs.ProgressStatusDialog;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;
import org.eclipselabs.real.gui.e4swt.util.FutureProgressMonitor;

public class HandlerSetForeground {
    private static final Logger log = LogManager.getLogger(HandlerSetForeground.class);

    @Inject
    UISynchronize uiSynch;

    @Execute
    public void execute(final IEclipseContext ctxt, @Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
        MPart activePart = ctxt.get(MPart.class);
        if (activePart != null) {
            log.info("Part in context " + activePart.getElementId());
            final GUISearchResult srObj = (GUISearchResult) activePart.getObject();
            String selectedText = (String) activePart.getContext().get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED);
            if ((selectedText == null) || ("".equals(selectedText))) {
                MessageBox noTextSelected = new MessageBox((Shell)ctxt.get(IServiceConstants.ACTIVE_SHELL),
                        SWT.ICON_ERROR | SWT.OK);
                noTextSelected.setText("Error");
                noTextSelected.setMessage("No text has been selected. Please select text to set foreground color");
                noTextSelected.open();
                return;
            }
            ColorDialog cd = new ColorDialog(parent);
            RGB selColorRGB = cd.open();
            if (selColorRGB != null) {
                final Color selColor = SWTResourceManager.getColor(selColorRGB.red, selColorRGB.green, selColorRGB.blue);
                final FutureProgressMonitor<Integer> fpm = srObj.setStyleForPattern(Pattern.compile(Pattern.quote(selectedText)), new TextStyle(null,selColor,null),true);
                final ProgressStatusDialog foregroundProgressDialog = ContextInjectionFactory.make(ProgressStatusDialog.class, ctxt);
                foregroundProgressDialog.init(fpm.getTotalProgress(), false);
                foregroundProgressDialog.open();
                foregroundProgressDialog.setStatus("Setting foreground color");
                final int guiUpdate = PerformanceUtils.getIntProperty(IEclipse4Constants.PERF_CONST_GUI_UPDATE_INTERVAL,
                        IEclipse4Constants.PERF_CONST_GUI_UPDATE_INTERVAL_DEFAULT);
                Runnable checkProgress = new Runnable() {

                    @Override
                    public void run() {
                        while (!fpm.getFuture().isDone()) {
                            try {
                                Thread.sleep(guiUpdate);
                            } catch (InterruptedException e) {
                                log.error("execute Sleep interrupted",e);
                            }
                            uiSynch.syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    foregroundProgressDialog.setProgress(fpm.getCurrentProgress());
                                }

                            });
                        }
                        uiSynch.syncExec(new Runnable() {
                            @Override
                            public void run() {
                                foregroundProgressDialog.setProgress(fpm.getCurrentProgress());
                                foregroundProgressDialog.setStatus("Complete");
                                foregroundProgressDialog.getShell().dispose();
                                try {
                                    MessageBox selectedDialog = new MessageBox((Shell)ctxt.get(IServiceConstants.ACTIVE_SHELL), SWT.ICON_INFORMATION | SWT.OK);
                                    selectedDialog.setText("Set foreground color");
                                    selectedDialog.setMessage("Selected " + fpm.getFuture().get() + " objects with R="
                                            + selColor.getRed() + " G=" + selColor.getGreen() + " B=" + selColor.getBlue());
                                    selectedDialog.open();
                                } catch (InterruptedException | ExecutionException e) {
                                    log.error("run ",e);
                                }
                            }
                        });
                    }
                };
                Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().execute(checkProgress);
            }
        } else {
            log.error("execute activePart in the context is null");
        }
    }

    @CanExecute
    public boolean canExecute(MPart execPart) {
        boolean canAddToGlobaList = false;
        if (execPart != null) {
            String selectedText = (String)execPart.getContext().get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED);
            if ((selectedText != null) && (!"".equals(selectedText))) {
                canAddToGlobaList = true;
            }
        }
        return canAddToGlobaList;
    }
}