package org.eclipselabs.real.core.searchobject.ref;

import java.util.function.Predicate;

import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;

public class RefAcceptanceCriterion extends RefSimpleImpl<IAcceptanceCriterion> {

    //private static final Logger log = LogManager.getLogger(RefAcceptanceCriterion.class);

    protected Predicate<IAcceptanceCriterion> matchPredicate = new Predicate<IAcceptanceCriterion>() {

        @Override
        public boolean test(IAcceptanceCriterion obj) {
            boolean result = true;
            if (refValue != null) {
                if (obj != null) {
                    if (result && (refValue.getName() != null)) {
                        result = ((refValue.getName().equals(obj.getName())));
                    }
                    if (result && (refValue.getType() != null)) {
                        result = (refValue.getType().equals(obj.getType()));
                    }
                } else {
                    result = false;
                }
            }
            return result;
        }
    };

    public RefAcceptanceCriterion(RefType aType, String aName) {
        super(aType, aName);
    }

    public RefAcceptanceCriterion(RefType aType, String aName, Integer pos) {
        super(aType, aName, pos);
    }

    @Override
    public String toString() {
        return "RefAcceptanceCriterion [refType=" + refType + ", name=" + name
                + ", position=" + position + ", acceptanceCriterion=" + refValue + "]";
    }

    @Override
    public Predicate<IAcceptanceCriterion> getDefaultMatchPredicate() {
        return matchPredicate;
    }

}
