package org.eclipselabs.real.core.searchobject.ref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.util.RealPredicate;

public class RefRealRegex extends RefImpl<IRealRegex> {
    private static final Logger log = LogManager.getLogger(RefRealRegex.class);

    protected List<RefRealRegexParam> refParamList;
    protected RefParamInteger refRegexFlags;

    protected RealPredicate<IRealRegex> matchPredicate = new RealPredicate<IRealRegex>() {

        @Override
        public boolean test(IRealRegex obj) {
            boolean result = true;
            if (refValue != null) {
                if ((obj != null) && (refValue.getRegexName() != null)) {
                    result = refValue.getRegexName().equals(obj.getRegexName());
                }
            }
            return result;
        }
    };

    public RefRealRegex(RefType aType, String aRefName) {
        super(aType,aRefName);
    }

    public RefRealRegex(RefType aType, String aRefName, Integer pos) {
        super(aType,aRefName, pos);
    }

    public RefRealRegex(RefType aType, String aRefName, IRealRegex reg) {
        this(aType,aRefName);
        refValue = reg;
    }

    public List<RefRealRegexParam> getRefParamList() {
        return refParamList;
    }

    public void setRefParamList(List<RefRealRegexParam> refParamList) {
        this.refParamList = refParamList;
    }

    public RefParamInteger getRefRegexFlags() {
        return refRegexFlags;
    }

    public void setRefRegexFlags(RefParamInteger refRegexFlags) {
        this.refRegexFlags = refRegexFlags;
    }

    @Override
    protected IRealRegex getClone(IRealRegex obj) {
        IRealRegex clonedObj = null;
        try {
            clonedObj = obj.clone();
        } catch (CloneNotSupportedException e) {
            log.error("Clone should be supported",e);
        }
        return clonedObj;
    }

    @Override
    public boolean matchByParameters(IRealRegex obj) {
        boolean matches = true;
        if ((refParamList != null) && (!refParamList.isEmpty())) {
            List<RefRealRegexParam> matchRealRegexParams = new ArrayList<>();
            for (RefRealRegexParam refRRParam : refParamList) {
                if ((RefType.MATCH.equals(refRRParam.getType()) && (refRRParam.getValue() != null))) {
                    matchRealRegexParams.add(refRRParam);
                }
            }

            if (!matchRealRegexParams.isEmpty()) {
                Collection<IRealRegexParam<?>> allRRParams = obj.getParameters();
                if ((allRRParams != null) && (!allRRParams.isEmpty())) {
                    for (RefRealRegexParam refRRParam : matchRealRegexParams) {
                        boolean refRRParamMatch = false;
                        for (IRealRegexParam<?> rpObj : allRRParams) {
                            if (refRRParam.getValue().equals(rpObj)) {
                                refRRParamMatch = true;
                                break;
                            }
                        }
                        if (!refRRParamMatch) {
                            matches = false;
                            log.debug("matchByParameters RealRegexParam not matched " + refRRParam + " obj name=" + obj.getRegexName());
                            break;
                        }
                    }
                }
            }
        }
        if (matches && (refRegexFlags != null) && (RefType.MATCH.equals(refRegexFlags.getType()))) {
            if (refRegexFlags.getValue() != null) {
                if (!refRegexFlags.getValue().equals(obj.getRegexFlags())) {
                    matches = false;
                    log.debug("matchByParameters RegexFlags not matched " + refRegexFlags + " obj name=" + obj.getRegexName());
                }
            } else {
                log.error("matchByParameters param value is null " + refRegexFlags);
            }
        }
        return matches;
    }

    @Override
    public Integer addParameters(IRealRegex obj) {
        Integer count = 0;
        if ((refParamList != null) && (!refParamList.isEmpty())) {
            for (RefRealRegexParam refRRParam : refParamList) {
                if (refRRParam.getValue() != null) {
                    log.error("addParameters null value for ref(cannot process this param)\n" + refRRParam);
                    continue;
                }
                if (refRRParam.getPosition() != null) {
                    log.warn("addParameters position is ignored for real regex params");
                }
                IRealRegexParam<?> existingParam = obj.getParameter(refRRParam.getValue().getName());
                if (existingParam == null) {
                    obj.putParameter(refRRParam.getValue());
                    count++;
                } else {
                    log.error("addParameters trying to add an already existing param " + refRRParam);
                }
            }
        }
        if ((refRegexFlags != null) && (RefType.ADD.equals(refRegexFlags.getType()))) {
            if (refRegexFlags.getValue() != null) {
                if (obj.getRegexFlags() != null) {
                    Integer resultFlags = obj.getRegexFlags() | refRegexFlags.getValue();
                    obj.setRegexFlags(resultFlags);
                } else {
                    obj.setRegexFlags(refRegexFlags.getValue());
                }
                count++;
            } else {
                log.error("addParameters param value is null " + refRegexFlags);
            }
        }
        return count;
    }

