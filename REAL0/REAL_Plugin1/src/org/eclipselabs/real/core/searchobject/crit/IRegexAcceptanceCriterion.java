package org.eclipselabs.real.core.searchobject.crit;

import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;

/**
 * This is the interface for the regex acceptance criteria. It has a list
 * of {@link IRealRegex} and the type. The type defines what to do
 * with the regexes: find, not find, match etc.
 * See more {@link AcceptanceCriterionType}
 *
 * @author Vadim Korkin
 *
 */
public interface IRegexAcceptanceCriterion extends IAcceptanceCriterion {

    /**
     * Returns the list of acceptance regexes
     * @return the list of acceptance regexes
     */
    public List<IRealRegex> getAcceptanceRegexList();
    /**
     * Sets the list of acceptance regexes
     * @param acceptanceRegex the new list of acceptance regexes
     */
    public void setAcceptanceRegexList(List<IRealRegex> acceptanceRegex);
}
