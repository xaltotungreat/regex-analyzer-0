package org.eclipselabs.real.gui.core.sotree;

import java.util.List;
import java.util.Map;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IDisplaySOTemplateAbstract extends IDisplaySOTreeItem {

    public IDisplaySOTemplateAbstract getParent();
    public void setParent(IDisplaySOTemplateAbstract prt);
    public List<IDisplaySO> getSearchTreeItems(List<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> soList);
    public boolean matchesSearchObject(IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> searchObj);
    
    public Map<String,Object> getGuiProperties();
    public void setGuiProperties(Map<String,Object> guiProperties);
}
