package org.eclipselabs.real.core.searchobject.ref;

import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.util.RealPredicate;

public class RefRealRegexParam extends RefSimpleImpl<IRealRegexParam<?>> {

    protected RealPredicate<IRealRegexParam<?>> matchPredicate = new RealPredicate<IRealRegexParam<?>>() {
        
        @Override
        public boolean test(IRealRegexParam<?> obj) {
            boolean result = true;
            if (refValue != null) {
                if (obj != null) {
                    if (result && (refValue.getName() != null)) {
                        result = refValue.getName().equals(obj.getName());
                    }
                    if (result && (refValue.getType() != null)) {
                        result = refValue.getType().equals(obj.getType());
                    }
                } else {
                    result = false;
                }
            }
            return result;
        }
    };
    
    public RefRealRegexParam(RefType aType, String aName) {
        super(aType, aName);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "RefRealRegexParam [refType=" + refType + ", name=" + name + ", refValue=" + refValue + "]";
    }

    @Override
    public RealPredicate<IRealRegexParam<?>> getDefaultMatchPredicate() {
        return matchPredicate;
    }

}
