package org.eclipselabs.real.core.searchobject.crit;

/**
 * This factory separates the interfaces for the criteria from
 * the implementations.
 *
 * @author Vadim Korkin
 *
 */
public class AcceptanceCriterionFactory {

    private static AcceptanceCriterionFactory instance;

    private AcceptanceCriterionFactory() {
    }

    public static AcceptanceCriterionFactory getInstance() {
        if (instance == null) {
            instance = new AcceptanceCriterionFactory();
        }
        return instance;
    }

    public IRegexAcceptanceCriterion getRegexAcceptanceCriterion(AcceptanceCriterionType aType) {
        return new RegexAcceptanceCriterion(aType);
    }

    public IDTIntervalCriterion getDTIntervalCriterion(AcceptanceCriterionType aType) {
        return new DTIntervalCriterion(aType);
    }

    public IDTIntervalGuess getDTIntervalGuess() {
        return new DTIntervalGuessImpl();
    }
}
