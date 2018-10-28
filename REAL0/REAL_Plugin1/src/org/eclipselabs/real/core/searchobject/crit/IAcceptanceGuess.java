package org.eclipselabs.real.core.searchobject.crit;

import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * This is the basic interface of the acceptance guess.
 * The guess identifies if the text contains any values acceptable
 * by the acceptance criterion. A criterion may have more than one guess.
 * The name "guess" doesn't mean the outcome may be uncertain or incomplete.
 * If the guess says the text doesn't contain any results then the search in this text
 * is not performed.
 * Using guesses increases the speed of search in multiple files. Some files
 * may be skipped completely.
 *
 * The acceptance guess is usually referenced by the name
 *
 * @author Vadim Korkin
 *
 */
public interface IAcceptanceGuess extends Cloneable {

    /**
     * Returns the name of this acceptance guess
     * @return the name of this acceptance guess
     */
    public String getName();
    /**
     * Sets the name of this acceptance guess
     * @param newName the new name of this acceptance guess
     */
    public void setName(String newName);
    /**
     * Returns the parent criterion
     * @return the parent criterion
     */
    public IAcceptanceCriterion getAcceptanceCriterion();
    /**
     * Sets the parent criterion
     * @param critRef the new parent criterion
     */
    public void setAcceptanceCriterion(IAcceptanceCriterion critRef);

    /**
     * Returns the guess result for the passed in text and the search result
     * @param logText the text in which a search may be performed
     * @param sr the search result of this search operation
     * @return the guess result which contains the results
     */
    public AcceptanceGuessResult getGuessResult(String logText,
            ISearchResult<? extends ISearchResultObject> sr) throws IncorrectPatternException;

    /**
     * Clones this acceptance guess
     * @return a clone of this acceptance guess
     * @throws CloneNotSupportedException shouldn't be thrown because this interface implements Cloneable
     */
    public IAcceptanceGuess clone() throws CloneNotSupportedException;
}
