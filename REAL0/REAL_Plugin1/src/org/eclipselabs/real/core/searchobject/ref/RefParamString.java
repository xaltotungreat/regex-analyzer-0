package org.eclipselabs.real.core.searchobject.ref;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RefParamString extends RefSimpleImpl<String> {

    private static final Logger log = LogManager.getLogger(RefParamString.class);

    public RefParamString(RefType aType, String aName) {
        super(aType, aName);
    }

    public RefParamString(RefType aType, String aName, String aText) {
        super(aType, aName);
        refValue = aText;
    }

    @Override
    public String toString() {
        return "RefParamString [name=" + name + ", refType=" + refType +", text=" + refValue + "]";
    }

    @Override
    public String resolve(String originalObj) {
        String result = null;
        switch (refType) {
        case ADD:
            StringBuilder sb = new StringBuilder();
            sb.append(originalObj);
            sb.append(refValue);
            result = sb.toString();
            break;
        case REMOVE:
            result = originalObj.replace(refValue, "");
            break;
        case REPLACE_ADD:
        case REPLACE:
            result = refValue;
            break;
        default:
            log.warn("resolve Unsuported type for RefString " + refType + " return null");
            break;
        }
        return result;
    }

    @Override
    public Predicate<String> getDefaultMatchPredicate() {
        // a string is used as other parameters
        // return false to avoid any confusion
        return RefUtil.getAlwaysFalsePredicate();
    }

}
