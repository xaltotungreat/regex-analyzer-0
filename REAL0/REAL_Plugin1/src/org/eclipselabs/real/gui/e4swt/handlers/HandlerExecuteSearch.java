package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.core.sotree.DisplaySOTreeItemType;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTreeItem;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.ConveyorMain;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchObjectTree;

public class HandlerExecuteSearch {
    private static final Logger log = LogManager.getLogger(HandlerExecuteSearch.class);

    @Inject
    EModelService modelService;

    @Execute
    public void execute(MWindow window, IEclipseContext ctxt) {
        TreeItem[] selItems = (TreeItem[])ctxt.get(IEclipse4Constants.CONTEXT_SEARCH_OBJECT_TREE_SELECTED);
        if ((selItems != null) && (selItems.length > 0)) {
            // create the request and fill in injectable fields
            ConvSearchRequest req = ContextInjectionFactory.make(ConvSearchRequest.class, ctxt);
            // maximize the stack with search results if it is the first search
            MPartStack srStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, window);
            if (!srStack.getTags().contains(IPresentationEngine.MAXIMIZED) && srStack.getChildren().isEmpty()) {
                srStack.getTags().add(IPresentationEngine.MAXIMIZED);
            }
            final IDisplaySO dso = (IDisplaySO)selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY);
            req.setDso(dso);
            req.setSearchTabsStack(srStack);
            req.setMaxWaitToComplete(new TimeUnitWrapper((long)30, TimeUnit.MINUTES));
            ConveyorMain.INSTANCE.submitRequest(req);
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext ctxt) {
        boolean returnValue = false;
        TreeItem[] selItems = (TreeItem[]) ctxt.get(IEclipse4Constants.CONTEXT_SEARCH_OBJECT_TREE_SELECTED);
        if ((selItems != null) && (selItems.length > 0) && (selItems[0].getData(IEclipse4Constants.DATA_TREE_ELEMENT_STATE_KEY) != null)
                && (selItems[0].getData(IEclipse4Constants.DATA_TREE_ELEMENT_STATE_KEY) == GUISearchObjectTree.TreeElementState.ACTIVE)
                && (selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY) != null)
                && (((IDisplaySOTreeItem) selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY)).getType() == DisplaySOTreeItemType.SEARCH_OBJECT)) {
            IDisplaySOTreeItem selItem = (IDisplaySOTreeItem) selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY);
            if ((selItem != null) && DisplaySOTreeItemType.SEARCH_OBJECT.equals(selItem.getType())) {
                returnValue = true;
            }
        } else {
            log.info("Command not executed for items " + Arrays.toString(selItems));
        }
        return returnValue;
    }

}