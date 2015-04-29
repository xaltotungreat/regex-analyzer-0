package org.eclipselabs.real.gui.e4swt.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipselabs.real.gui.core.sotree.DisplaySOTreeItemType;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTreeItem;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.dialogs.SimpleMessageDialog;

public class HandlerSODescription {

    @Execute
    public void execute(IEclipseContext execCtxt) {
        // check again just in case
        TreeItem[] selItems = (TreeItem[]) execCtxt.get(IEclipse4Constants.CONTEXT_SEARCH_OBJECT_TREE_SELECTED);
        if ((selItems != null) && (selItems.length > 0)
                && (selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY) != null)) {
            IDisplaySOTreeItem selItem = (IDisplaySOTreeItem) selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY);
            if ((selItem != null) && DisplaySOTreeItemType.SEARCH_OBJECT.equals(selItem.getType())) {
                IDisplaySO selItemSO = (IDisplaySO) selItem;
                if ((selItemSO.getSearchObject().getSearchObjectDescription() != null) && (!selItemSO.getSearchObject().getSearchObjectDescription().isEmpty())) {
                    SimpleMessageDialog dialogDescr = new SimpleMessageDialog((Shell)execCtxt.get(IServiceConstants.ACTIVE_SHELL));
                    dialogDescr.setCaption("Search Object Description");
                    dialogDescr.setLabelText(selItemSO.getSearchObject().getSearchObjectDescription());
                    dialogDescr.open();
                }
            }
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext ctxt) {
        boolean returnValue = false;
        TreeItem[] selItems = (TreeItem[]) ctxt.get(IEclipse4Constants.CONTEXT_SEARCH_OBJECT_TREE_SELECTED);
        if ((selItems != null) && (selItems.length > 0)
                && (selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY) != null)) {
            IDisplaySOTreeItem selItem = (IDisplaySOTreeItem) selItems[0].getData(IEclipse4Constants.DATA_SEARCH_OBJECT_TREE_KEY);
            if ((selItem != null) && DisplaySOTreeItemType.SEARCH_OBJECT.equals(selItem.getType())) {
                IDisplaySO selItemSO = (IDisplaySO) selItem;
                if ((selItemSO.getSearchObject().getSearchObjectDescription() != null) && (!selItemSO.getSearchObject().getSearchObjectDescription().isEmpty())) {
                    returnValue = true;
                }
            }
        }
        return returnValue;
    }

}