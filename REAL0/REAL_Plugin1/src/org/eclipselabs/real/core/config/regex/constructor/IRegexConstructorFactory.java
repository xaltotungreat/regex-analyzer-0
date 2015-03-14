package org.eclipselabs.real.core.config.regex.constructor;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.w3c.dom.Node;

public interface IRegexConstructorFactory<K> {

    public IReplaceParamConstructor<K> getReplaceParamConstructor();

    public IRegexConstructor<K> getRegexConstructor();

    public IRealRegexConstructor<K> getRealRegexConstructor();

    public IComplexRegexConstructor<K> getComplexRegexConstructor();
    
    public IComplexRegexViewConstructor<K> getComplexRegexViewConstructor();
    
    public IDateTimeSortRequestConstructor<K> getDateTimeSortRequestConstructor();
    
    public IRegexSortRequestConstructor<K> getRegexSortRequestConstructor();
    
    public IRegexComplexSortRequestConstructor<K> getRegexComplexSortRequestConstructor();
    
    public ISearchScriptConstructor<K> getSearchScriptConstructor();
    
    public <V extends IKeyedSearchObject<?, ?>> ISOConstructor<K, V> getSOConstructor(K sourceObj);
    
    public IRefConstructor<Node, ? extends RefKeyedSO<
            ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> getRefConstructor(Node constrNode);
    
    public IRegexAcceptanceConstructor<K> getRegexAcceptanceConstructor();
    
    public IDTIntervalAcceptanceConstructor<Node> getDTIntervalAcceptanceConstructor();
}
