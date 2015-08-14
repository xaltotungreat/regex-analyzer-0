package org.eclipselabs.real.gui.e4swt.persist;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.gui.core.util.SearchInfo;

/**
 * This class stores information about one Search-in-current i.e. one tab in a part.
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
@XmlType(propOrder={"searchID","searchTime","searchObjectName","searchObjectGroup","tabTitle","selectedIndex",
        "searchObjectTags","customReplaceTable"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResultCurrentInfo {

    protected String searchID;
    protected Calendar searchTime;
    protected String searchObjectName;
    protected SearchObjectGroupPersist searchObjectGroup;
    protected String tabTitle;
    protected Integer selectedIndex;
    @XmlElementWrapper(nillable=true)
    protected Map<String,String> searchObjectTags;
    @XmlElementWrapper(nillable=true)
    protected Map<ReplaceableParamKeyPersist,ReplaceableParamPersist<?>> customReplaceTable;

    public SearchResultCurrentInfo() {  }

    public SearchResultCurrentInfo(SearchInfo origInfo) {
        searchID = origInfo.getSearchID();
        searchTime = Calendar.getInstance();
        searchTime.set(origInfo.getSearchTime().getYear(), origInfo.getSearchTime().getMonthValue(),
                origInfo.getSearchTime().getDayOfMonth(), origInfo.getSearchTime().getHour(),
                origInfo.getSearchTime().getMinute(), origInfo.getSearchTime().getSecond());
        searchObjectName = origInfo.getSearchObjectName();
        searchObjectGroup = new SearchObjectGroupPersist(origInfo.getSearchObjectGroup());// origInfo.getSearchObjectGroup().getString();
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

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
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

    @Override
    public String toString() {
        return "SearchResultCurrentInfo [searchID=" + searchID
                + ", searchObjectName=" + searchObjectName
                + ", searchObjectGroup=" + searchObjectGroup + "]";
    }

}
