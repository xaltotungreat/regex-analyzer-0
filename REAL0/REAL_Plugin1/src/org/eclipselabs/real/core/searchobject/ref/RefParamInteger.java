package org.eclipselabs.real.core.searchobject.ref;

import java.util.function.Predicate;


public class RefParamInteger extends RefSimpleImpl<Integer> {

    public RefParamInteger(RefType aType, String aName) {
        super(aType, aName);
    }

    public RefParamInteger(RefType aType, String aName, Integer rFlags) {
        super(aType, aName);
        refValue = rFlags;
    }

    @Override
    public Integer resolve(Integer originalObj) {
        Integer result = refValue;
        if (originalObj != null) {
            switch (refType) {
            case ADD:
                result = result | originalObj;
                break;
            case REMOVE:
                result = originalObj & (refValue ^ Integer.MAX_VALUE);
                break;
            case REPLACE:
            case REPLACE_ADD:
                result = originalObj;
                break;
            default:
                break;
            }
        }
        return result;
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
        return "RefRegexFlags [name=" + name + ", refType=" + refType + ", regexFlags=" + refValue + "]";
    }

    @Override
    public Predicate<Integer> getDefaultMatchPredicate() {
        // a string is used as other parameters
        // return false to avoid any confusion
        return RefUtil.getAlwaysFalsePredicate();
    }

}
