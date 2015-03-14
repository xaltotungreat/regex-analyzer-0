package org.eclipselabs.real.gui.e4swt.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.NamedBookmark;
import org.eclipselabs.real.gui.e4swt.OOIInfo;
import org.eclipselabs.real.gui.e4swt.TrackOOI;

public class GUIOOI implements IEclipse4Constants {

    private static final Logger log = LogManager.getLogger(GUIOOI.class);

    Tree mainOOITree;
    TreeItem globalRoot;
    TreeItem localRoot;
    TreeItem bookmarkRoot;

    @Inject
    EModelService modelService;

    @Inject
    EMenuService menuService;

    @Inject
    ECommandService commandService;

    @Inject
    EHandlerService handlerService;

    @Inject
    MWindow mainWindow;

    @Inject
    UISynchronize uiSynch;

    @Inject
    IEclipseContext partContext;

    TrackOOI tracker;

    /**
     * Create contents of the view part.
     */
    @PostConstruct
    public void createControls(Composite parent) {
        mainOOITree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        mainOOITree.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selItems = ((Tree)e.getSource()).getSelection();
                if ((selItems != null) && (selItems.length > 0)) {
                    partContext.remove(APP_MODEL_COMMAND_PARAM_FIND_TEXT);
                    partContext.remove(APP_MODEL_COMMAND_PARAM_FIND_DIRECTION);
                    partContext.remove(APP_MODEL_COMMAND_PARAM_FIND_WRAP);
                    partContext.remove(APP_MODEL_COMMAND_PARAM_FIND_IS_REGEX);
                    partContext.remove(APP_MODEL_COMMAND_PARAM_BOOKMARK_START);
                    partContext.remove(APP_MODEL_COMMAND_PARAM_BOOKMARK_END);
                    partContext.remove(APP_MODEL_COMMAND_PARAM_SEL_BOOKMARK_UUID);

                    TreeItem selItem = selItems[0];
                    partContext.set(CONTEXT_GUIOOI_SELECTED_ITEM, selItems);
                    if ((selItem.getData(DATA_GLOBAL_OOI_KEY) != null) || (selItem.getData(DATA_LOCAL_OOI_KEY) != null)
                            || (selItem.getData(DATA_GLOBAL_OOI_STRING_KEY) != null)) {
                        partContext.set(APP_MODEL_COMMAND_PARAM_FIND_TEXT, selItems[0].getText());
                        partContext.set(APP_MODEL_COMMAND_PARAM_FIND_DIRECTION, String.valueOf(SWT.DOWN));
                        partContext.set(APP_MODEL_COMMAND_PARAM_FIND_WRAP, String.valueOf(true));
                        partContext.set(APP_MODEL_COMMAND_PARAM_FIND_IS_REGEX, String.valueOf(false));
                    } else if (selItem.getData(DATA_NAMED_BOOKMARK_KEY) != null) {
                        NamedBookmark selBkm = (NamedBookmark)selItem.getData(DATA_NAMED_BOOKMARK_KEY);
                        partContext.set(APP_MODEL_COMMAND_PARAM_BOOKMARK_START, selBkm.getStartPos());
                        partContext.set(APP_MODEL_COMMAND_PARAM_BOOKMARK_END, selBkm.getEndPos());
                        partContext.set(APP_MODEL_COMMAND_PARAM_SEL_BOOKMARK_UUID, selBkm.getId());
                    } else if (selItem.getData(DATA_NAMED_BOOKMARK_STRING_KEY) != null) {
                        TreeItem parentBkm = selItem.getParentItem();
                        if ((parentBkm != null) && (parentBkm.getData(DATA_NAMED_BOOKMARK_KEY) != null)) {
                            NamedBookmark selBkm = (NamedBookmark)parentBkm.getData(DATA_NAMED_BOOKMARK_KEY);
                            partContext.set(APP_MODEL_COMMAND_PARAM_BOOKMARK_START, selBkm.getStartPos());
                            partContext.set(APP_MODEL_COMMAND_PARAM_BOOKMARK_END, selBkm.getEndPos());
                            partContext.set(APP_MODEL_COMMAND_PARAM_SEL_BOOKMARK_UUID, selBkm.getId());
                        }
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                TreeItem[] selItems = ((Tree)e.getSource()).getSelection();
                if ((selItems != null) && (selItems.length > 0)) {
                    TreeItem selItem = selItems[0];
                    if ((selItem.getData(DATA_GLOBAL_OOI_KEY) != null) || (selItem.getData(DATA_LOCAL_OOI_KEY) != null)
                            || (selItem.getData(DATA_GLOBAL_OOI_STRING_KEY) != null)) {
                        Command findInCurrentCommand = commandService.getCommand(APP_MODEL_COMMAND_FIND_IN_CURRENT);
                        ParameterizedCommand commToExecute = new ParameterizedCommand(findInCurrentCommand, new Parameterization[] {});
                        if (handlerService.canExecute(commToExecute)) {
                            handlerService.executeHandler(commToExecute);
                        } else {
                            log.error("Command is disabled for execution " + APP_MODEL_COMMAND_FIND_IN_CURRENT);
                        }
                    } else if ((selItem.getData(DATA_NAMED_BOOKMARK_KEY) != null) || (selItem.getData(DATA_NAMED_BOOKMARK_STRING_KEY) != null)) {
                        Command selectInCurrentCommand = commandService.getCommand(APP_MODEL_COMMAND_SELECT_IN_CURRENT);
                        ParameterizedCommand commToExecute = new ParameterizedCommand(selectInCurrentCommand, new Parameterization[] {});
                        if (handlerService.canExecute(commToExecute)) {
                            handlerService.executeHandler(commToExecute);
                        } else {
                            log.error("Command is disabled for execution " + APP_MODEL_COMMAND_SELECT_IN_CURRENT);
                        }
                    } else {
                        log.error("No default action for this type of selected TreeItem " + selItem.getText());
                    }
                }

                /*TreeItem[] selItems = ((Tree)e.getSource()).getSelection();
                if ((selItems != null) && (selItems.length > 0) && (selItems[0].getData(IEclipse4Constants.DATA_GLOBAL_OOI_KEY) != null)) {
                    mainWindow.getContext().set(IEclipse4Constants.CONTEXT_GUIOOI_SELECTED_ITEM, ((Tree)e.getSource()).getSelection());
                    String textToFind = selItems[0].getText();
                    Command findInCurrentCommand = commandService.getCommand(IEclipse4Constants.APP_MODEL_COMMAND_FIND_IN_CURRENT);
                    IParameter findTextParam;
                    IParameter findDirectionParam;
                    IParameter wrapFindParam;
                    IParameter isRegexParam;
                    try {
                        findTextParam = findInCurrentCommand.getParameter(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_TEXT);
                        findDirectionParam = findInCurrentCommand.getParameter(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_DIRECTION);
                        wrapFindParam = findInCurrentCommand.getParameter(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_WRAP);
                        isRegexParam = findInCurrentCommand.getParameter(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_FIND_IS_REGEX);
                        ParameterizedCommand commToExecute = new ParameterizedCommand(findInCurrentCommand, new Parameterization[] {
                                new Parameterization(findTextParam, textToFind),
                                new Parameterization(findDirectionParam, String.valueOf(SWT.DOWN)),
                                new Parameterization(wrapFindParam, Boolean.toString(true)),
                                new Parameterization(isRegexParam, Boolean.toString(false))});
                        if (handlerService.canExecute(commToExecute)) {
                            handlerService.executeHandler(commToExecute);
                        } else {
                            log.error("Command is disabled for execution " + IEclipse4Constants.APP_MODEL_COMMAND_EXECUTE_SEARCH);
                        }
                    } catch (NotDefinedException e1) {
                        log.error("Cannot execute command", e1);
                    }
                }*/
            }

        });
        menuService.registerContextMenu(mainOOITree, APP_MODEL_POPUP_MENU_GLOBAL_OOI_TREE);

