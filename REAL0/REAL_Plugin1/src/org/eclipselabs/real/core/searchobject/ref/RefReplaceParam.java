package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;

public class RefReplaceParam extends RefSimpleImpl<List<IReplaceParam<?>>> {

    private static final Logger log = LogManager.getLogger(RefReplaceParam.class);

    protected Predicate<List<IReplaceParam<?>>> matchPredicate = new Predicate<List<IReplaceParam<?>>>() {

        @Override
        public boolean test(List<IReplaceParam<?>> obj) {
            boolean result = true;
            if (refValue != null) {
                if (obj != null) {
                    for (IReplaceParam<?> rpRef : refValue) {
                        boolean currRefParamMatch = false;
                        for (IReplaceParam<?> rpObj : obj) {
                            if ((rpRef != null) && (rpRef.getKey() != null) && (rpObj != null)) {
                                if (rpRef.getKey().equals(rpObj.getKey())) {
                                    currRefParamMatch = true;
                                    break;
                                }
                            }
                        }
                        if (!currRefParamMatch) {
                            result = false;
                            log.debug("matchByParameters ReplaceParam not matched " + rpRef);
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

    public RefReplaceParam(RefType aType, String aName) {
        super(aType, aName);
    }

    public void addReplaceParam(IReplaceParam<?> param) {
        if (refValue != null) {
            refValue.add(param);
        } else {
            log.warn("addReplaceParam cannot add a IReplaceParam because the list is null");
        }
    }

    @Override
    public String toString() {
        return "RefReplaceParam [name=" + name + ", refType=" + refType + ", replaceParams=" + refValue + "]";
    }

    @Override
    public Predicate<List<IReplaceParam<?>>> getDefaultMatchPredicate() {
        return matchPredicate;
    }


}
