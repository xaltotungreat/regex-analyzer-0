package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.IKeyedObjectRepository;

public interface ISearchObjectRepository extends IKeyedObjectRepository<SearchObjectKey, 
            IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>{

}
