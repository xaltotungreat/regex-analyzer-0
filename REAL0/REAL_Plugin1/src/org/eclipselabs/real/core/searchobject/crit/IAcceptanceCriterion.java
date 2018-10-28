package org.eclipselabs.real.core.searchobject.crit;

import java.util.List;
import java.util.Set;

import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * The interface IAcceptanceCriterion resembles Predicate<T> but in the test method
 * two parameters are passed: the search result object that is going to be tested
 * and the search result. It is assumed that for many search objects the search result
 * plays the context role during the search.
 * The acceptance criteria are used to filter out unwanted results. Even though it may be
 * possible to write a regular expressions that finds only the correct results it may be
 * too long, take too much time to execute etc. Then it makes sense to create a simpler regular expression
 * that will filter out as many records as possible and use the acceptance criteria
 * to remove the unwanted records that passed through the first regular expression.
 *
 * The acceptance criterion may have many basic
 * properties that are defined in this interface (by declaring the corresponding methods).
 * The most basic properties are:
 * Name - a String to use for reference
 * Stages - the stages of search process during which this acceptance must be applied.
 *     See more {@link AcceptanceCriterionStage}
 * Type - see more about types {@link AcceptanceCriterionType}
 * Accumulating - some acceptance criteria may accumulate information during early stages
 *     of search to use it at later stages to make a decision.
 * Guess list - some acceptances may use guesses to try to identify if the text contains
 *     acceptable results or not. The guess then takes the whole text and returns either true or false
 *     (search in this text or it certainly doesn't contain any results). Even though it is called a guess
 *     it is assumed that the results are certain if the guess returns false no search is performed
 *
 * @author Vadim Korkin
 *
 */
public interface IAcceptanceCriterion extends Cloneable {

    public AcceptanceCriterionType getType();
    public void setType(AcceptanceCriterionType newType);
    public String getName();
    public void setName(String newName);
    public Set<AcceptanceCriterionStage> getStages();
    public void setStages(Set<AcceptanceCriterionStage> newStages);
    /**
     * Returns true if this criterion is accumulating false otherwise.
     * This characteristic cannot be changed, it is the property of the criterion.
     * @return true if this criterion is accumulating false otherwise.
     */
    public boolean isAccumulating();

    public List<IAcceptanceGuess> getGuessList();
    public void setGuessList(List<IAcceptanceGuess> newList);
    public void addGuess(IAcceptanceGuess newGuess);
    public void addGuesses(List<IAcceptanceGuess> newGuesses);

    /*
     * If the criterion is created by means of injection the instance for the guessses
     * must be set after the initialization in the init method.
     */
    public void updateCriterionReferences();
    /**
     * This method returns a list of results for every guess. All the results
     * must be true for the search to continue
     * @param logText the full text
     * @param sr the search result
     * @return a list of results for every guess
     */
    public List<AcceptanceGuessResult> getGuessResults(String logText, ISearchResult<? extends ISearchResultObject> sr) throws IncorrectPatternException;

    /**
     * Some acceptance criteria may contain parameters (replaceable parameters)
     * that must be initialized from the replace table.
     * @param sr the search result object. It usually contains important information
     * about the current search operation like the current replace table
     */
    public void init(ISearchResult<? extends ISearchResultObject> sr);
    /**
     * Tests the SRO against this acceptance criteria. Returns true if the test is passed
     * false otherwise
     * @param sro the search result object to test
     * @param sr the search result of this search
     * @return true if the test is passed false otherwise
     */
    public boolean test(ISearchResultObject sro, ISearchResult<? extends ISearchResultObject> sr);
    public IAcceptanceCriterion clone() throws CloneNotSupportedException;
}
