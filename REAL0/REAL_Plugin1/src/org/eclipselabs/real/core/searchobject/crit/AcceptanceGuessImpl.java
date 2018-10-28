package org.eclipselabs.real.core.searchobject.crit;

public abstract class AcceptanceGuessImpl implements IAcceptanceGuess {

    protected String name;
    protected IAcceptanceCriterion acceptanceCriterion;
    
    public AcceptanceGuessImpl() {
    }
    
    public AcceptanceGuessImpl(String aName) {
        name = aName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    @Override
    public IAcceptanceCriterion getAcceptanceCriterion() {
        return acceptanceCriterion;
    }

    @Override
    public void setAcceptanceCriterion(IAcceptanceCriterion critRef) {
        acceptanceCriterion = critRef;
    }
    
    public IAcceptanceGuess clone() throws CloneNotSupportedException {
        return (IAcceptanceGuess)super.clone();
    }

}
