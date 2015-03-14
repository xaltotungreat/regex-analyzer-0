package org.eclipselabs.real.core.searchresult.sort;

public class SortRequestFactory {

    private SortRequestFactory() {}
    
    public static SortRequestFactory getInstance() {
        return new SortRequestFactory();
    }
    
    public IDateTimeSortRequest getDateTimeSortRequest(String aName) {
        return new DateTimeSortRequest(aName); 
    }
    
    public IRegexSortRequest getRegexSortRequest(String aName) {
        return new RegexSortRequest(aName);
    }
    
    public IRegexComplexSortRequest<String> getRegexComplexSortRequest(String aName) {
        return new RegexComplexSortRequest<String>(aName);
    }

}
