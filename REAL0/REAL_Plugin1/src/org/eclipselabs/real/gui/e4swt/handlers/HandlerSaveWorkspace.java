package org.eclipselabs.real.gui.e4swt.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.NamedBookmark;
import org.eclipselabs.real.gui.e4swt.OOIInfo;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult.SearchResultActiveState;
import org.eclipselabs.real.gui.e4swt.persist.GlobalOOIPersist;
import org.eclipselabs.real.gui.e4swt.persist.NamedBookmarkPersist;
import org.eclipselabs.real.gui.e4swt.persist.OOIPersist;
import org.eclipselabs.real.gui.e4swt.persist.PersistUtil;
import org.eclipselabs.real.gui.e4swt.persist.SavedWorkspace;
import org.eclipselabs.real.gui.e4swt.persist.SearchResultCurrentInfo;
import org.eclipselabs.real.gui.e4swt.persist.SearchResultPartInfo;
import org.eclipselabs.real.gui.e4swt.persist.SearchResultPartStackInfo;

public class HandlerSaveWorkspace {
    private static final Logger log = LogManager.getLogger(HandlerSaveWorkspace.class);

    @Inject
    EModelService modelService;

    @Execute
    public void execute(MWindow mainWindow, IEclipseContext execCtxt) {
        FileDialog fd = new FileDialog((Shell) mainWindow.getContext().get(IServiceConstants.ACTIVE_SHELL), SWT.SAVE);
        String[] filterExt = new String[] {"*.xml"};
        fd.setFilterExtensions(filterExt);
        String wsFilePath = fd.open();
        File wsFile = null;
        int confirmOverwrite = SWT.OK;
        if (wsFilePath != null) {
            wsFile = new File(wsFilePath);
            if (wsFile.exists()) {
                MessageBox confirmOverwriteBox = new MessageBox((Shell) mainWindow.getContext().get(IServiceConstants.ACTIVE_SHELL),
                        SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                confirmOverwriteBox.setText("Confirmaion");
                confirmOverwriteBox.setMessage("The file already exists. Do you want to overwrite?");
                confirmOverwrite = confirmOverwriteBox.open();
            }
        }
        if ((wsFile != null) && (confirmOverwrite == SWT.OK)) {
            log.info("Saving current workspace to " + wsFilePath);
            SavedWorkspace workspaceToSave = new SavedWorkspace();
            workspaceToSave.setSavedTime(new Date(System.currentTimeMillis()));
            // saving the global OOI
            List<GlobalOOIPersist> globOOIPersist = new ArrayList<>();
            Map<String,GlobalOOIInfo> globOOI = (Map<String,GlobalOOIInfo>)execCtxt.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
            if ((globOOI != null) && (!globOOI.isEmpty())) {
                for (GlobalOOIInfo info : globOOI.values()) {
                    GlobalOOIPersist infoPst = new GlobalOOIPersist(info);
                    globOOIPersist.add(infoPst);
                }
                workspaceToSave.setGlobalOOIList(globOOIPersist);
            }

            // saving the search results part stack
            MPartStack srStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, mainWindow);
            if (srStack != null) {
                SearchResultPartStackInfo saveSRPartStackInfo = new SearchResultPartStackInfo();
                List<MPart> savedParts = new ArrayList<>();
                for (MStackElement currSE : srStack.getChildren()) {
                    if ((currSE instanceof MPart) && (((MPart)currSE).isToBeRendered())) {
                        GUISearchResult guiObj = (GUISearchResult)((MPart)currSE).getObject();
                        if ((guiObj != null) && (guiObj.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED)) {
                            savedParts.add((MPart)currSE);
                        }
                    }
                }
                for (MPart srPart : savedParts) {
                    GUISearchResult guiObj = (GUISearchResult)srPart.getObject();
                    SearchResultPartInfo savedPart = new SearchResultPartInfo(guiObj.getMainSearchInfo());
                    savedPart.setPartLabel(srPart.getLabel());
                    savedPart.setPartIconURI(srPart.getIconURI());
                    savedPart.setCaretPos(guiObj.getCaretPosition());
                    int selIndex = guiObj.getSelectedIndex(savedPart.getSearchID());
                    if (selIndex != -1) {
                        savedPart.setSelectedIndex(selIndex);
                    }
                    if ((savedPart.getCurrentSearchInfos() != null) && (!savedPart.getCurrentSearchInfos().isEmpty())) {
                        for (SearchResultCurrentInfo info : savedPart.getCurrentSearchInfos()) {
                            info.setTabTitle(guiObj.getTabTitle(info.getSearchID()));
                            int currSelIndex = guiObj.getSelectedIndex(info.getSearchID());
                            if (currSelIndex != -1) {
                                info.setSelectedIndex(currSelIndex);
                            }
                        }
                    }
                    // saving the local OOI
                    List<OOIPersist> localOOIPersist = null;
                    Map<String,OOIInfo> styleOOI = guiObj.getStyleOOICache();
                    if ((styleOOI != null) && (!styleOOI.isEmpty())) {
                        localOOIPersist = new ArrayList<>();
                        for (OOIInfo info : styleOOI.values()) {
                            boolean containsInGlobal = false;
                            if ((globOOIPersist != null) && (!globOOIPersist.isEmpty())) {
                                for (GlobalOOIPersist globOOIP : globOOIPersist) {
                                    if (globOOIP.getText().equals(info.getTextPattern().pattern())) {
                                        containsInGlobal = true;
                                        break;
                                    }
                                }
                            }
                            if (!containsInGlobal) {
                                localOOIPersist.add(new OOIPersist(info));
                            }
                        }
                    }
                    if (localOOIPersist != null) {
                        savedPart.setLocalOOI(localOOIPersist);
                    }

                    // saving the bookmarks
                    if ((guiObj.getLocalBookmarks() != null) && (!guiObj.getLocalBookmarks().isEmpty())) {
                        List<NamedBookmarkPersist> localBookmarks = new ArrayList<>();
                        for (NamedBookmark nmBkm : guiObj.getLocalBookmarks()) {
                            NamedBookmarkPersist nmBkp = new NamedBookmarkPersist(nmBkm);
                            localBookmarks.add(nmBkp);
                        }
                        savedPart.setLocalBookmarks(localBookmarks);
                    }

                    // completed add the saved part to the list
                    saveSRPartStackInfo.getPartsInfo().add(savedPart);
                }
                workspaceToSave.setSrPartStackInfo(saveSRPartStackInfo);
            }

            // marshalling
            JAXBContext mrshContext;
            try {
                Schema wsSchema = PersistUtil.getSchema(IEclipse4Constants.PATH_WORKSPACE_PERSIST_SCHEMA);
                mrshContext = JAXBContext.newInstance(SavedWorkspace.class);
                Marshaller mrsh = mrshContext.createMarshaller();
                mrsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                mrsh.setSchema(wsSchema);
                mrsh.marshal(workspaceToSave, wsFile);
            } catch (JAXBException e) {
                log.error("Marshalling exception",e);
            }
        } else {
            log.info("execute No file selected");
        }
    }

    @CanExecute
    public boolean canExecute() {
        // TODO Your code goes here
        return true;
    }

}