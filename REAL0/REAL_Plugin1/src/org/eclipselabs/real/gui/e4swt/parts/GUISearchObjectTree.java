package org.eclipselabs.real.gui.e4swt.parts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.core.event.logfile.LogFileTypeStateChangedEvent;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypeState;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.logtype.LogTypeActivation;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.util.ITypedObject;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.core.GUIConfigController;
import org.eclipselabs.real.gui.core.GUIConfigKey;
import org.eclipselabs.real.gui.core.GUIConfigObjectType;
import org.eclipselabs.real.gui.core.sotree.DisplaySOTreeItemType;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOConstants;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOFolder;
import org.eclipselabs.real.gui.core.sotree.IDisplaySORoot;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOSelector;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTemplate;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTemplateAbstract;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTreeItem;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.osgi.framework.Bundle;

import com.google.common.eventbus.Subscribe;

public class GUISearchObjectTree implements IEclipse4Constants {

    private static final Logger log = LogManager.getLogger(GUISearchObjectTree.class);
    @Inject
    MWindow mainWindow;

    @Inject
    ECommandService commandService;

    @Inject
    EHandlerService handlerService;

    @Inject
    EMenuService menuService;

    @Inject
    EModelService modelService;

    @Inject
    UISynchronize uiSynch;

    protected Tree treeSearchObjects;
    protected Map<String,Image> cleanImageMap = new HashMap<>();

    public static final TimeUnitWrapper TREE_WRITE_TIMEOUT = new TimeUnitWrapper((long)10, TimeUnit.SECONDS);
    public static final TimeUnitWrapper TREE_READ_TIMEOUT = new TimeUnitWrapper((long)10, TimeUnit.SECONDS);

    protected Map<DisplaySOTreeItemType,Map<TreeElementState,TreeElementStyle>> styleMap = new HashMap<>();

    public enum TreeElementState {
        ACTIVE,
        UNAVAILABLE,
        DISABLED;
    }

    public static class TreeElementStyle {
        protected Color bkgrColor;
        protected Color frgrColor;
        protected Font font;

        public TreeElementStyle(Color aBkgr, Color aFrgr, Font aFont) {
            bkgrColor = aBkgr;
            frgrColor = aFrgr;
            font = aFont;
        }

        public Color getBkgrColor() {
            return bkgrColor;
        }

        public void setBkgrColor(Color bkgrColor) {
            this.bkgrColor = bkgrColor;
        }

        public Color getFrgrColor() {
            return frgrColor;
        }

        public void setFrgrColor(Color frgrColor) {
            this.frgrColor = frgrColor;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }
    }

    public static class LogTypeStateWrapper {
        LogFileTypeState logTypeState;

        public LogTypeStateWrapper(){

        }

        public LogTypeStateWrapper(LogFileTypeState state) {
            logTypeState = state;
        }

        public LogFileTypeState getLogTypeState() {
            return logTypeState;
        }

        public void setLogTypeState(LogFileTypeState logTypeState) {
            this.logTypeState = logTypeState;
        }
    }

    public GUISearchObjectTree() {

    }

