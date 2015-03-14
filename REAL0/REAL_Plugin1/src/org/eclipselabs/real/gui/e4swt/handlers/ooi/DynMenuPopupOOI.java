package org.eclipselabs.real.gui.e4swt.handlers.ooi;

import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.IGuiStrings;

public class DynMenuPopupOOI implements IEclipse4Constants, IGuiStrings {
    @AboutToShow
    public void aboutToShow(List<MMenuElement> items, IEclipseContext ctxt, EModelService modelService) {
        MPart activePart = ctxt.get(MPart.class);
        if (activePart.getObject() != null) {
            TreeItem[] selItems = (TreeItem[])ctxt.get(CONTEXT_GUIOOI_SELECTED_ITEM);
            if ((selItems != null) && (selItems.length > 0)) {
                TreeItem selItem = selItems[0];
                if (selItem.getData(DATA_GLOBAL_OOI_KEY) != null) {
                    MHandledMenuItem hmiAddStrKey = modelService.createModelElement(MHandledMenuItem.class);
                    hmiAddStrKey.setLabel(GUI_MI_ADD_STRING_KEY);
                    MCommand addStrKeyCmd = MCommandsFactory.INSTANCE.createCommand();
                    addStrKeyCmd.setElementId(IEclipse4Constants.APP_MODEL_COMMAND_ADD_STRING_KEY);
                    addStrKeyCmd.setCommandName("DMC.Add Str Key");
                    hmiAddStrKey.setCommand(addStrKeyCmd);
                    items.add(hmiAddStrKey);
                    
                    MHandledMenuItem hmiRemoveGlobOOI = modelService.createModelElement(MHandledMenuItem.class);
                    hmiRemoveGlobOOI.setLabel(GUI_MI_REMOVE_FROM_GLOBAL_OOI);
                    MCommand removeGlobOOICmd = MCommandsFactory.INSTANCE.createCommand();
                    removeGlobOOICmd.setElementId(IEclipse4Constants.APP_MODEL_COMMAND_REMOVE_FROM_GLOBAL_OOI);
                    removeGlobOOICmd.setCommandName("DMC.Rem From Glob OOI");
                    hmiRemoveGlobOOI.setCommand(removeGlobOOICmd);
                    items.add(hmiRemoveGlobOOI);
                } 
                if ((selItem.getData(DATA_GLOBAL_OOI_STRING_KEY) != null) || (selItem.getData(DATA_NAMED_BOOKMARK_STRING_KEY) != null)) {
                    MHandledMenuItem hmiRemoveStrKey = modelService.createModelElement(MHandledMenuItem.class);
                    hmiRemoveStrKey.setLabel(GUI_MI_REMOVE_STRING_KEY);
                    MCommand removeStringKeyCommand = MCommandsFactory.INSTANCE.createCommand();
                    removeStringKeyCommand.setElementId(IEclipse4Constants.APP_MODEL_COMMAND_REMOVE_STRING_KEY);
                    removeStringKeyCommand.setCommandName("DMC.Rem Str Key");
                    hmiRemoveStrKey.setCommand(removeStringKeyCommand);
                    items.add(hmiRemoveStrKey);
                   
                }
                if (selItem.getData(DATA_LOCAL_OOI_KEY) != null) {
                    MHandledMenuItem hmiRemoveLOOI = modelService.createModelElement(MHandledMenuItem.class);
                    hmiRemoveLOOI.setLabel(GUI_MI_REMOVE_FROM_LOCAL_OOI);
                    MCommand removeLOOICmd = MCommandsFactory.INSTANCE.createCommand();
                    removeLOOICmd.setElementId(IEclipse4Constants.APP_MODEL_COMMAND_REMOVE_FROM_LOCAL_OOI);
                    removeLOOICmd.setCommandName("DMC.Rem local OOI");
                    hmiRemoveLOOI.setCommand(removeLOOICmd);
                    items.add(hmiRemoveLOOI);
                }
                if (selItem.getData(DATA_NAMED_BOOKMARK_KEY) != null) {
                    MHandledMenuItem hmiAddStrKey = modelService.createModelElement(MHandledMenuItem.class);
                    hmiAddStrKey.setLabel(GUI_MI_ADD_STRING_KEY);
                    MCommand addStrKeyCmd = MCommandsFactory.INSTANCE.createCommand();
                    addStrKeyCmd.setElementId(IEclipse4Constants.APP_MODEL_COMMAND_ADD_STRING_KEY);
                    addStrKeyCmd.setCommandName("DMC.Add Str Key");
                    hmiAddStrKey.setCommand(addStrKeyCmd);
                    items.add(hmiAddStrKey);
                    
                    MHandledMenuItem hmiRemoveBkm = modelService.createModelElement(MHandledMenuItem.class);
                    hmiRemoveBkm.setLabel(GUI_MI_REMOVE_BOOKMARK);
                    MCommand removeBkmCmd = MCommandsFactory.INSTANCE.createCommand();
                    removeBkmCmd.setElementId(IEclipse4Constants.APP_MODEL_COMMAND_REMOVE_NAMED_BOOKMARK);
                    removeBkmCmd.setCommandName("DMC.Rem Bkm");
                    hmiRemoveBkm.setCommand(removeBkmCmd);
                    items.add(hmiRemoveBkm);
                }
                
            }
        }
    }

    @AboutToHide
    public void aboutToHide(List<MMenuElement> items) {
        // TODO Your code goes here
    }

}