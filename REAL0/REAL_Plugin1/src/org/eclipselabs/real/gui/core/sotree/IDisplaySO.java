package org.eclipselabs.real.gui.core.sotree;

import java.util.List;
import java.util.Map;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.gui.core.sort.SortRequestKey;

public interface IDisplaySO extends IDisplaySOTreeItem {
    public IKeyedSearchObject<?,?> getSearchObject();
    public SearchObjectType getSearchObjectType();
    
    public String getTextViewName();
    public void setTextViewName(String textViewName);
    
    public List<String> getViewNamePatterns();
    public void setViewNamePatterns(List<String> viewNamePatterns);
    public void addViewNamePattern(String aViewPattern);
    
    public List<SortRequestKey> getSortRequestKeys();
    public void setSortRequestKeys(List<SortRequestKey> sortRequestKeys);
    
    public String getDisplayName();
    public void setDisplayName(String newDisplayName);
    public String getDescription();
    public void setDescription(String newLongName);
    public Map<String,Object> getGuiProperties();
    public void setGuiProperties(Map<String,Object> guiProperties);
    public String getIconPath(String identifier);
}
