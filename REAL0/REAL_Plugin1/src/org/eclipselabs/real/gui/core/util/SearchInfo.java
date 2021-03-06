package org.eclipselabs.real.gui.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.IRealCoreConstants;
import org.eclipselabs.real.gui.core.GuiSearchObjectHelper;

public class SearchInfo implements Cloneable {

    private static final Logger log = LogManager.getLogger(SearchInfo.class);
    protected String searchID;
    protected LocalDateTime searchTime;
    protected SearchObjectType searchObjType;
    protected String searchObjectName;
    protected ISearchObjectGroup<String> searchObjectGroup;
    protected Map<String,String> searchObjectTags;
    protected Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceTable;
    protected Integer foundObjects;
    protected Map<String,String> customProgressKeys;
    protected List<SearchInfo> children = new ArrayList<>();

    private DateTimeFormatter toStr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", IRealCoreConstants.DEFAULT_DATE_LOCALE);

    public SearchInfo() {

    }

    public SearchInfo(LocalDateTime aTime) {
        searchTime = aTime;

    }

    public List<LogFileTypeKey> getRequiredLogTypes() {
        List<LogFileTypeKey> lftList = new ArrayList<>();
        IKeyedSearchObject<? extends IKeyedSearchResult<? extends ISearchResultObject>,? extends ISearchResultObject> so
            = GuiSearchObjectHelper.getSearchObject(searchObjectName, searchObjectGroup, searchObjectTags);
        if ((so != null) && (so.getRequiredLogTypes() != null)) {
            for (LogFileTypeKey key : so.getRequiredLogTypes()) {
                try {
                    lftList.add(key.clone());
                } catch (CloneNotSupportedException e) {
                    log.error("Clone not supported?",e);
                }
            }
        } else {
            if (so == null) {
                log.error("getRequiredLogTypes so is null");
            } else {
                log.error("getRequiredLogTypes so.getRequiredLogTypes is null");
            }
        }
        return lftList;
    }

    @Override
    public SearchInfo clone() throws CloneNotSupportedException {
        SearchInfo newInstance = (SearchInfo)super.clone();
        newInstance.setSearchObjectTags((getSearchObjectTags() != null)?new HashMap<>(getSearchObjectTags()):null);
        newInstance.setFoundObjects(getFoundObjects());
        newInstance.setCustomProgressKeys((getCustomProgressKeys() != null)?new HashMap<>(getCustomProgressKeys()):null);
        newInstance.setCustomReplaceTable((getCustomReplaceTable() != null)?new HashMap<>(getCustomReplaceTable()):null);
        return newInstance;
    }

    public void setParamsFromSO(IKeyedSearchObject<?, ?> so) {
        searchObjectName = so.getSearchObjectName();
        try {
            searchObjectGroup = so.getSearchObjectGroup().clone();
        } catch (CloneNotSupportedException e) {
            log.error("setParamsFromSO",e);
        }
        if (so.getSearchObjectTags() != null) {
            searchObjectTags = new HashMap<>(so.getSearchObjectTags());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((searchID == null) ? 0 : searchID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchInfo other = (SearchInfo) obj;
        if (searchID == null) {
            if (other.searchID != null) {
                return false;
            }
        } else if (!searchID.equals(other.searchID)) {
            return false;
        }
        return true;
    }

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    public SearchObjectType getSearchObjType() {
        return searchObjType;
    }

    public void setSearchObjType(SearchObjectType searchObjType) {
        this.searchObjType = searchObjType;
    }

    public String getSearchObjectName() {
        return searchObjectName;
    }

    public void setSearchObjectName(String searchObjectName) {
        this.searchObjectName = searchObjectName;
    }

    public ISearchObjectGroup<String> getSearchObjectGroup() {
        return searchObjectGroup;
    }

    public void setSearchObjectGroup(ISearchObjectGroup<String> searchObjectGroup) {
        this.searchObjectGroup = searchObjectGroup;
    }

    public Map<String, String> getSearchObjectTags() {
        return searchObjectTags;
    }

    public void setSearchObjectTags(Map<String, String> searchObjectTags) {
        this.searchObjectTags = searchObjectTags;
    }

    public Map<ReplaceableParamKey, IReplaceableParam<?>> getCustomReplaceTable() {
        return customReplaceTable;
    }

    public void setCustomReplaceTable(Map<ReplaceableParamKey, IReplaceableParam<?>> cachedReplaceTable) {
        this.customReplaceTable = cachedReplaceTable;
    }

    public Integer getFoundObjects() {
        return foundObjects;
    }

    public void setFoundObjects(Integer foundObjects) {
        this.foundObjects = foundObjects;
    }

    public Map<String,String> getCustomProgressKeys() {
        return customProgressKeys;
    }

    public void setCustomProgressKeys(Map<String,String> customProgressKeys) {
        this.customProgressKeys = customProgressKeys;
    }

    public LocalDateTime getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(LocalDateTime searchTime) {
        this.searchTime = searchTime;
    }

    public List<SearchInfo> getChildren() {
        return children;
    }

    public void setChildren(List<SearchInfo> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "SearchInfo [searchID=" + searchID + ", searchTime="
                + toStr.format(searchTime) + ", searchObjectName=" + searchObjectName
                + ", searchObjectGroup=" + searchObjectGroup + "]";
    }

}
