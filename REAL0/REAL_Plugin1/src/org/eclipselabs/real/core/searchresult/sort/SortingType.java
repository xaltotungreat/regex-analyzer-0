package org.eclipselabs.real.core.searchresult.sort;

public enum SortingType {
    /**
     * No sorting is performed in any case 
     */
    NONE,
    /**
     * Sorting will be performed when both the method sort is called and 
     * when this Search Result is merged with other results. If the sort
     * method is called all existing SROs are sorted based on the date fields.
     * If this Search Result is merged with other results the lists from other
     * SRs are inserted into the SRO list of this SR. The insertion happens 
     * according to the rules of MERGE_RESULTS_DATE_TIME. Then the whole 
     * list is sorted by the date fields.
     */
    DATE_TIME,
    /**
     * Sorting will be performed when both the method sort is called and 
     * when this Search Result is merged with other results. If the sort
     * method is called all existing SROs are sorted based on the sort regex fields.
     * If this Search Result is merged with other results the lists from other
     * SRs are inserted into the SRO list of this SR. The insertion happens 
     * according to the rules of MERGE_RESULTS_DATE_TIME. Then the whole 
     * list is sorted by the date fields.
     */
    REGEX;
}
