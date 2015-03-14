package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IKeyedComplexSearchObject<R extends IKeyedComplexSearchResult<O,W,X,Q>,
                O extends IComplexSearchResultObject<W,X,Q>,
                V extends ISearchObject<W,X>,W extends ISearchResult<X>, 
                X extends ISearchResultObject,Q> 
        extends IKeyedSearchObject<R, O>, IComplexSearchObject<R, O, V, W, X, Q> {

}
