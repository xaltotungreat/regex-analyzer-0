package org.eclipselabs.real.core.searchobject.ref;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.ISORegex;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectType;

public class RefSORegex extends RefKeyedSO<ISORegex> {

    private static final Logger log = LogManager.getLogger(RefSORegex.class);
    protected RefRealRegex refRegex;

    public RefSORegex(SearchObjectType soType, String aName) {
        super(soType, aName);
    }

    public RefSORegex(RefType aType, SearchObjectType soType, String aName) {
        super(aType, soType, aName);
    }

    public RefSORegex(SearchObjectType soType, String aName, ISearchObjectGroup<String> aGroup, Map<String, String> aTags) {
        super(soType, aName, aGroup, aTags);
    }

    /**
     * @return the refRegex
     */
    public RefRealRegex getRefRegex() {
        return refRegex;
    }

    /**
     * @param refRegex the refRegex to set
     */
    public void setRefRegex(RefRealRegex refRegex) {
        this.refRegex = refRegex;
    }

    @Override
    public boolean matchByParameters(ISORegex obj) {
        boolean matches = super.matchByParameters(obj);
        if (matches && (refRegex != null)) {
            matches = refRegex.matchByParameters(obj.getRegex());
        }
        return matches;
    }

    @Override
    public Integer addParameters(ISORegex obj) {
        Integer count =  super.addParameters(obj);
        if (RefType.ADD.equals(refRegex.getType())) {
            log.error("addParameters cannot add a parameter to a Keyed regex");
        }
        return count;
    }

    @Override
    public Integer replaceAddParameters(ISORegex obj) {
        Integer count = super.replaceAddParameters(obj);
        if (refRegex != null) {
            if (RefType.REPLACE_ADD.equals(refRegex.getType())) {
                if (refRegex.getValue() != null) {
                    obj.setRegex(refRegex.getValue());
                }
                count++;
            } else {
                log.error("replaceAddParameters param value is null " + refRegex);
            }
        }
        return count;
    }

    @Override
    public Integer replaceParameters(ISORegex obj) {
        Integer count = super.replaceParameters(obj);
        if (refRegex != null) {
            if (RefType.REPLACE.equals(refRegex.getType())) {
                if (refRegex.getValue() != null) {
                    obj.setRegex(refRegex.getValue());
                }
                count++;
            } else {
                log.error("replaceAddParameters param value is null " + refRegex);
            }
        }
        return count;
    }

    @Override
    public Integer removeParameters(ISORegex obj) {
        Integer count = super.removeParameters(obj);
        if (refRegex != null) { 
            if (RefType.REMOVE.equals(refRegex.getType())) {
                log.warn("removeParameters RefRegex with REMOVE type. Setting the regex to null");
                obj.setRegex(null);
            }
            count++;
        }
        return count;
    }

    @Override
    public Integer cpAdd(ISORegex obj) {
        Integer count = super.cpAdd(obj);
        if (refRegex != null) { 
            if (RefType.CP_ADD.equals(refRegex.getType())) {
                log.warn("cpAdd RefRegex with CP_ADD type. Trying to replace the current real regex");
                IRealRegex resolvedObj = refRegex.resolve(obj.getRegex());
                if (resolvedObj != null) {
                    obj.setRegex(null);
                } else {
                    log.error("cpAdd resolved object as null not replaced");
                }
            }
            count++;
        }
        return count;
    }

    @Override
    public Integer cpReplace(ISORegex obj) {
        Integer count = super.cpReplace(obj);
        if (refRegex != null) {
            if (RefType.CP_REPLACE.equals(refRegex.getType())) {
                IRealRegex resolvedObj = refRegex.resolve(obj.getRegex());
                if (resolvedObj != null) {
                    obj.setRegex(null);
                } else {
                    log.error("cpReplace resolved object as null not replaced");
                }
            }
            count++;
        }
        return count;
    }

    @Override
    public String toString() {
        return "RefSORegex [name=" + name + ", group=" + group + ", tags=" + tags + ", refType=" + refType 
                + ", refName=" + refName + ", refGroup=" + refGroup + ", refTags=" + refTags
                + ", refReplaceParams=" + refReplaceParams + ", refSortRequests=" + refSortRequests 
                + ", refRegexFlags=" + refRegexFlags + ", refDateInfo=" + refDateInfo 
                + ", refRegex=" + refRegex + "]";
    }

}
