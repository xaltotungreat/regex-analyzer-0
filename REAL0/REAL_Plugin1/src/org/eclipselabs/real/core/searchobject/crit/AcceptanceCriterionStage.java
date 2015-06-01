package org.eclipselabs.real.core.searchobject.crit;

public enum AcceptanceCriterionStage {

    /**
     * This is the stage when the search is performed and new SROs are created
     */
    SEARCH,
    /**
     * This is the stage when many search results from different files are merged
     * into one search result
     */
    MERGE;
}
