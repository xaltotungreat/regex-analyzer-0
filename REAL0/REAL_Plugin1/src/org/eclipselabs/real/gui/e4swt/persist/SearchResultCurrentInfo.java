package org.eclipselabs.real.gui.e4swt.persist;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.gui.core.util.SearchInfo;

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
    protected Map<ReplaceParamKeyPersist,ReplaceParamPersist<?>> customReplaceTable;

    public SearchResultCurrentInfo() {  }

    public SearchResultCurrentInfo(SearchInfo origInfo) {
        searchID = origInfo.getSearchID();
        searchTime = origInfo.getSearchTime();
        searchObjectName = origInfo.getSearchObjectName();
        searchObjectGroup = new SearchObjectGroupPersist(origInfo.getSearchObjectGroup());// origInfo.getSearchObjectGroup().getString();
        if (origInfo.getSearchObjectTags() != null) {
            searchObjectTags = new HashMap<>(origInfo.getSearchObjectTags());
        } else {
            searchObjectTags = new HashMap<>();
        }
        if (origInfo.getCustomReplaceTable() != null) {
            customReplaceTable = new HashMap<>();
            for (Map.Entry<ReplaceParamKey, IReplaceParam<?>> currEntry : origInfo.getCustomReplaceTable().entrySet()) {
                ReplaceParamPersist<?> rpp = null;
                switch(currEntry.getValue().getType()) {
                case BOOLEAN:
                    rpp = new ReplaceParamPersist<Boolean>((IReplaceParam<Boolean>)currEntry.getValue());
                    break;
                case DATE:
                    //rpp = new DateReplaceParamPersist((IReplaceParam<Calendar>)currEntry.getValue());
                    rpp = new ReplaceParamPersist<Calendar>((IReplaceParam<Calendar>)currEntry.getValue());
                    break;
                case INTEGER:
                    rpp = new ReplaceParamPersist<Integer>((IReplaceParam<Integer>)currEntry.getValue());
                    break;
                case STRING:
                default:
                    rpp = new ReplaceParamPersist<String>((IReplaceParam<String>)currEntry.getValue());
                    break;
                }
                customReplaceTable.put(new ReplaceParamKeyPersist(currEntry.getKey()), rpp);
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

    public Map<ReplaceParamKeyPersist,ReplaceParamPersist<?>> getCustomReplaceTable() {
        return customReplaceTable;
    }

    public void setCustomReplaceTable(Map<ReplaceParamKeyPersist,ReplaceParamPersist<?>> customReplaceTable) {
        this.customReplaceTable = customReplaceTable;
    }

    @Override
    public String toString() {
        return "SearchResultCurrentInfo [searchID=" + searchID
                + ", searchObjectName=" + searchObjectName
                + ", searchObjectGroup=" + searchObjectGroup + "]";
    }

}
