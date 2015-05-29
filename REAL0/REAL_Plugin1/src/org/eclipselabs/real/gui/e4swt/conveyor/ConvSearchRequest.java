package org.eclipselabs.real.gui.e4swt.conveyor;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.e4swt.NamedBookmark;
import org.eclipselabs.real.gui.e4swt.OOIInfo;
import org.eclipselabs.real.gui.e4swt.conveyor.stage.build.RequestInformationLevel;

/**
 * This is the main request class. It contains all the necessary information
 * to begin a search.
 *
 * @author Vadim Korkin
 *
 */
@Creatable
public class ConvSearchRequest {

    private RequestInformationLevel infoLevel = RequestInformationLevel.STANDARD;

    private String oldSearchID;

    private IDisplaySO dso;

    private MPartStack searchTabsStack;

    private boolean searchInCurrent = false;

    private TimeUnitWrapper maxWaitToComplete;

    // params injected from the context
    @Inject
    private IEclipseContext handlerContext;

    @Inject
    private UISynchronize uiSynch;

    @Inject
    private EModelService modelService;

    @Inject
    protected EPartService partService;

    @Inject
    private MApplication application;

    // custom parameters for the GUI
    private String partLabel = null;
    private String tabTitle = null;
    private String iconURI = null;

    private List<OOIInfo> localOOIList;

    private List<NamedBookmark> namedBookmarkList;

    private Integer caretPosition;

    private Integer selectedIndex;

    private boolean installGOSilently = false;

    private Map<ReplaceParamKey,IReplaceParam<?>> preparedParams;

    @Inject
    public ConvSearchRequest() {

    }

    public ConvSearchRequest(IDisplaySO displaySO, MPartStack tabs, TimeUnitWrapper maxWait) {
        dso = displaySO;
        searchTabsStack = tabs;
        maxWaitToComplete = maxWait;
    }

    public RequestInformationLevel getInfoLevel() {
        return infoLevel;
    }

    public void setInfoLevel(RequestInformationLevel infoLevel) {
        this.infoLevel = infoLevel;
    }

    public String getOldSearchID() {
        return oldSearchID;
    }

    public void setOldSearchID(String oldSearchID) {
        this.oldSearchID = oldSearchID;
    }

    public IDisplaySO getDso() {
        return dso;
    }

    public void setDso(IDisplaySO dso) {
        this.dso = dso;
    }

    public boolean isSearchInCurrent() {
        return searchInCurrent;
    }

    public void setSearchInCurrent(boolean searchInCurrent) {
        this.searchInCurrent = searchInCurrent;
    }

    public TimeUnitWrapper getMaxWaitToComplete() {
        return maxWaitToComplete;
    }

    public void setMaxWaitToComplete(TimeUnitWrapper maxWaitToComplete) {
        this.maxWaitToComplete = maxWaitToComplete;
    }

    public IEclipseContext getHandlerContext() {
        return handlerContext;
    }

    public void setHandlerContext(IEclipseContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    public UISynchronize getUiSynch() {
        return uiSynch;
    }

    public void setUiSynch(UISynchronize uiSynch) {
        this.uiSynch = uiSynch;
    }

    public EModelService getModelService() {
        return modelService;
    }

    public void setModelService(EModelService modelService) {
        this.modelService = modelService;
    }

    public EPartService getPartService() {
        return partService;
    }

    public void setPartService(EPartService partService) {
        this.partService = partService;
    }

    public MPartStack getSearchTabsStack() {
        return searchTabsStack;
    }

    public void setSearchTabsStack(MPartStack searchTabsStack) {
        this.searchTabsStack = searchTabsStack;
    }

    public MApplication getApplication() {
        return application;
    }

    public void setApplication(MApplication application) {
        this.application = application;
    }

    public String getPartLabel() {
        return partLabel;
    }

    public void setPartLabel(String partLabel) {
        this.partLabel = partLabel;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public String getIconURI() {
        return iconURI;
    }

    public void setIconURI(String iconURI) {
        this.iconURI = iconURI;
    }

    public boolean isInstallGOSilently() {
        return installGOSilently;
    }

    public void setInstallGOSilently(boolean installGOSilently) {
        this.installGOSilently = installGOSilently;
    }

    public Map<ReplaceParamKey, IReplaceParam<?>> getPreparedParams() {
        return preparedParams;
    }

    public void setPreparedParams(Map<ReplaceParamKey, IReplaceParam<?>> preparedParams) {
        this.preparedParams = preparedParams;
    }

    public List<OOIInfo> getLocalOoiList() {
        return localOOIList;
    }

    public void setLocalOoiList(List<OOIInfo> ooiList) {
        this.localOOIList = ooiList;
    }

    public List<NamedBookmark> getNamedBookmarkList() {
        return namedBookmarkList;
    }

    public void setNamedBookmarkList(List<NamedBookmark> namedBookmarkList) {
        this.namedBookmarkList = namedBookmarkList;
    }

    public Integer getCaretPosition() {
        return caretPosition;
    }

    public void setCaretPosition(Integer caretPosition) {
        this.caretPosition = caretPosition;
    }

    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex = selectedIndex;
    }


}
