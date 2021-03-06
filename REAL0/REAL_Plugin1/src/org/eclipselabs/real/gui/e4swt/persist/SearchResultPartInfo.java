package org.eclipselabs.real.gui.e4swt.persist;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.gui.core.util.SearchInfo;

/**
 * This class stores information about one part. it is one search
 * in all log files of an aggregate.
 * This class still uses Calendar and was not moved to LocalDateTime.
 * The reason for this is that LocalDateTime (as of 2015-04-29) doesn't have
 * a no-arg constructor and JAXB doesn't work with java.time types.
 *
 * As soon as JAXB is modified to handle java.time types this Calendar is to be replaced
 * with LocalDateTime.
 *
 * @author Vadim Korkin
 *
 */
@XmlType(propOrder={"searchID","searchTime","searchObjectName","searchObjectGroup","partLabel","partIconURI",
        "caretPos","selectedIndex", "searchObjectTags","customReplaceTable","currentSearchInfos",
        "localOOI","localBookmarks"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResultPartInfo {

    protected String searchID;
    protected Calendar searchTime;
    protected String searchObjectName;
    protected SearchObjectGroupPersist searchObjectGroup;
    protected String partLabel;
    protected String partIconURI;
    protected Integer caretPos;
    protected Integer selectedIndex;
    @XmlElementWrapper(nillable=true)
    protected Map<String,String> searchObjectTags;
    @XmlElementWrapper(nillable=true)
    protected Map<ReplaceableParamKeyPersist,ReplaceableParamPersist<?>> customReplaceTable;
    @XmlElementWrapper(nillable=true)
    protected List<SearchResultCurrentInfo> currentSearchInfos;
    @XmlElementWrapper(nillable=true)
    protected List<OOIPersist> localOOI;
    @XmlElementWrapper(nillable=true)
    protected List<NamedBookmarkPersist> localBookmarks;

    public SearchResultPartInfo() {  }

    public SearchResultPartInfo(SearchInfo origInfo) {
        searchID = origInfo.getSearchID();
        searchTime = Calendar.getInstance();
        searchTime.set(origInfo.getSearchTime().getYear(), origInfo.getSearchTime().getMonthValue(),
                origInfo.getSearchTime().getDayOfMonth(), origInfo.getSearchTime().getHour(),
                origInfo.getSearchTime().getMinute(), origInfo.getSearchTime().getSecond());
        searchObjectName = origInfo.getSearchObjectName();
        searchObjectGroup = new SearchObjectGroupPersist(origInfo.getSearchObjectGroup());
        if (origInfo.getSearchObjectTags() != null) {
            searchObjectTags = new HashMap<>(origInfo.getSearchObjectTags());
        } else {
            searchObjectTags = new HashMap<>();
        }
        if (origInfo.getCustomReplaceTable() != null) {
            customReplaceTable = new HashMap<>();
            for (Map.Entry<ReplaceableParamKey, IReplaceableParam<?>> currEntry : origInfo.getCustomReplaceTable().entrySet()) {
                ReplaceableParamPersist<?> rpp = null;
                switch(currEntry.getValue().getType()) {
                case BOOLEAN:
                    rpp = new ReplaceableParamPersist<Boolean>((IReplaceableParam<Boolean>)currEntry.getValue());
                    break;
                case DATE:
                    //rpp = new DateReplaceParamPersist((IReplaceParam<Calendar>)currEntry.getValue());
                    /*
                     * If store the Calendar Type the loaded type is XMLGregorianCalndar
                     * It doesn't contain milliseconds. Therefore a String with mililseconds
                     * is stored instead.
                     */
                    rpp = new ReplaceableParamPersist<String>((IReplaceableParam<LocalDateTime>)currEntry.getValue());
                    break;
                case INTEGER:
                    rpp = new ReplaceableParamPersist<Integer>((IReplaceableParam<Integer>)currEntry.getValue());
                    break;
                case STRING:
                default:
                    rpp = new ReplaceableParamPersist<String>((IReplaceableParam<String>)currEntry.getValue());
                    break;
                }
                customReplaceTable.put(new ReplaceableParamKeyPersist(currEntry.getKey()), rpp);
            }
        } else {
            customReplaceTable = new HashMap<>();
        }
        if ((origInfo.getChildren() != null) && !origInfo.getChildren().isEmpty()) {
            currentSearchInfos = new ArrayList<>();
            for (SearchInfo childInfo : origInfo.getChildren()) {
                SearchResultCurrentInfo childPart = new SearchResultCurrentInfo(childInfo);
                currentSearchInfos.add(childPart);
            }
        }
    }

    public SearchResultCurrentInfo getCurrentInfo(String searchIDChild) {
        SearchResultCurrentInfo res = null;
        if ((currentSearchInfos != null) && (!currentSearchInfos.isEmpty())) {
            for (SearchResultCurrentInfo info : currentSearchInfos) {
                if (info.getSearchID().equals(searchIDChild)) {
                    res = info;
                    break;
                }
            }
        }
        return res;
    }

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    public Calendar getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(Calendar searchTime) {
        this.searchTime = searchTime;
    }

    public String getSearchObjectName() {
        return searchObjectName;
    }

    public void setSearchObjectName(String searchObjectName) {
        this.searchObjectName = searchObjectName;
    }

    public SearchObjectGroupPersist getSearchObjectGroup() {
        return searchObjectGroup;
    }

    public void setSearchObjectGroup(SearchObjectGroupPersist searchObjectGroup) {
        this.searchObjectGroup = searchObjectGroup;
    }

    public String getPartLabel() {
        return partLabel;
    }

    public void setPartLabel(String partLabel) {
        this.partLabel = partLabel;
    }

    public String getPartIconURI() {
        return partIconURI;
    }

    public void setPartIconURI(String partIconURI) {
        this.partIconURI = partIconURI;
    }

    public Integer getCaretPos() {
        return caretPos;
    }

    public void setCaretPos(Integer caretPos) {
        this.caretPos = caretPos;
    }

    public Integer getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public Map<String, String> getSearchObjectTags() {
        return searchObjectTags;
    }

    public void setSearchObjectTags(Map<String, String> searchObjectTags) {
        this.searchObjectTags = searchObjectTags;
    }

    public Map<ReplaceableParamKeyPersist,ReplaceableParamPersist<?>> getCustomReplaceTable() {
        return customReplaceTable;
    }

    public void setCustomReplaceTable(Map<ReplaceableParamKeyPersist,ReplaceableParamPersist<?>> customReplaceTable) {
        this.customReplaceTable = customReplaceTable;
    }

    public List<SearchResultCurrentInfo> getCurrentSearchInfos() {
        return currentSearchInfos;
    }

    public void setCurrentSearchInfos(
            List<SearchResultCurrentInfo> currentSearchInfos) {
        this.currentSearchInfos = currentSearchInfos;
    }

    public List<OOIPersist> getLocalOOI() {
        return localOOI;
    }

    public void setLocalOOI(List<OOIPersist> localOOI) {
        this.localOOI = localOOI;
    }

    public List<NamedBookmarkPersist> getLocalBookmarks() {
        return localBookmarks;
    }

    public void setLocalBookmarks(List<NamedBookmarkPersist> localBookmarks) {
        this.localBookmarks = localBookmarks;
    }

    @Override
    public String toString() {
        return "SearchResultPartInfo [searchID=" + searchID + ", searchObjectName=" + searchObjectName + ", searchObjectGroup=" + searchObjectGroup + ", partLabel=" + partLabel + ", caretPos="
                + caretPos + ", selectedIndex=" + selectedIndex + ", searchObjectTags=" + searchObjectTags + ", dynamicReplaceParams=" + customReplaceTable + ", currentSearchInfos="
                + currentSearchInfos + ", localOOI=" + localOOI + ", localBookmarks=" + localBookmarks + "]";
    }


}
