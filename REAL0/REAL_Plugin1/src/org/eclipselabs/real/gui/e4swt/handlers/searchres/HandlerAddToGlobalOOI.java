package org.eclipselabs.real.gui.e4swt.handlers.searchres;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.OOIHelper;
import org.eclipselabs.real.gui.e4swt.dialogs.DialogResult;
import org.eclipselabs.real.gui.e4swt.dialogs.InputTextDialog;

public class HandlerAddToGlobalOOI {
    private static final Logger log = LogManager.getLogger(HandlerAddToGlobalOOI.class);

    @Inject
    EModelService modelService;

    @Inject
    UISynchronize uiSynch;

    @Execute
    public void execute(final MWindow execWindow, MPart execPart,
            @Named(IServiceConstants.ACTIVE_SHELL) Shell parent, IEclipseContext ctxt) {
        Map<String, GlobalOOIInfo> globalObjList = null;
        if (execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) {
            globalObjList = (Map<String, GlobalOOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
        } else {
            globalObjList = new ConcurrentHashMap<>();
            execWindow.getContext().set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST, globalObjList);
            execWindow.getContext().set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, false);
        }
        if (execPart != null) {
            final String selectedText = (String)execPart.getContext().get(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED);
            int userConfirm = SWT.YES;
            if (globalObjList.containsKey(Pattern.quote(selectedText))) {
                MessageBox noTextSelected = new MessageBox((Shell) execWindow.getContext().get(IServiceConstants.ACTIVE_SHELL),
                        SWT.ICON_WARNING | SWT.YES | SWT.NO);
                noTextSelected.setText("Confirmation");
                noTextSelected.setMessage("This object is already a Global OOI. Do you want to overwrite?");
                userConfirm = noTextSelected.open();
            }
            RGB selColorRGB = null;
            if (userConfirm == SWT.YES) {
                ColorDialog cd = new ColorDialog(parent);
                selColorRGB = cd.open();
            }
            if (selColorRGB != null) {
                Map<String, GlobalOOIInfo> globalOOIPendList = null;
                if (execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST) != null) {
                    globalOOIPendList = (Map<String, GlobalOOIInfo>)execWindow.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST);
                } else {
                    globalOOIPendList = new ConcurrentHashMap<>();
                    execWindow.getContext().set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST, globalOOIPendList);
                    execWindow.getContext().set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST_CHANGED, false);
                }
                Color selColor = SWTResourceManager.getColor(selColorRGB.red, selColorRGB.green, selColorRGB.blue);
                String firstStringKey = null;
                InputTextDialog stringKeyDialog = ContextInjectionFactory.make(InputTextDialog.class, ctxt);
                stringKeyDialog.init("Enter String Key");
                DialogResult<String> stringKeyResult = stringKeyDialog.open();
                if (stringKeyResult.getAction() == SWT.OK) {
                    firstStringKey = stringKeyResult.getResult();
                }
                log.info("Adding to Global Objects " + selectedText
                        + " Color R=" + selColor.getRed() + " B=" + selColor.getBlue() + " G=" + selColor.getGreen()
                        + " String Key=" + firstStringKey);
                GlobalOOIInfo info = new GlobalOOIInfo(Pattern.compile(Pattern.quote(selectedText)), new TextStyle(null,null,selColor));
                globalOOIPendList.put(info.getTextPattern().pattern(), info);
                if ((firstStringKey != null) && (!"".equals(firstStringKey))) {
                    info.addStringKey(firstStringKey);
                }
                MPartStack resultsStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, execWindow);
                OOIHelper.installGlobalOOI(info, execWindow.getContext(), uiSynch, resultsStack);
            } else {
                log.warn("No color has been selected. Aborting");
            }
        } else {
            log.warn("The part in which the handler is executing is null");
        }
    }

    @CanExecute
    public boolean canExecute(MWindow execWindow, MPart execPart) {
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