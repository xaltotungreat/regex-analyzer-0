package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IKeyedSearchObject<R extends IKeyedSearchResult<O>,O extends ISearchResultObject> 
        extends ISearchObject<R,O>, IKeyedSO {

}
