package org.eclipselabs.real.core.searchresult.sort;

public enum SortingApplicability {

    /**
     * When errors are encountered trying to find the right value
     * set NONE.
     */
    NONE(0),
    /**
     * Only when this Search Result is merged with other results
     * sorting will be performed. The first search result objects
     * will be selected from both files. The SROs (search result objects) from
     * the SR with the lesser first SRO will be inserted first in the list.
     */
    MERGE_RESULTS(1),
    /**
     * Sorting will be performed when both the method sort is called and 
     * when this Search Result is merged with other results. If the sort
     * method is called all existing SROs are sorted.
     * If this Search Result is merged with other results the lists from other
     * SRs are inserted into the SRO list of this SR. The insertion happens 
     * according to the rules of MERGE_RESULTS_DATE_TIME. Then the whole 
     * list is sorted.
     */
    ALL(2);
    
    private Integer scope;
    
    private SortingApplicability(Integer aScope) {
        scope = aScope;
    }

    public Integer getScope() {
        return scope;
    }

    public void setScope(Integer scope) {
        this.scope = scope;
    }
}
