package org.eclipselabs.real.core.searchobject.ref;

import org.eclipselabs.real.core.searchobject.ISearchObjectDateInfo;
import org.eclipselabs.real.core.util.RealPredicate;

public class RefDateInfo extends RefSimpleImpl<ISearchObjectDateInfo> {

    //private static final Logger log = LogManager.getLogger(RefDateInfo.class);
    
    public RefDateInfo(RefType aType, String aName) {
        super(aType, aName);
    }

    @Override
    public String toString() {
        return "RefDateInfo [name=" + name + ", refType=" + refType + ", dateInfo=" + refValue + "]";
    }

    /**
     * A ref to a date info is always equal to other date info because
     * a search object can have only one date info
     */
    @Override
    public RealPredicate<ISearchObjectDateInfo> getDefaultMatchPredicate() {
        return RefUtil.getAlwaysTruePredicate();
    }

}