    /**
     * Create contents of the view part.
     */
    @PostConstruct
    public void createControls(Composite parent, final IEclipseContext ctxt) {
        log.debug("createControls start");
        treeSearchObjects = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        menuService.registerContextMenu(treeSearchObjects, APP_MODEL_POPUP_MENU_SEARCH_TREE);
        treeSearchObjects.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selItems = treeSearchObjects.getSelection();
                if ((selItems != null) && (selItems.length > 0)) {
                    mainWindow.getContext().set(CONTEXT_SEARCH_OBJECT_TREE_SELECTED, treeSearchObjects.getSelection());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                Command dsoCommand = commandService.getCommand(APP_MODEL_COMMAND_EXECUTE_SEARCH);
                ParameterizedCommand commToExecute = new ParameterizedCommand(dsoCommand, new Parameterization[] {});
                if (handlerService.canExecute(commToExecute)) {
                    handlerService.executeHandler(new ParameterizedCommand(dsoCommand, new Parameterization[] {}));
                } else {
                    log.error("Command is disabled for execution " + APP_MODEL_COMMAND_EXECUTE_SEARCH);
                }
            }
        });
        mainWindow.getContext().set(CONTEXT_SEARCH_OBJECT_TREE_SELECTED, treeSearchObjects.getSelection());
        fillInStyleMap(treeSearchObjects.getBackground(), treeSearchObjects.getForeground(), treeSearchObjects.getFont());
        TreeItem topRoot = new TreeItem(treeSearchObjects, SWT.SINGLE);
        topRoot.setText("Root");
        setTreeItemStyle(topRoot, styleMap.get(DisplaySOTreeItemType.ROOT).get(TreeElementState.ACTIVE));
        GUIConfigKey searchTreeKey = new GUIConfigKey(GUIConfigObjectType.SEARCH_OBJECT_TREE);
        fillInSOTree((DefaultMutableTreeNode)GUIConfigController.INSTANCE.getGUIObjectRepository().get(searchTreeKey),
                    topRoot);
        topRoot.setExpanded(true);
        CoreEventBus.INSTANCE.register(this);
    }

    protected void fillInStyleMap(Color mainBkgr, Color mainFrgr, Font mainFont) {
        Map<TreeElementState,TreeElementStyle> tmpMap = new HashMap<>();
        tmpMap.put(TreeElementState.ACTIVE, new TreeElementStyle(mainBkgr, mainFrgr, mainFont));
        tmpMap.put(TreeElementState.UNAVAILABLE, new TreeElementStyle(mainBkgr, mainFrgr, mainFont));
        tmpMap.put(TreeElementState.DISABLED, new TreeElementStyle(mainBkgr, mainFrgr, mainFont));
        styleMap.put(DisplaySOTreeItemType.ROOT, tmpMap);

        tmpMap.clear();
        tmpMap.put(TreeElementState.ACTIVE, new TreeElementStyle(mainBkgr, mainFrgr, SWTResourceManager.getFont("Segoe UI", 11, SWT.ITALIC)));
        tmpMap.put(TreeElementState.UNAVAILABLE, new TreeElementStyle(mainBkgr, mainFrgr, SWTResourceManager.getFont("Segoe UI", 11, SWT.ITALIC)));
        tmpMap.put(TreeElementState.DISABLED, new TreeElementStyle(mainBkgr, mainFrgr, SWTResourceManager.getFont("Segoe UI", 11, SWT.ITALIC)));
        styleMap.put(DisplaySOTreeItemType.FOLDER, tmpMap);

        tmpMap.clear();
        tmpMap.put(TreeElementState.ACTIVE, new TreeElementStyle(mainBkgr, mainFrgr, SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD)));
        tmpMap.put(TreeElementState.UNAVAILABLE, new TreeElementStyle(SWTResourceManager.getColor(56,91,131), mainFrgr, SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD)));
        tmpMap.put(TreeElementState.DISABLED, new TreeElementStyle(SWTResourceManager.getColor(133,133,133), mainFrgr, SWTResourceManager.getFont("Segoe UI", 10, SWT.BOLD)));
        styleMap.put(DisplaySOTreeItemType.TEMPLATE, tmpMap);

        tmpMap.clear();
        tmpMap.put(TreeElementState.ACTIVE, new TreeElementStyle(mainBkgr, mainFrgr, SWTResourceManager.getFont("Segoe UI", 9, SWT.NONE)));
        tmpMap.put(TreeElementState.UNAVAILABLE, new TreeElementStyle(SWTResourceManager.getColor(56,91,131), mainFrgr, SWTResourceManager.getFont("Segoe UI", 9, SWT.NONE)));
        tmpMap.put(TreeElementState.DISABLED, new TreeElementStyle(SWTResourceManager.getColor(133,133,133), mainFrgr, SWTResourceManager.getFont("Segoe UI", 9, SWT.NONE)));
        styleMap.put(DisplaySOTreeItemType.SEARCH_OBJECT, tmpMap);
    }

    protected void setTreeItemStyle(TreeItem treeItem, TreeElementStyle tiStyle) {
        treeItem.setBackground(tiStyle.getBkgrColor());
        treeItem.setForeground(tiStyle.getFrgrColor());
        treeItem.setFont(tiStyle.getFont());
    }

    public void fillInSOTree(DefaultMutableTreeNode configTree, TreeItem parentItem) {
        ITypedObject<DisplaySOTreeItemType> userObj = (ITypedObject<DisplaySOTreeItemType>)configTree.getUserObject();
        if (DisplaySOTreeItemType.ROOT.equals(userObj.getType())) {
            IDisplaySORoot rootData = (IDisplaySORoot)userObj;
            parentItem.setData(DATA_SEARCH_OBJECT_TREE_KEY, rootData);
            parentItem.setData(DATA_TREE_ELEMENT_STATE_KEY, TreeElementState.ACTIVE);
            Enumeration chl = configTree.children();
            while(chl.hasMoreElements()) {
                DefaultMutableTreeNode currChild = (DefaultMutableTreeNode)chl.nextElement();
                fillInSOTree(currChild, parentItem);
            }
        } else if (DisplaySOTreeItemType.FOLDER.equals(userObj.getType())) {
            IDisplaySOFolder fld = (IDisplaySOFolder)userObj;
            TreeItem ti = new TreeItem(parentItem, SWT.NONE);
            ti.setData(DATA_SEARCH_OBJECT_TREE_KEY, fld);
            ti.setData(DATA_TREE_ELEMENT_STATE_KEY, TreeElementState.ACTIVE);
            ti.setText(fld.getName());
            setTreeItemStyle(ti, styleMap.get(DisplaySOTreeItemType.FOLDER).get(TreeElementState.ACTIVE));
            Enumeration chl = configTree.children();
            while(chl.hasMoreElements()) {
                DefaultMutableTreeNode currChild = (DefaultMutableTreeNode)chl.nextElement();
                fillInSOTree(currChild, ti);
            }
            ti.setExpanded(fld.getExpanded());
        } else if (DisplaySOTreeItemType.TEMPLATE.equals(userObj.getType())) {
            IDisplaySOTemplate soRef = (IDisplaySOTemplate)userObj;
            TreeItem tiRef = new TreeItem(parentItem, SWT.NONE);
            tiRef.setData(DATA_SEARCH_OBJECT_TREE_KEY, soRef);
            tiRef.setText(soRef.getName());

            if (soRef.getSelectorList() != null) {
                for (IDisplaySOTemplateAbstract currTemplateAbstract : soRef.getSelectorList()) {
                    if (DisplaySOTreeItemType.SELECTOR.equals(currTemplateAbstract.getType())) {
                        constructSelectorTreeItems((IDisplaySOSelector)currTemplateAbstract, tiRef);
                    } else if (DisplaySOTreeItemType.TEMPLATE.equals(currTemplateAbstract.getType())) {
                        Enumeration chl = configTree.children();
                        while(chl.hasMoreElements()) {
                            DefaultMutableTreeNode currChild = (DefaultMutableTreeNode)chl.nextElement();
                            if ((currChild.getUserObject() != null) && (currChild.getUserObject().equals(currTemplateAbstract))) {
                                fillInSOTree(currChild, tiRef);
                            }
                        }
                    }
                }
            }
            TreeElementState tiRefState = getTreeElementState(tiRef);
            tiRef.setData(DATA_TREE_ELEMENT_STATE_KEY, tiRefState);
            setTreeItemStyle(tiRef, styleMap.get(DisplaySOTreeItemType.TEMPLATE).get(tiRefState));
            tiRef.setExpanded(soRef.getExpanded());
        }
    }

    protected void constructSelectorTreeItems(IDisplaySOSelector selector, TreeItem parentItem) {
        List<IDisplaySO> soList = selector.getSearchTreeItems(SearchObjectController.INSTANCE.getSearchObjectRepository().getAllValues());
        Collections.sort(soList, new Comparator<IDisplaySO>() {

            @Override
            public int compare(IDisplaySO o1, IDisplaySO o2) {
                int returnVal = 0;
                if ((o1 != null) && (o2 != null)) {
                    returnVal = o1.getName().compareTo(o2.getName());
                } else if ((o1 != null) && (o2 == null)) {
                    returnVal = 1;
                } else if ((o1 == null) && (o2 != null)) {
                    returnVal = -1;
                } else {
                    returnVal = 0;
                }
                return returnVal;
            }
        });
        Bundle plugBundle = Platform.getBundle(DEFINING_PLUGIN_NAME);
        if (plugBundle == null) {
            log.error("constructSelectorTreeItems bundle not found " + DEFINING_PLUGIN_NAME);
            return;
        }

        Image treeIcon = null;
        for (IDisplaySO currDSO : soList) {
            treeIcon = null;
            TreeItem ti = new TreeItem(parentItem, SWT.NONE);
            ti.setData(DATA_SEARCH_OBJECT_TREE_KEY, currDSO);
            Set<LogFileTypeKey> soLogTypes = currDSO.getSearchObject().getRequiredLogTypes();
            Map<LogFileTypeKey,LogTypeStateWrapper> reqTypesMap = new HashMap<>();
            TreeElementStyle currElemStyle = null;
            TreeElementState currElemState = null;
            if (soLogTypes != null) {
                boolean lftEnabled = true;
                for (LogFileTypeKey lftKey : soLogTypes) {
                    LogFileType currType = LogFileTypes.INSTANCE.getLogFileType(lftKey.getLogTypeName());
                    if (currType != null) {
                        reqTypesMap.put(lftKey, new LogTypeStateWrapper(currType.getStateInfoCopy()));
                        if (!currType.isEnabled()) {
                            lftEnabled = false;
                        }
                    } else {
                        log.error("constructSelectorTreeItems LogFileType not found for key " + lftKey);
                    }
                }
                currElemState = lftEnabled?TreeElementState.UNAVAILABLE:TreeElementState.DISABLED;
                currElemStyle = styleMap.get(DisplaySOTreeItemType.SEARCH_OBJECT).get(currElemState);
            } else {
                currElemState = TreeElementState.ACTIVE;
                currElemStyle = styleMap.get(DisplaySOTreeItemType.SEARCH_OBJECT).get(currElemState);
            }
            ti.setData(DATA_TREE_ELEMENT_STATE_KEY, currElemState);
            ti.setData(DATA_REQ_LOG_TYPES_SET, reqTypesMap);
            ti.setText(currDSO.getName());
            // TODO add a tooltip for every display search object
            setTreeItemStyle(ti, currElemStyle);
            try {
                IPath pt;
                String iconPath = currDSO.getIconPath(IDisplaySOConstants.ICON_PROPERTY_TREE);
                if (IDisplaySOConstants.ICON_PROPERTY_TREE != null) {
                    if (cleanImageMap.containsKey(iconPath)) {
                        treeIcon = cleanImageMap.get(iconPath);
                    } else {
                        pt = new Path(iconPath);
                        try (InputStream treeImageIS = FileLocator.openStream(plugBundle, pt, false)) {
                            if (treeImageIS != null) {
                                treeIcon = new Image(parentItem.getDisplay(), treeImageIS);
                                cleanImageMap.put(iconPath, treeIcon);
                            }
                        }
                    }
                }
                if (treeIcon != null) {
                    ti.setImage(treeIcon);
                }
            } catch (IOException e) {
                log.error("constructSelectorTreeItems IO Exception", e);
            }
        }
    }

    protected void updateTreeElementStyles(TreeItem updateItem, LogFileTypeKey updateKey,
            LogFileTypeState newGlobalState) {
        /*log.debug("updateTreeElementStyles item=" + ((updateItem != null)?updateItem.getText():"null")
                + " LogFileType=" + ((updateKey != null)?updateKey.getLogTypeName():"null")
                + " newState=" + newState);*/
        if (updateItem != null) {
            if (updateItem.getItemCount() > 0) {
                TreeItem[] updateChildren = updateItem.getItems();
                for (TreeItem updateChild : updateChildren) {
                    updateTreeElementStyles(updateChild, updateKey, newGlobalState);
                }
            }
            IDisplaySOTreeItem currItem = (IDisplaySOTreeItem)updateItem.getData(DATA_SEARCH_OBJECT_TREE_KEY);
            switch(currItem.getType()) {
                case ROOT:
                case FOLDER:
                    break;
                case TEMPLATE:
                    TreeElementState newRefState = getTreeElementState(updateItem);
                    boolean refExpanded = (newRefState == TreeElementState.ACTIVE)?true:false;
                    setTreeItemStyle(updateItem, styleMap.get(DisplaySOTreeItemType.TEMPLATE).get(newRefState));
                    updateItem.setData(DATA_TREE_ELEMENT_STATE_KEY, newRefState);
                    updateItem.setExpanded(refExpanded);
                    break;
                case SEARCH_OBJECT:
                    Map<LogFileTypeKey,LogTypeStateWrapper> statesMap = (Map<LogFileTypeKey,LogTypeStateWrapper>)updateItem.getData(DATA_REQ_LOG_TYPES_SET);
                    if ((statesMap != null) && (!statesMap.isEmpty())) {
                        if (statesMap.containsKey(updateKey)) {
                            LogTypeStateWrapper wrap = statesMap.get(updateKey);
                            if (newGlobalState != null) {
                                wrap.setLogTypeState(newGlobalState);
                            }
                        }
                        TreeElementState newState = getTreeElementState(statesMap);
                        setTreeItemStyle(updateItem, styleMap.get(DisplaySOTreeItemType.SEARCH_OBJECT).get(newState));
                        updateItem.setData(DATA_TREE_ELEMENT_STATE_KEY, newState);
                    } else {
                        log.error("updateTreeElementStyles logFileState map is empty or null elemName=" + updateItem.getText() + " map=" + statesMap);
                    }
                    break;
                default:
                    log.error("updateTreeElementStyles");
                    break;
            }
        } else {
            TreeItem[] rootItems = treeSearchObjects.getItems();
            for (TreeItem rootItem : rootItems) {
                updateTreeElementStyles(rootItem, updateKey, newGlobalState);
            }
        }
    }

    protected TreeElementState getTreeElementState(TreeItem parentItem) {
        TreeElementState tiRefState = TreeElementState.DISABLED;
        if (parentItem.getItemCount() > 0) {
            TreeItem[] updateChildren = parentItem.getItems();
            for (TreeItem updateChild : updateChildren) {
                if (updateChild.getData(DATA_TREE_ELEMENT_STATE_KEY) != null) {
                    TreeElementState updateChildObj = (TreeElementState) updateChild.getData(DATA_TREE_ELEMENT_STATE_KEY);
                    if (updateChildObj == TreeElementState.UNAVAILABLE) {
                        tiRefState = TreeElementState.UNAVAILABLE;
                    } else if (updateChildObj == TreeElementState.ACTIVE) {
                        tiRefState = TreeElementState.ACTIVE;
                        break;
                    }
                }
            }
        }
        return tiRefState;
    }

    protected TreeElementState getTreeElementState(Map<LogFileTypeKey,LogTypeStateWrapper> lftStateMap) {
        TreeElementState newState = TreeElementState.ACTIVE;
        for (Map.Entry<LogFileTypeKey,LogTypeStateWrapper> stateEntry : lftStateMap.entrySet()) {
            if (!stateEntry.getValue().getLogTypeState().isEnabled()) {
                newState = TreeElementState.DISABLED;
            } else if (!stateEntry.getValue().getLogTypeState().isAvailable()) {
                newState = TreeElementState.UNAVAILABLE;
            }
        }
        return newState;
    }

    @Subscribe
    public void logFileTypeStateChange(final LogFileTypeStateChangedEvent stateChangedEvent) {
        log.debug("State change event for " + stateChangedEvent.getLogFileTypeKey().getLogTypeName()
                + " old=" + stateChangedEvent.getOldState() + " new=" + stateChangedEvent.getNewState());
        if (!stateChangedEvent.getOldState().equals(stateChangedEvent.getNewState())) {
            uiSynch.syncExec(new Runnable() {

                @Override
                public void run() {
                    updateTreeElementStyles(null, stateChangedEvent.getLogFileTypeKey(), stateChangedEvent.getNewState());
                    treeSearchObjects.redraw();
                }
            });
        } else {
            log.warn("logAggregateFilesCountChange No Change for " + stateChangedEvent.getLogFileTypeKey().getLogTypeName()
                + " old=" + stateChangedEvent.getOldState() + " new=" + stateChangedEvent.getNewState());
        }
    }

    /*@Subscribe
    public void controllerFolderListChange(ControllerFolderListUpdated fldListEvent) {
        log.debug("Log Folder List change event oldSize=" + ((fldListEvent.getOldFolders()!=null)?fldListEvent.getOldFolders().size():"null")
                + " newSize=" + ((fldListEvent.getNewFolders()!=null)?fldListEvent.getNewFolders().size():"null"));
        if ((fldListEvent.getOldFolders() != null) && (fldListEvent.getNewFolders() != null)) {
            if ((fldListEvent.getOldFolders().size() == 0) && (fldListEvent.getNewFolders().size() != 0)) {
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        updateTreeElementStyles(null, null, fldListEvent.getNewFolders().size(), TreeElementState.ACTIVE);
                        treeSearchObjects.redraw();
                    }
                });
            } else if ((fldListEvent.getOldFolders().size() != 0) && (fldListEvent.getNewFolders().size() == 0)) {
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        updateTreeElementStyles(null, null, TreeElementState.DISABLED);
                        treeSearchObjects.redraw();
                    }
                });
            } else {
                log.error("controllerFolderListChange No Change old=" + fldListEvent.getOldFolders().size()
                        + " new=" + fldListEvent.getNewFolders().size());
            }
        }
    }*/

    @PreDestroy
    public void dispose() {
        for (Map.Entry<String, Image> imageEntry : cleanImageMap.entrySet()) {
            imageEntry.getValue().dispose();
        }
        log.info("dispose Writing log types activation");
        // writing active log types before exit
        // do not use schema as the feature is new
        JAXBContext mrshContext;
        Bundle plugin = Platform.getBundle (IEclipse4Constants.DEFINING_PLUGIN_NAME);
        URL url = plugin.getEntry ("/");

        File activeLogTypeConfig = null;
        try {
            URL resolvedURL = FileLocator.resolve(url);
            activeLogTypeConfig = new File (resolvedURL.getFile() + File.separator + "config" + File.separator + "active_config.xml");
            //Schema wsSchema = PersistUtil.getSchema(IEclipse4Constants.PATH_WORKSPACE_PERSIST_SCHEMA);
            mrshContext = JAXBContext.newInstance(LogTypeActivation.class);
            Marshaller mrsh = mrshContext.createMarshaller();
            mrsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //mrsh.setSchema(wsSchema);
            mrsh.marshal(LogTypeActivation.loadFromList(LogFileTypes.INSTANCE.getAllTypes()), activeLogTypeConfig);
        } catch (JAXBException e) {
            log.error("dispose Marshalling exception",e);
        } catch (IOException e) {
            log.error("dispose ", e);
        }
    }

    @Focus
    public void setFocus() {
        treeSearchObjects.setFocus();
    }
}
