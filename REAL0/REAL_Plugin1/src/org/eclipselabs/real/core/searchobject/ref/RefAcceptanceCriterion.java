package org.eclipselabs.real.core.searchobject.ref;

import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.util.RealPredicate;

public class RefAcceptanceCriterion extends RefSimpleImpl<IAcceptanceCriterion> {

    //private static final Logger log = LogManager.getLogger(RefAcceptanceCriterion.class);

    protected RealPredicate<IAcceptanceCriterion> matchPredicate = new RealPredicate<IAcceptanceCriterion>() {
        
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
    public RealPredicate<IAcceptanceCriterion> getDefaultMatchPredicate() {
        return matchPredicate;
    }

}
