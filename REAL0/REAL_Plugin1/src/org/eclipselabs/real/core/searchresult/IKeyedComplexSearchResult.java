package org.eclipselabs.real.core.searchresult;

import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IKeyedComplexSearchResult<O extends IComplexSearchResultObject<W,X,Q>,
            W extends ISearchResult<X>, X extends ISearchResultObject,Q> 
        extends IComplexSearchResult<O, W, X, Q>, IKeyedSearchResult<O> {

}
