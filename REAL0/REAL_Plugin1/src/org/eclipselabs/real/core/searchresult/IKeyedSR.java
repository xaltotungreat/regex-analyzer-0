package org.eclipselabs.real.core.searchresult;

import java.util.Map;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;

public interface IKeyedSR {

    public ISearchObjectGroup<String> getSearchObjectGroup();
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup);
    public Map<String, String> getSearchObjectTags();
    public void setSearchObjectTags(Map<String, String> soTags);
    
}
