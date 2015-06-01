package org.eclipselabs.real.core.searchobject;

/**
 * This factory separates the interfaces that should be used when working with
 * the search objects from the implementations.
 *
 * @author Vadim Korkin
 *
 */
public class SearchObjectFactory {

    private SearchObjectFactory (){}

    public static SearchObjectFactory getInstance() {
        return new SearchObjectFactory();
    }

    public ISOComplexRegex getSOComplexRegex(String aName) {
        return new SOComplexRegexImpl(aName);
    }

    public ISOComplexRegexView getSOComplexRegexView(String aName) {
        return new SOComplexRegexViewImpl(aName);
    }

    public ISORegex getSORegex(String aName) {
        return new SORegexImpl(aName);
    }

    public ISOSearchScript getSOSearchScript(String aName) {
        return new SOSearchScript(aName);
    }

    public ISearchObjectDateInfo getDateInfo() {
        return new SearchObjectDateInfoImpl();
    }

    public ISearchObjectGroup<String> getSearchObjectGroup() {
        return new SearchObjectGroupStrImpl();
    }

    public ISearchObjectGroup<String> getSearchObjectGroup(String groupVal, String delim) {
        return new SearchObjectGroupStrImpl(groupVal, delim);
    }

    public ISearchObjectGroup<String> getSearchObjectGroup(String groupVal) {
        return new SearchObjectGroupStrImpl(groupVal);
    }

}
