package org.eclipselabs.real.gui.core.sotree;

import java.util.List;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IDisplaySOTemplate  extends IDisplaySOTemplateAbstract {
    public List<IDisplaySO> getSearchTreeItems(int selectorPos, List<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> soList);

    public List<IDisplaySOTemplateAbstract> getSelectorList();
    public void setSelectorList(List<IDisplaySOTemplateAbstract> selectorList);
    public void addSelector(IDisplaySOTemplateAbstract aSelector);
    
    public IDisplaySO getDisplaySearchObject(IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> searchObj);
}
