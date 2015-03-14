package org.eclipselabs.real.core.searchobject.crit;

public class AcceptanceGuessResult {

    protected String criterionName;
    protected AcceptanceCriterionType criterionType;
    protected boolean continueSearch;
    
    public AcceptanceGuessResult() {
    }
    
    public AcceptanceGuessResult(String aName, AcceptanceCriterionType aType) {
        this(aName, aType, false);
    }
    
    public AcceptanceGuessResult(String aName, AcceptanceCriterionType aType, boolean cont) {
        criterionName = aName;
        criterionType = aType;
        continueSearch = cont;
    }

    public boolean isContinueSearch() {
        return continueSearch;
    }

    public void setContinueSearch(boolean continueSearch) {
        this.continueSearch = continueSearch;
    }

    public String getCriterionName() {
        return criterionName;
    }

    public void setCriterionName(String criterionName) {
        this.criterionName = criterionName;
    }

    public AcceptanceCriterionType getCriterionType() {
        return criterionType;
    }

    public void setCriterionType(AcceptanceCriterionType criterionType) {
        this.criterionType = criterionType;
    }

}
