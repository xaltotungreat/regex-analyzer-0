package org.eclipselabs.real.gui.e4swt.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.e4swt.E4SwtSearchObjectHelper;
import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.NamedBookmark;
import org.eclipselabs.real.gui.e4swt.OOIHelper;
import org.eclipselabs.real.gui.e4swt.OOIInfo;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchResult;
import org.eclipselabs.real.gui.e4swt.conveyor.ConveyorMain;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.build.RequestInformationLevel;
import org.eclipselabs.real.gui.e4swt.dialogs.ProgressStatusDialog;
import org.eclipselabs.real.gui.e4swt.persist.GlobalOOIPersist;
import org.eclipselabs.real.gui.e4swt.persist.NamedBookmarkPersist;
import org.eclipselabs.real.gui.e4swt.persist.OOIPersist;
import org.eclipselabs.real.gui.e4swt.persist.PersistUtil;
import org.eclipselabs.real.gui.e4swt.persist.SavedWorkspace;
import org.eclipselabs.real.gui.e4swt.persist.SearchResultCurrentInfo;
import org.eclipselabs.real.gui.e4swt.persist.SearchResultPartInfo;

@Creatable
public class WorkspaceLoader implements Runnable {

    private static final Logger log = LogManager.getLogger(WorkspaceLoader.class);

    @Inject
    UISynchronize uiSynch;

    @Inject
    EModelService modelService;

    @Inject
    IEclipseContext execCtxt;

    @Inject
    MWindow mainWindow;

    private SavedWorkspace workspace;

    private TimeUnitWrapper oneRequestWait;

    public static class PartInfoWrapper {
        private ConvSearchRequest mainRequest;
        private List<ConvSearchRequest> childRequests = new ArrayList<ConvSearchRequest>();
        public PartInfoWrapper(ConvSearchRequest mainReq) {
            mainRequest = mainReq;
        }
        public ConvSearchRequest getMainRequest() {
            return mainRequest;
        }
        public void setMainRequest(ConvSearchRequest mainRequest) {
            this.mainRequest = mainRequest;
        }
        public List<ConvSearchRequest> getChildRequests() {
            return childRequests;
        }
        public void setChildRequests(List<ConvSearchRequest> childRequests) {
            this.childRequests = childRequests;
        }
    }

    public WorkspaceLoader() { }

    public WorkspaceLoader(SavedWorkspace ws, TimeUnitWrapper reqWait) {
        workspace = ws;
        oneRequestWait = reqWait;
    }

    public static SavedWorkspace loadFromXML(String wsPath) throws JAXBException {
        File wsFile = new File(wsPath);
        // unmarshalling
        SavedWorkspace wsLoaded = null;
        Schema wsSchema = PersistUtil.getSchema(IEclipse4Constants.PATH_WORKSPACE_PERSIST_SCHEMA);
        JAXBContext unmarshalCtxt = JAXBContext.newInstance(IEclipse4Constants.PACKAGE_WORKSPACE_PERSIST);
        Unmarshaller unm = unmarshalCtxt.createUnmarshaller();
        unm.setSchema(wsSchema);
        wsLoaded = (SavedWorkspace)unm.unmarshal(wsFile);
        return wsLoaded;
    }

    @Override
    public void run() {
        TimeUnitWrapper timeWait = oneRequestWait;
        // 30 minutes by default
        if (timeWait == null) {
            timeWait = new TimeUnitWrapper((long)30, TimeUnit.MINUTES);
        }
        final MPartStack resultsStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, mainWindow);
        if (!resultsStack.getTags().contains(IPresentationEngine.MAXIMIZED) && resultsStack.getChildren().isEmpty()) {
            resultsStack.getTags().add(IPresentationEngine.MAXIMIZED);
        }
        // first load the global OOI. Then they will be automatically installed on new search results
        List<GlobalOOIPersist> globOOIPst = workspace.getGlobalOOIList();
        loadGlobalOOI(globOOIPst, resultsStack, mainWindow, uiSynch);