    @Override
    public Integer replaceAddParameters(IRealRegex obj) {
        Integer count = 0;
        if ((refParamList != null) && (!refParamList.isEmpty())) {
            for (RefRealRegexParam refRRParam : refParamList) {
                if (RefType.REPLACE_ADD.equals(refRRParam.getType())) {
                    if (refRRParam.getValue() != null) {
                        log.error("replaceAddParameters null value for ref(cannot process this param)\n" + refRRParam);
                        continue;
                    }
                    if (refRRParam.getPosition() != null) {
                        log.warn("replaceAddParameters position is ignored for real regex params");
                    }
                    obj.putParameter(refRRParam.getValue());
                    count++;
                }
            }
        }
        if ((refRegexFlags != null) && (RefType.REPLACE_ADD.equals(refRegexFlags.getType()))) {
            if (refRegexFlags.getValue() != null) {
                if (obj.getRegexFlags() != null) {
                    Integer resultFlags = obj.getRegexFlags() | refRegexFlags.getValue();
                    obj.setRegexFlags(resultFlags);
                } else {
                    obj.setRegexFlags(refRegexFlags.getValue());
                }
                count++;
            } else {
                log.error("replaceAddParameters param value is null " + refRegexFlags);
            }
        }
        return count;
    }

    public Integer replaceParameters(IRealRegex obj) {
        Integer count = 0;
        if ((refParamList != null) && (!refParamList.isEmpty())) {
            for (RefRealRegexParam refRRParam : refParamList) {
                if (RefType.REPLACE_ADD.equals(refRRParam.getType())) {
                    if (refRRParam.getValue() != null) {
                        log.error("replaceAddParameters null value for ref(cannot process this param)\n" + refRRParam);
                        continue;
                    }
                    if (refRRParam.getPosition() != null) {
                        log.warn("replaceAddParameters position is ignored for real regex params");
                    }
                    IRealRegexParam<?> existingParam = obj.getParameter(refRRParam.getValue().getName());
                    if (existingParam != null) {
                        obj.putParameter(refRRParam.getValue());
                        count++;
                    } else {
                        log.error("replaceParameters trying to replace a non-existing param " + refRRParam);
                    }
                }
            }
        }
        if ((refRegexFlags != null) && (RefType.REPLACE_ADD.equals(refRegexFlags.getType()))
                && (refRegexFlags.getValue() != null)) {
            obj.setRegexFlags(refRegexFlags.getValue());
            count++;
            if (refRegexFlags.getValue() == null) {
                log.warn("replaceParameters param value is null " + refRegexFlags);
            }
        }
        return count;
    }

    @Override
    public Integer removeParameters(IRealRegex obj) {
        Integer count = 0;
        if ((refParamList != null) && (!refParamList.isEmpty())) {
            for (RefRealRegexParam refRRParam : refParamList) {
                if (RefType.REMOVE.equals(refRRParam.getType())) {
                    if (refRRParam.getValue() != null) {
                        log.error("removeParameters null value for ref(cannot process this param)\n" + refRRParam);
                        continue;
                    }
                    if (refRRParam.getPosition() != null) {
                        log.warn("removeParameters position is ignored for real regex params");
                    }
                    obj.removeParameter(refRRParam.getValue().getName(), refRRParam.getValue().getType());
                    count++;
                }
            }
        }
        if ((refRegexFlags != null) && (RefType.REMOVE.equals(refRegexFlags.getType()))
                && (refRegexFlags.getValue() != null)) {
            if (refRegexFlags.getValue() != null) {
                if (obj.getRegexFlags() != null) {
                    Integer resultFlags = obj.getRegexFlags() & (refRegexFlags.getValue() ^ Integer.MAX_VALUE);
                    obj.setRegexFlags(resultFlags);
                    count++;
                } else {
                    log.warn("removeParameters obj regex flags is null nothing to remove");
                }
            } else {
                log.error("removeParameters param value is null " + refRegexFlags);
            }
        }
        return count;
    }

    /**
     * RefRealRegex contains no refs that could be processed in cpAdd
     * (i.e. no refs that contains other refs) therefore this method returns null.
     *
     * @return 0
     */
    @Override
    protected Integer cpAdd(IRealRegex obj) {
        return 0;
    }

    /**
     * RefRealRegex contains no refs that could be processed in cpReplace
     * (i.e. no refs that contains other refs) therefore this method returns null.
     *
     * @return 0
     */
    @Override
    protected Integer cpReplace(IRealRegex obj) {
        return 0;
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
        return "RefRealRegex [refType=" + refType + ", name=" + name + ", refRegex=" + refValue
                + ", position=" + position + ", refParamList=" + refParamList + ", refRegexFlags=" + refRegexFlags + "]";
    }

    @Override
    public RealPredicate<IRealRegex> getDefaultMatchPredicate() {
        return matchPredicate;
    }

}
