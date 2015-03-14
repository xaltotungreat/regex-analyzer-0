package org.eclipselabs.real.core.searchobject.crit;

import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IAcceptanceGuess extends Cloneable {

    public String getName();
    public void setName(String newName);
    public IAcceptanceCriterion getAcceptanceCriterion();
    public void setAcceptanceCriterion(IAcceptanceCriterion critRef);
    
    public AcceptanceGuessResult getGuessResult(String logText, 
            ISearchResult<? extends ISearchResultObject> sr);
    
    public IAcceptanceGuess clone() throws CloneNotSupportedException;
}