        // global OOI are handled now the main search
        if ((workspace.getSrPartStackInfo() != null)
                && (workspace.getSrPartStackInfo().getPartsInfo() != null) && (!workspace.getSrPartStackInfo().getPartsInfo().isEmpty())) {
            final List<PartInfoWrapper> searchReqs = getAllSearchRequests(workspace, resultsStack, timeWait, execCtxt);
            if (!searchReqs.isEmpty()) {
                final Set<ProgressStatusDialog> tmpSet = new HashSet<>();
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        ProgressStatusDialog globObjProgressDialog = new ProgressStatusDialog((Shell)mainWindow.getContext().get(IServiceConstants.ACTIVE_SHELL),
                                SWT.BORDER | SWT.CLOSE | SWT.RESIZE, "Loading workspace");
                        globObjProgressDialog.init(searchReqs.size(), false);
                        globObjProgressDialog.open();
                        globObjProgressDialog.setStatus("Initializing");
                        tmpSet.add(globObjProgressDialog);
                    }
                });
                final ProgressStatusDialog wsLoadProgressDialog = tmpSet.iterator().next();
                for (final PartInfoWrapper currPartInfo : searchReqs) {
                    loadSearchObject(currPartInfo, wsLoadProgressDialog);
                }
                // dispose the progress dialog
                uiSynch.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        wsLoadProgressDialog.setStatus("Complete");
                        wsLoadProgressDialog.getShell().dispose();
                    }
                });
            }
        }
     }

    protected List<PartInfoWrapper> getAllSearchRequests(SavedWorkspace wsToLoad, MPartStack resultsStack,
            TimeUnitWrapper timeWait, IEclipseContext execContext) {
        List<PartInfoWrapper> searchReqs = new ArrayList<>();
        for (SearchResultPartInfo partInfo : wsToLoad.getSrPartStackInfo().getPartsInfo()) {
            ISearchObjectGroup<String> soGroup = SearchObjectFactory.getInstance().getSearchObjectGroup();
            if ((partInfo.getSearchObjectGroup() != null) && (partInfo.getSearchObjectGroup().getPathElements() != null)) {
                soGroup.addGroupElements(partInfo.getSearchObjectGroup().getPathElements());
            }
            SearchObjectKey soKey = new SearchObjectKey(partInfo.getSearchObjectName(),
                    soGroup, partInfo.getSearchObjectTags());
            IKeyedSearchObject<?, ?> searchObj = SearchObjectController.INSTANCE.getSearchObjectRepository().get(soKey);
            if (searchObj != null) {
                log.info("HandlerLoadWorkspace found SO for ID " + partInfo.getSearchID() + " name=" + partInfo.getSearchObjectName());
                IDisplaySO displaySearchObj = E4SwtSearchObjectHelper.getDisplaySOForSO(searchObj);
                if (displaySearchObj == null) {
                    log.error("execute Display Search Object not found - omitting. SOKey " + soKey);
                    continue;
                }

                ConvSearchRequest newReq = new ConvSearchRequest(displaySearchObj, resultsStack, timeWait);
                newReq.setOldSearchID(partInfo.getSearchID());
                newReq.setPartLabel(partInfo.getPartLabel());
                newReq.setPreparedParams(PersistUtil.getReplaceMap(partInfo.getCustomReplaceTable()));
                newReq.setCaretPosition(partInfo.getCaretPos());
                newReq.setSelectedIndex(partInfo.getSelectedIndex());
                newReq.setInstallGOSilently(true);
                newReq.setIconURI(partInfo.getPartIconURI());
                newReq.setInfoLevel(RequestInformationLevel.EXTENDED);
                ContextInjectionFactory.inject(newReq, execContext);

                if ((partInfo.getLocalOOI() != null) && (!partInfo.getLocalOOI().isEmpty())) {
                    List<OOIInfo> thisOOIList = getOOI(partInfo.getLocalOOI());
                    if (!thisOOIList.isEmpty()) {
                        newReq.setLocalOoiList(thisOOIList);
                    }
                }

                if ((partInfo.getLocalBookmarks() != null) && (!partInfo.getLocalBookmarks().isEmpty())) {
                    List<NamedBookmark> localBkm = new ArrayList<>();
                    for(NamedBookmarkPersist nmBkP : partInfo.getLocalBookmarks()) {
                        NamedBookmark newBkm = new NamedBookmark(UUID.fromString(nmBkP.getId()), nmBkP.getName(), nmBkP.getStartPos(), nmBkP.getEndPos());
                        if (nmBkP.getStringKeys() != null) {
                            newBkm.getStringKeys().addAll(nmBkP.getStringKeys());
                        }
                        localBkm.add(newBkm);
                    }
                    newReq.setNamedBookmarkList(localBkm);
                }
                PartInfoWrapper allPartInfo = new PartInfoWrapper(newReq);

                if ((partInfo.getCurrentSearchInfos() != null) && (!partInfo.getCurrentSearchInfos().isEmpty())) {
                    List<ConvSearchRequest> childRequests = new ArrayList<>();
                    for (SearchResultCurrentInfo currChildInfo : partInfo.getCurrentSearchInfos()) {
                        ISearchObjectGroup<String> soCurrentGroup = SearchObjectFactory.getInstance().getSearchObjectGroup();
                        if ((currChildInfo.getSearchObjectGroup() != null) && (currChildInfo.getSearchObjectGroup().getPathElements() != null)) {
                            soCurrentGroup.addGroupElements(currChildInfo.getSearchObjectGroup().getPathElements());
                        }
                        SearchObjectKey soKeyCurrent = new SearchObjectKey(currChildInfo.getSearchObjectName(),
                                soCurrentGroup, currChildInfo.getSearchObjectTags());
                        IKeyedSearchObject<?, ?> searchObjCurrent = SearchObjectController.INSTANCE.getSearchObjectRepository().get(soKeyCurrent);
                        if (searchObjCurrent != null) {
                            log.info("HandlerLoadWorkspace found current SO for ID " + currChildInfo.getSearchID() + " name=" + currChildInfo.getSearchObjectName());
                            IDisplaySO displaySearchObjCurrent = E4SwtSearchObjectHelper.getDisplaySOForSO(searchObjCurrent);
                            if (displaySearchObjCurrent == null) {
                                log.error("execute Display Search Object current not found - omitting. SOKey " + soKeyCurrent);
                                continue;
                            }

                            ConvSearchRequest newReqChild = new ConvSearchRequest(displaySearchObjCurrent, resultsStack, timeWait);
                            newReqChild.setOldSearchID(currChildInfo.getSearchID());
                            newReqChild.setTabTitle(currChildInfo.getTabTitle());
                            newReqChild.setPreparedParams(PersistUtil.getReplaceMap(currChildInfo.getCustomReplaceTable()));
                            //newReqChild.setCaretPosition(partInfo.getCaretPos());
                            newReqChild.setSelectedIndex(currChildInfo.getSelectedIndex());
                            newReqChild.setInstallGOSilently(true);
                            newReqChild.setSearchInCurrent(true);
                            newReqChild.setInfoLevel(RequestInformationLevel.EXTENDED);
                            ContextInjectionFactory.inject(newReqChild, execContext);
                            childRequests.add(newReqChild);
                        }
                    }
                    if (!childRequests.isEmpty()) {
                        allPartInfo.setChildRequests(childRequests);
                    }
                }
                searchReqs.add(allPartInfo);
            } else {
                log.error("execute Search Object not found - omitting. SOKey " + soKey);
            }
        }
        return searchReqs;
    }

    /**
     * This method blocks any request main/child to make sure all
     * child searches are performed as searches in the current
     * @param partInfo
     */
    protected void loadSearchObject(final PartInfoWrapper partInfo, ProgressStatusDialog wsLoadProgressDialog) {
        ConvSearchRequest mainReq = partInfo.getMainRequest();
        // update the name of the SO being searched
        uiSynch.asyncExec(new Runnable() {

            @Override
            public void run() {
                wsLoadProgressDialog.setStatus("Loading "
                        + (((mainReq.getPartLabel() != null) && (!mainReq.getPartLabel().isEmpty()))
                            ?mainReq.getPartLabel():mainReq.getDso().getDisplayName()));
            }
        });
        CompletableFuture<ConvSearchResult> mainResultFuture = ConveyorMain.INSTANCE.submitRequest(mainReq);
        try {
            // wait till it is completed because other searches are searches in current
            mainResultFuture.get();
            for (ConvSearchRequest childReq : partInfo.getChildRequests()) {
                // update the name of the SO being searched
                uiSynch.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        wsLoadProgressDialog.setStatus("Loading Tab " + childReq.getDso().getDisplayName());
                    }
                });
                CompletableFuture<ConvSearchResult> currChildFuture = ConveyorMain.INSTANCE.submitRequest(childReq);
                // wait till it is completed it looks better for the user
                // when one search is completed then the next one begins
                currChildFuture.get();
            }
            // increase the progress for the dialog after the part is complete with all tabs
            uiSynch.syncExec(new Runnable() {

                @Override
                public void run() {
                    wsLoadProgressDialog.increaseProgress(1);
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            log.error("loadSearchObject",e);
        }
    }

    protected List<OOIInfo> getOOI(List<OOIPersist> ooiPList) {
        List<OOIInfo> thisOOIList = new ArrayList<>();
        for(OOIPersist ooiP : ooiPList) {
            Font fnt = null;
            if (ooiP.getStyle().getFont() != null) {
                fnt = SWTResourceManager.getFont(ooiP.getStyle().getFont().getFontName(),
                        ooiP.getStyle().getFont().getFontSize(), ooiP.getStyle().getFont().getFontStyle());
            }
            Color frgrColor = null;
            if (ooiP.getStyle().getForeground() != null) {
                frgrColor = SWTResourceManager.getColor(ooiP.getStyle().getForeground().getRed(),
                        ooiP.getStyle().getForeground().getGreen(), ooiP.getStyle().getForeground().getBlue());
            }
            Color bkgrColor = null;
            if (ooiP.getStyle().getBackground() != null) {
                bkgrColor = SWTResourceManager.getColor(ooiP.getStyle().getBackground().getRed(),
                        ooiP.getStyle().getBackground().getGreen(), ooiP.getStyle().getBackground().getBlue());
            }
            OOIInfo newInfo = new OOIInfo(Pattern.compile(ooiP.getText()), new TextStyle(fnt, frgrColor, bkgrColor));
            thisOOIList.add(newInfo);
        }
        return thisOOIList;
    }

    protected void loadGlobalOOI(List<GlobalOOIPersist> globOOIPst, final MPartStack resultsStack, final MWindow mainWindowPar, final UISynchronize uiSynchPar) {
        if ((globOOIPst != null) && (!globOOIPst.isEmpty())) {
            final List<GlobalOOIInfo> newGlobalOOI = new ArrayList<>();
            for (final GlobalOOIPersist pstInfo : globOOIPst) {
                uiSynchPar.syncExec(new Runnable() {
                    @Override
                    public void run() {
                        Font fnt = null;
                        if (pstInfo.getStyle().getFont() != null) {
                            fnt = SWTResourceManager.getFont(pstInfo.getStyle().getFont().getFontName(),
                                pstInfo.getStyle().getFont().getFontSize(), pstInfo.getStyle().getFont().getFontStyle());
                        }
                        Color frgrColor = null;
                        if (pstInfo.getStyle().getForeground() != null) {
                            frgrColor = SWTResourceManager.getColor(pstInfo.getStyle().getForeground().getRed(),
                                pstInfo.getStyle().getForeground().getGreen(), pstInfo.getStyle().getForeground().getBlue());
                        }
                        Color bkgrColor = null;
                        if (pstInfo.getStyle().getBackground() != null) {
                            bkgrColor = SWTResourceManager.getColor(pstInfo.getStyle().getBackground().getRed(),
                                pstInfo.getStyle().getBackground().getGreen(), pstInfo.getStyle().getBackground().getBlue());
                        }
                        GlobalOOIInfo newInfo = new GlobalOOIInfo(Pattern.compile(pstInfo.getText()), new TextStyle(fnt, frgrColor, bkgrColor));
                        newInfo.setStringKeys(pstInfo.getStringKeys());
                        newGlobalOOI.add(newInfo);
                    }
                });
            }
            final CountDownLatch oneTimeLt = new CountDownLatch(1);
            uiSynchPar.syncExec(new Runnable () {

                @Override
                public void run() {
                    CompletableFuture<Void> installGOOI = OOIHelper.installGlobalOOI(newGlobalOOI, mainWindowPar.getContext(), uiSynchPar, resultsStack);
                    installGOOI.handle((Void arg0, Throwable t) ->
                    {
                        oneTimeLt.countDown();
                        if (t != null) {
                            log.error("HandlerLoadWorkspace Exception installing global OOI", arg0);
                        }
                        return null;
                    });
                }
            });
            try {
                oneTimeLt.await();
            } catch (InterruptedException e) {
                log.error("HandlerLoadWorkspace Global OOI latch interrupted",e);
            }
        }
    }

    public SavedWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(SavedWorkspace workspace) {
        this.workspace = workspace;
    }

    public TimeUnitWrapper getOneRequestWait() {
        return oneRequestWait;
    }

    public void setOneRequestWait(TimeUnitWrapper oneRequestWait) {
        this.oneRequestWait = oneRequestWait;
    }
}
