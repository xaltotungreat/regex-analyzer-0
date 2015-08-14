package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;

public class RefReplaceParam extends RefSimpleImpl<List<IReplaceableParam<?>>> {

    private static final Logger log = LogManager.getLogger(RefReplaceParam.class);

    protected Predicate<List<IReplaceableParam<?>>> matchPredicate = new Predicate<List<IReplaceableParam<?>>>() {

        @Override
        public boolean test(List<IReplaceableParam<?>> obj) {
            boolean result = true;
            if (refValue != null) {
                if (obj != null) {
                    for (IReplaceableParam<?> rpRef : refValue) {
                        boolean currRefParamMatch = false;
                        for (IReplaceableParam<?> rpObj : obj) {
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

    public void addReplaceParam(IReplaceableParam<?> param) {
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
    public Predicate<List<IReplaceableParam<?>>> getDefaultMatchPredicate() {
        return matchPredicate;
    }


}
