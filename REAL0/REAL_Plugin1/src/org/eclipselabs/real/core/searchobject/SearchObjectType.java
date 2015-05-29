package org.eclipselabs.real.core.searchobject;

public enum SearchObjectType {
    /**
     * Currenly is not used
     */
    SIMPLE_REGEX,
    /**
     * See more {@link ISOComplexRegex}
     */
    COMPLEX_REGEX,
    /**
     * See more {@link ISOSearchScript}
     */
    SEARCH_SCRIPT,
    /**
     * See more {@link ISOComplexRegexView}
     */
    COMPLEX_REGEX_VIEW;
}
