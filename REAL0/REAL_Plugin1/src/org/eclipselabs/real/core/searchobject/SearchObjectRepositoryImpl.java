package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.KeyedObjectRepositoryImpl;

public class SearchObjectRepositoryImpl extends KeyedObjectRepositoryImpl<SearchObjectKey, 
            IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> implements ISearchObjectRepository {

}
