package org.eclipselabs.real.gui.core;

import java.util.Map;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public class GuiSearchObjectHelper {

    private GuiSearchObjectHelper() {
        // TODO Auto-generated constructor stub
    }
    
    public static IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> 
            getSearchObject(String aName, ISearchObjectGroup<String> aGroup, Map<String,String> tags) {
        SearchObjectKey newKey = new SearchObjectKey(aName, aGroup, tags);
        return SearchObjectController.INSTANCE.getSearchObjectRepository().get(newKey);
    }

}
