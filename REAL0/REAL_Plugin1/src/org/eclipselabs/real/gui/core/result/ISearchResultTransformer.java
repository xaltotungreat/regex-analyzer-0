package org.eclipselabs.real.gui.core.result;

import java.util.List;

import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;

public interface ISearchResultTransformer {

    public <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> 
            IDisplayResult transformTextResult(List<R> searchResults, IDisplaySO displaySearchObject, 
                    TransformIndexMehod indexCalcMethod, Boolean sortValues);
    
    public <R extends IKeyedComplexSearchResult<O, W, X, String>, 
                O extends IComplexSearchResultObject<W, X, String>, 
                V extends ISearchObject<W, X>, W extends ISearchResult<X>, 
                X extends ISearchResultObject> 
            IDisplayResult transformTextResultWithViews(List<R> searchResults, IDisplaySO displaySearchObject, 
                    TransformIndexMehod indexCalcMethod, Boolean sortValues);
}
