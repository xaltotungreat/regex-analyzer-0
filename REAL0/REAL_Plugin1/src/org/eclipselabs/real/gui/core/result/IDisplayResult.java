package org.eclipselabs.real.gui.core.result;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.gui.core.util.SearchInfo;

public interface IDisplayResult {

    public List<String> getText();
    public String getTextConcat();
    public String getTextConcatWithCleanup();
    public Long getTextConcatLength();
    public void addText(String newText);
    
    public SearchInfo getSearchInfo();
    public void setSearchInfo(SearchInfo newInfo);
    
    public String getSearchObjectName();
    public void setSearchObjectName(String newName);
    public ISearchObjectGroup<String> getSearchObjectGroup();
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup);
    public Map<String, String> getSearchObjectTags();
    public void setSearchObjectTags(Map<String, String> soTags);
}
