package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IRealRegexParam;

public class RefRealRegexParam extends RefSimpleImpl<List<IRealRegexParam<?>>> {
    private static final Logger log = LogManager.getLogger(RefRealRegexParam.class);

    protected Predicate<List<IRealRegexParam<?>>> matchPredicate = new Predicate<List<IRealRegexParam<?>>>() {

        @Override
        public boolean test(List<IRealRegexParam<?>> obj) {
            boolean result = true;
            /*
             * This is a simple ref it must have a value. If the value is null
             * no match
             */
            if (refValue != null) {
                if (obj != null) {
                    for (IRealRegexParam<?> rpRef : refValue) {
                        boolean currRefParamMatch = false;
                        for (IRealRegexParam<?> rpObj : obj) {
                            if ((rpRef != null) && (rpRef.getName() != null) && (rpObj != null)) {
                                if (rpRef.getName().equals(rpObj.getName())) {
                                    currRefParamMatch = true;
                                    break;
                                }
                            }
                        }
                        if (!currRefParamMatch) {
                            result = false;
                            log.debug("matchPredicate RealRegexParam not matched " + rpRef);
                            break;
                        }
                    }
                } else {
                    result = false;
                }
            } else {
                log.warn("Ref Replace Param list is null");
                result = true;
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
    public Predicate<List<IRealRegexParam<?>>> getDefaultMatchPredicate() {
        return matchPredicate;
    }

}
