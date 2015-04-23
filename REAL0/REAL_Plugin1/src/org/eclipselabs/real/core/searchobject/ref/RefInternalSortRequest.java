package org.eclipselabs.real.core.searchobject.ref;

import java.util.function.Predicate;

import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public class RefInternalSortRequest extends RefSimpleImpl<IInternalSortRequest> {

    protected Predicate<IInternalSortRequest> matchPredicate = new Predicate<IInternalSortRequest>() {

        @Override
        public boolean test(IInternalSortRequest obj) {
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

    public RefInternalSortRequest(RefType aType, String aName) {
        super(aType, aName);
    }

    public RefInternalSortRequest(RefType aType, String aName, Integer pos) {
        super(aType, aName, pos);
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
        return "RefInternalSortRequest [refType=" + refType + ", name=" + name
                + ", position=" + position + ", sortRequest=" + refValue + "]";
    }

    @Override
    public Predicate<IInternalSortRequest> getDefaultMatchPredicate() {
        return matchPredicate;
    }


}
