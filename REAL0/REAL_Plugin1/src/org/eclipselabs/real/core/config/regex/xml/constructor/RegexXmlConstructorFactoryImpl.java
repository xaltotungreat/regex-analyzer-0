package org.eclipselabs.real.core.config.regex.xml.constructor;

import org.eclipselabs.real.core.config.regex.constructor.IComplexRegexConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IComplexRegexViewConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IDTIntervalAcceptanceConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IDateTimeSortRequestConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRealRegexConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRefConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexAcceptanceConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexComplexSortRequestConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.regex.constructor.IRegexSortRequestConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IReplaceableParamConstructor;
import org.eclipselabs.real.core.config.regex.constructor.ISOConstructor;
import org.eclipselabs.real.core.config.regex.constructor.ISearchScriptConstructor;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.w3c.dom.Node;

public class RegexXmlConstructorFactoryImpl implements IRegexConstructorFactory<Node> {

    private RegexXmlConstructorFactoryImpl() {
        
    }
    
    public static RegexXmlConstructorFactoryImpl getInstance() {
        return new RegexXmlConstructorFactoryImpl();
    }
    
    @Override
    public IReplaceableParamConstructor<Node> getReplaceableParamConstructor() {
        return new ReplaceableParamXmlConstructorImpl();
    }

    @Override
    public IRegexConstructor<Node> getRegexConstructor() {
        return new RegexXmlConstructorImpl();
    }

    @Override
    public IRealRegexConstructor<Node> getRealRegexConstructor() {
        return new RealRegexXmlConstructorImpl();
    }

    @Override
    public IComplexRegexConstructor<Node> getComplexRegexConstructor() {
        return new ComplexRegexXmlConstructorImpl();
    }
    
    @Override
    public IComplexRegexViewConstructor<Node> getComplexRegexViewConstructor() {
        return new ComplexRegexViewXmlConstructorImpl();
    }

    @Override
    public IDateTimeSortRequestConstructor<Node> getDateTimeSortRequestConstructor() {
        return new DateTimeSortRequestXmlConstructor();
    }

    @Override
    public IRegexSortRequestConstructor<Node> getRegexSortRequestConstructor() {
        return new RegexSortRequestXmlConstructor();
    }

    @Override
    public IRegexComplexSortRequestConstructor<Node> getRegexComplexSortRequestConstructor() {
        return new RegexComplexSortRequestXmlConstructor();
    }
    
    @Override
    public ISearchScriptConstructor<Node> getSearchScriptConstructor() {
        return new SearchScriptXmlConstructorImpl();
    }

    @Override
    public <V extends IKeyedSearchObject<?, ?>> ISOConstructor<Node, V> getSOConstructor(Node sourceObj) {
        if (XmlConfigNodeType.COMPLEX_REGEX.equalsNode(sourceObj)) {
            return (ISOConstructor<Node, V>)(new ComplexRegexXmlConstructorImpl());
        } else if (XmlConfigNodeType.SEARCH_SCRIPT.equalsNode(sourceObj)) {
            return (ISOConstructor<Node, V>)(new SearchScriptXmlConstructorImpl());
        } else if (XmlConfigNodeType.KEYED_REGEX.equalsNode(sourceObj)) {
            return (ISOConstructor<Node, V>)(new RegexXmlConstructorImpl());
        }
        return null;
    }
    
    @Override
    public IRefConstructor<Node, ? extends RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> getRefConstructor(Node constrNode) {
        IRefConstructor<Node, ? extends RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> constrObj = null;
        if (XmlConfigNodeType.REF_COMPLEX_REGEX.equalsNode(constrNode)) {
            constrObj = new RefSOComplexRegexXmlConstructorImpl();
        } else if (XmlConfigNodeType.REF_SEARCH_SCRIPT.equalsNode(constrNode)) {
            constrObj = new RefSOSearchScriptXmlConstructorImpl();
        } else if (XmlConfigNodeType.REF_KEYED_REGEX.equalsNode(constrNode)) {
            constrObj = new RefSORegexXmlConstructorImpl();
        }
        return constrObj;
    }

    @Override
    public IRegexAcceptanceConstructor<Node> getRegexAcceptanceConstructor() {
        return new RegexAcceptanceXmlConstructorImpl();
    }
    
    @Override
    public IDTIntervalAcceptanceConstructor<Node> getDTIntervalAcceptanceConstructor() {
        return new DTIntervalAcceptanceXmlConstructorImpl();
    }

}