        globalRoot = new TreeItem(mainOOITree, SWT.None);
        globalRoot.setText("Global OOI");
        globalRoot.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.BOLD));
        globalRoot.setExpanded(true);

        localRoot = new TreeItem(mainOOITree, SWT.NONE);
        localRoot.setText("Local OOI");
        localRoot.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.BOLD));
        localRoot.setExpanded(true);

        bookmarkRoot = new TreeItem(mainOOITree, SWT.NONE);
        bookmarkRoot.setText("Bookmarks");
        bookmarkRoot.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.BOLD));
        bookmarkRoot.setExpanded(true);
    }

    /**
     * This method can be run from any thread
     * @param globalOOIList the the map of Global OOI
     */
    public void updateGlobalOOI(final Map<String, GlobalOOIInfo> globalOOIList) {
        uiSynch.asyncExec(new Runnable() {

            @Override
            public void run() {
                if ((globalOOIList != null) && (!globalOOIList.isEmpty())) {
                    for (TreeItem currItem : globalRoot.getItems()) {
                        currItem.dispose();
                    }
                    List<String> globalOOIKeyList = new ArrayList<>(globalOOIList.keySet());
                    Collections.sort(globalOOIKeyList);
                    for (String globObjKey : globalOOIKeyList) {
                        TreeItem objItem = new TreeItem(globalRoot, SWT.NONE);
                        GlobalOOIInfo currGlobalOOIInfo = globalOOIList.get(globObjKey);
                        objItem.setText(currGlobalOOIInfo.getDisplayString());
                        if (currGlobalOOIInfo.getStyle().font == null) {
                            objItem.setFont(SWTResourceManager.getFont("Courier New", 11, SWT.NONE));
                        } else {
                            objItem.setFont(currGlobalOOIInfo.getStyle().font);
                        }
                        if (currGlobalOOIInfo.getStyle().foreground != null) {
                            objItem.setForeground(currGlobalOOIInfo.getStyle().foreground);
                        }
                        if (currGlobalOOIInfo.getStyle().background != null) {
                            objItem.setBackground(currGlobalOOIInfo.getStyle().background);
                        }
                        objItem.setData(DATA_GLOBAL_OOI_KEY, globalOOIList.get(globObjKey));
                        for (String strKey : globalOOIList.get(globObjKey).getStringKeys()) {
                            TreeItem keyItem = new TreeItem(objItem, SWT.NONE);
                            keyItem.setText(strKey);
                            keyItem.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NONE));
                            keyItem.setData(DATA_GLOBAL_OOI_STRING_KEY, strKey);
                        }
                        objItem.setExpanded(true);
                    }
                    globalRoot.setExpanded(true);

                } else {
                    for (TreeItem currItem : globalRoot.getItems()) {
                        currItem.dispose();
                    }
                }
                mainOOITree.redraw();
            }
        });
    }

    /**
     * This method can be run from any thread
     * @param localOOIList the the map of local OOI
     */
    public void updateLocalOOI(final Map<String, OOIInfo> localOOIList) {
        uiSynch.asyncExec(new Runnable() {

            @Override
            public void run() {
                if ((localOOIList != null) && (!localOOIList.isEmpty())) {
                    for (TreeItem currItem : localRoot.getItems()) {
                        currItem.dispose();
                    }
                    List<String> localOOIKeyList = new ArrayList<>(localOOIList.keySet());
                    Collections.sort(localOOIKeyList);
                    for (String localObjKey : localOOIKeyList) {
                        TreeItem objItem = new TreeItem(localRoot, SWT.NONE);
                        OOIInfo currLocalOOIInfo = localOOIList.get(localObjKey);
                        objItem.setText(currLocalOOIInfo.getDisplayString());
                        if (currLocalOOIInfo.getStyle().font == null) {
                            objItem.setFont(SWTResourceManager.getFont("Courier New", 11, SWT.NONE));
                        } else {
                            objItem.setFont(currLocalOOIInfo.getStyle().font);
                        }
                        if (currLocalOOIInfo.getStyle().foreground != null) {
                            objItem.setForeground(currLocalOOIInfo.getStyle().foreground);
                        }
                        if (currLocalOOIInfo.getStyle().background != null) {
                            objItem.setBackground(currLocalOOIInfo.getStyle().background);
                        }
                        objItem.setData(DATA_LOCAL_OOI_KEY, localOOIList.get(localObjKey));
                    }
                    localRoot.setExpanded(true);

                } else {
                    for (TreeItem currItem : localRoot.getItems()) {
                        currItem.dispose();
                    }
                }
                mainOOITree.redraw();
            }
        });
    }

    /**
     * This method can be run from any thread
     * @param localBkm the the map of local bookmarks
     */
    public void updateLocalBookmaks(final List<NamedBookmark> localBkm) {
        uiSynch.asyncExec(new Runnable() {

            @Override
            public void run() {
                if ((localBkm != null) && (!localBkm.isEmpty())) {
                    for (TreeItem currItem : bookmarkRoot.getItems()) {
                        currItem.dispose();
                    }
                    Collections.sort(localBkm, new Comparator<NamedBookmark>() {

                        @Override
                        public int compare(NamedBookmark o1, NamedBookmark o2) {
                            int result = 0;
                            if ((o1 != null) && (o2 != null)) {
                                if ((o1.getBookmarkName() != null) && (o2.getBookmarkName() != null)) {
                                    result = o1.getBookmarkName().compareTo(o2.getBookmarkName());
                                } else if ((o1.getBookmarkName() == null) && (o2.getBookmarkName() != null)) {
                                    result = -1;
                                } else if ((o1.getBookmarkName() != null) && (o2.getBookmarkName() == null)) {
                                    result = 1;
                                }
                            } else if ((o1 == null) && (o2 != null)) {
                                result = -1;
                            } else if ((o1 != null) && (o2 == null)) {
                                result = 1;
                            }
                            return result;
                        }
                    });
                    for (NamedBookmark nmBk : localBkm) {
                        TreeItem objItem = new TreeItem(bookmarkRoot, SWT.NONE);
                        objItem.setFont(SWTResourceManager.getFont("Courier New", 11, SWT.NONE));
                        objItem.setText(nmBk.getBookmarkName());
                        if ((nmBk.getStringKeys() != null) && (!nmBk.getStringKeys().isEmpty())) {
                            for (String strKey : nmBk.getStringKeys()) {
                                TreeItem keyItem = new TreeItem(objItem, SWT.NONE);
                                keyItem.setText(strKey);
                                keyItem.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NONE));
                                keyItem.setData(DATA_NAMED_BOOKMARK_STRING_KEY, strKey);
                            }
                        }
                        objItem.setData(DATA_NAMED_BOOKMARK_KEY, nmBk);
                        objItem.setExpanded(true);
                    }
                    bookmarkRoot.setExpanded(true);
                } else {
                    for (TreeItem currItem : bookmarkRoot.getItems()) {
                        currItem.dispose();
                    }
                }
                mainOOITree.redraw();
            }
        });
    }

    public void addGlobalOOIStringKey(String objName, String newKey) {
        if (globalRoot != null) {
            Map<String, GlobalOOIInfo> globalObjList = (Map<String, GlobalOOIInfo>)mainWindow.getContext().get(CONTEXT_GLOBAL_OOI_LIST);
            for (TreeItem currItem : globalRoot.getItems()) {
                if (currItem.getText().equals(objName)) {
                    TreeItem keyItem = new TreeItem(currItem, SWT.NONE);
                    keyItem.setText(newKey);
                    keyItem.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NONE));
                    keyItem.setData(DATA_GLOBAL_OOI_STRING_KEY, newKey);
                    currItem.setExpanded(true);
                    break;
                }
            }
            mainOOITree.redraw();
            // The global OOI for now always uses the quoted text
            GlobalOOIInfo updateInfo = globalObjList.get(Pattern.quote(objName));
            if (updateInfo != null) {
                updateInfo.addStringKey(newKey);
            }
        } else {
            log.error("Calling addStringKey when the globalRoot is null");
        }
    }

    public void addBookmarkStringKey(UUID objID, String newKey) {
        if (bookmarkRoot != null) {
            for (TreeItem currItem : bookmarkRoot.getItems()) {
                if ((currItem.getData(DATA_NAMED_BOOKMARK_KEY) != null)) {
                    NamedBookmark bkm = (NamedBookmark)currItem.getData(DATA_NAMED_BOOKMARK_KEY);
                    if (bkm.getId().equals(objID)) {
                        bkm.getStringKeys().add(newKey);
                        TreeItem keyItem = new TreeItem(currItem, SWT.NONE);
                        keyItem.setText(newKey);
                        keyItem.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NONE));
                        keyItem.setData(DATA_NAMED_BOOKMARK_STRING_KEY, newKey);
                        currItem.setExpanded(true);
                    }
                }
            }
            mainOOITree.redraw();
        } else {
            log.error("Calling addStringKey when the bookmarkRoot is null");
        }
    }

    public void removeSelectedGlobalOOIStringKey() {
        if (globalRoot != null) {
            TreeItem[] selItems = mainOOITree.getSelection();
            if ((selItems != null) && (selItems.length > 0)
                    && (selItems[0].getData(DATA_GLOBAL_OOI_STRING_KEY) != null)) {
                Map<String, GlobalOOIInfo> globalObjList = (Map<String, GlobalOOIInfo>)mainWindow.getContext().get(CONTEXT_GLOBAL_OOI_LIST);

                TreeItem selKey = selItems[0];
                TreeItem parentGlobObj = selItems[0].getParentItem();
                if (parentGlobObj != null) {
                    GlobalOOIInfo globInfo = globalObjList.get(parentGlobObj.getText());
                    if (globInfo != null) {
                        globInfo.removeStringKey(selKey.getText());
                    }
                    mainOOITree.setSelection(parentGlobObj);
                    selKey.dispose();
                    mainOOITree.redraw();
                } else {
                    log.error("Global OOI is null for " + selKey.getText());
                }
            }
        } else {
            log.error("Calling addStringKey when the globalRoot is null");
        }
    }

    public void removeSelectedBookmarkStringKey() {
        if (bookmarkRoot != null) {
            TreeItem[] selItems = mainOOITree.getSelection();
            if ((selItems != null) && (selItems.length > 0)
                    && (selItems[0].getData(DATA_NAMED_BOOKMARK_STRING_KEY) != null)) {
                TreeItem parentBkmItem = selItems[0].getParentItem();
                if ((parentBkmItem != null) && (parentBkmItem.getData(DATA_NAMED_BOOKMARK_KEY) != null)) {
                    NamedBookmark parentBkm = (NamedBookmark)parentBkmItem.getData(DATA_NAMED_BOOKMARK_KEY);
                    if ((parentBkm.getStringKeys() != null) && (parentBkm.getStringKeys().contains(selItems[0].getData(DATA_NAMED_BOOKMARK_STRING_KEY)))) {
                        parentBkm.getStringKeys().remove(selItems[0].getData(DATA_NAMED_BOOKMARK_STRING_KEY));
                    }
                    selItems[0].dispose();
                    mainOOITree.redraw();
                } else {
                    log.warn("Parent Bookmark not found for string key " + selItems[0].getText());
                }
            }
        } else {
            log.error("Calling addStringKey when the bookmarkRoot is null");
        }
    }

    /*@PreDestroy
    public void dispose() {
    }*/

    @Focus
    public void setFocus() {
        if (tracker == null) {
            tracker = new TrackOOI(APP_MODEL_PART_GLOBAL_OOI_TREE);
            mainWindow.getContext().runAndTrack(tracker);
        }
    }

}
