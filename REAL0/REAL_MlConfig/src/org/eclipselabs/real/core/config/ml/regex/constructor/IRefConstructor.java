package org.eclipselabs.real.core.config.ml.regex.constructor;

import org.eclipselabs.real.core.config.ml.IConfigObjectConstructor;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IRefConstructor<K,V extends RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> extends IConfigObjectConstructor<K, V> {

}
