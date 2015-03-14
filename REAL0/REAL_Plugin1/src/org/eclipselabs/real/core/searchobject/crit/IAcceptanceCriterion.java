package org.eclipselabs.real.core.searchobject.crit;

import java.util.List;
import java.util.Set;

import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * The interface IAcceptanceCriterion resembles Predicate<T> but in the test method
 * two parameters are passed: the search result object that is going to be tested
 * and the search result. It is assumed that for many search objects the search result
 * plays the context role during the search. 
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
    public boolean isAccumulating();
    
    public List<IAcceptanceGuess> getGuessList();
    public void setGuessList(List<IAcceptanceGuess> newList);
    public void addGuess(IAcceptanceGuess newGuess);
    public void addGuesses(List<IAcceptanceGuess> newGuesses);
    public List<AcceptanceGuessResult> getGuessResults(String logText, ISearchResult<? extends ISearchResultObject> sr);
    
    public void init(ISearchResult<? extends ISearchResultObject> sr);
    public boolean test(ISearchResultObject sro, ISearchResult<? extends ISearchResultObject> sr);
    public IAcceptanceCriterion clone() throws CloneNotSupportedException;
}
