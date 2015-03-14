package org.eclipselabs.real.gui.e4swt.handlers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.core.sotree.DisplaySOTreeItemType;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTreeItem;
import org.eclipselabs.real.gui.core.util.SearchInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.ConveyorMain;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchObjectTree;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;

public class HandlerSearchInCurrent {

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
            final IDisplaySO dso = (IDisplaySO)selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY);
            req.setDso(dso);
            req.setSearchTabsStack(srStack);
            req.setMaxWaitToComplete(new TimeUnitWrapper((long)30, TimeUnit.MINUTES));
            req.setSearchInCurrent(true);
            ConveyorMain.INSTANCE.submitRequest(req);
        }
    }

    @CanExecute
    public boolean canExecute(MWindow window, IEclipseContext ctxt) {
        Boolean returnValue = false;
        TreeItem[] selItems = (TreeItem[])ctxt.get(IEclipse4Constants.CONTEXT_SEARCH_OBJECT_TREE_SELECTED);
        if ((selItems != null) && (selItems.length > 0)
                && (selItems[0].getData(IEclipse4Constants.DATA_TREE_ELEMENT_STATE_KEY) != null)
                && (selItems[0].getData(IEclipse4Constants.DATA_TREE_ELEMENT_STATE_KEY) == GUISearchObjectTree.TreeElementState.ACTIVE)
                && (selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY) != null)
                && (((IDisplaySOTreeItem)selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY)).getType() == DisplaySOTreeItemType.SEARCH_OBJECT)) {
            IDisplaySOTreeItem selItem = (IDisplaySOTreeItem)selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY);
            if ((selItem != null) && DisplaySOTreeItemType.SEARCH_OBJECT.equals(selItem.getType())) {
                IDisplaySO selDSO = (IDisplaySO)selItem;
                if (SearchObjectType.COMPLEX_REGEX.equals(selDSO.getSearchObject().getType())) {
                    returnValue = true;
                } else if (SearchObjectType.SEARCH_SCRIPT.equals(selDSO.getSearchObject().getType())) {
                    MPartStack srPartStack = (MPartStack) modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, window);
                    if (srPartStack != null) {
                        MPart selPart = (MPart)srPartStack.getSelectedElement();
                        if ((selPart != null) && (selPart.getObject() != null)) {
                            GUISearchResult guiSR = (GUISearchResult)selPart.getObject();
                            SearchInfo mainSI = guiSR.getMainSearchInfo();
                            if (mainSI != null) {
                                List<LogFileTypeKey> guiResultReqLogTypes = mainSI.getRequiredLogTypes();
                                returnValue = guiResultReqLogTypes.containsAll(selDSO.getSearchObject().getRequiredLogTypes());
                            }
                        }
                    }
                }
            }
        }
        MPartStack srPartStack = (MPartStack) modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, window);
        if (srPartStack.getChildren().isEmpty()) {
            returnValue = false;
        }
        return returnValue;
    }

}