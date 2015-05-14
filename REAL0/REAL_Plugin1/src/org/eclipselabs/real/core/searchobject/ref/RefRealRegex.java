package org.eclipselabs.real.core.searchobject.ref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexParam;

public class RefRealRegex extends RefImpl<IRealRegex> {
    private static final Logger log = LogManager.getLogger(RefRealRegex.class);

    protected List<RefRealRegexParam> refParamList;
    protected List<RefParamInteger> refRegexFlags;

    /*
     * This could be a simple ref with only the value and a compound ref
     * with more parameters.
     * If this is a simple ref comare the name of the regex value with the object name
     * If this is a compound ref then compare its name and the object name.
     */
    protected Predicate<IRealRegex> matchPredicate = new Predicate<IRealRegex>() {

        @Override
        public boolean test(IRealRegex obj) {
            boolean result = false;
            if (obj != null) {
                if ((refValue != null) && (refValue.getRegexName() != null)) {
                    result = refValue.getRegexName().equals(obj.getRegexName());
                } else {
                    result = getName().equals(obj.getRegexName());
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

    public List<RefParamInteger> getRefRegexFlags() {
        return refRegexFlags;
    }

    public void setRefRegexFlags(List<RefParamInteger> refRegexFlags) {
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
                        boolean refRRParamMatch = allRRParams.stream().anyMatch(rpObj -> refRRParam.getValue().equals(rpObj));
                        if (!refRRParamMatch) {
                            matches = false;
                            log.debug("matchByParameters RealRegexParam not matched " + refRRParam + " obj name=" + obj.getRegexName());
                            break;
                        }
                    }
                }
            }
        }

        if ((refRegexFlags != null) && (!refRegexFlags.isEmpty())) {
            List<RefParamInteger> matchRegexFlags = new ArrayList<>();
            for (RefParamInteger refRRFlag : refRegexFlags) {
                if ((RefType.MATCH.equals(refRRFlag.getType()) && (refRRFlag.getValue() != null))) {
                    matchRegexFlags.add(refRRFlag);
                }
            }

            if (!matchRegexFlags.isEmpty()) {
                Integer objFlags = obj.getRegexFlags();
                List<RefParamInteger> nonMatching = matchRegexFlags.stream().
                        filter(intParam -> !((objFlags & intParam.getValue()) == intParam.getValue())).collect(Collectors.toList());
                if (!nonMatching.isEmpty()) {
                    nonMatching.forEach(flags -> log.debug("matchByParameters Flags not matched " + flags.getName()));
                    matches = false;
                }
            }
        }

        return matches;
    }

    @Override
    public Integer addParameters(IRealRegex obj) {
        Integer count = 0;
        if ((refParamList != null) && (!refParamList.isEmpty())) {
            for (RefRealRegexParam refRRParam : refParamList) {
                if (RefType.ADD.equals(refRRParam.getType())) {
                    if (refRRParam.getValue() == null) {
                        log.error("addParameters null value for ref(cannot process this param)\n" + refRRParam);
                        continue;
                    }
                    if (refRRParam.getPosition() != null) {
                        log.warn("addParameters position is ignored for real regex params");
                    }
                    for (IRealRegexParam<?> rParam : refRRParam.getValue()) {
                        if (obj.getParameter(rParam.getName()) == null) {
                            obj.putParameter(rParam);
                            count++;
                        } else {
                            log.error("addParameters trying to add an already existing param " + rParam);
                        }
                    }
                }
            }
        }
        if ((refRegexFlags != null) && (!refRegexFlags.isEmpty())) {
            for (RefParamInteger refRRFlag : refRegexFlags) {
                if (RefType.ADD.equals(refRRFlag.getType())) {
                    if (refRRFlag.getValue() == null) {
                        log.error("addParameters null value for ref(cannot process this param)\n" + refRRFlag);
                        continue;
                    }
                    if (refRRFlag.getPosition() != null) {
                        log.warn("addParameters position is ignored for real regex params");
                    }
                    if (obj.getRegexFlags() != null) {
                        Integer resultFlags = obj.getRegexFlags() | refRRFlag.getValue();
                        obj.setRegexFlags(resultFlags);
                    } else {
                        obj.setRegexFlags(refRRFlag.getValue());
                    }
                    count++;
                }
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
                    if (refRRParam.getValue() == null) {
                        log.error("replaceAddParameters null value for ref(cannot process this param)\n" + refRRParam);
                        continue;
                    }
                    if (refRRParam.getPosition() != null) {
                        log.warn("replaceAddParameters position is ignored for real regex params");
                    }
                    for (IRealRegexParam<?> rParam : refRRParam.getValue()) {
                        obj.putParameter(rParam);
                        count++;
                    }
                }
            }
        }
        if ((refRegexFlags != null) && (!refRegexFlags.isEmpty())) {
            for (RefParamInteger refRRFlag : refRegexFlags) {
                if (RefType.REPLACE_ADD.equals(refRRFlag.getType())) {
                    if (refRRFlag.getValue() == null) {
                        log.error("replaceAddParameters null value for ref(cannot process this param)\n" + refRRFlag);
                        continue;
                    }
                    if (refRRFlag.getPosition() != null) {
                        log.warn("replaceAddParameters position is ignored for real regex params");
                    }
                    if (obj.getRegexFlags() != null) {
                        Integer resultFlags = obj.getRegexFlags() | refRRFlag.getValue();
                        obj.setRegexFlags(resultFlags);
                    } else {
                        obj.setRegexFlags(refRRFlag.getValue());
                    }
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Integer replaceParameters(IRealRegex obj) {
        Integer count = 0;
        if ((refParamList != null) && (!refParamList.isEmpty())) {
            for (RefRealRegexParam refRRParam : refParamList) {
                if (RefType.REPLACE.equals(refRRParam.getType())) {
                    if (refRRParam.getValue() == null) {
                        log.error("replaceParameters null value for ref(cannot process this param)\n" + refRRParam);
                        continue;
                    }
                    if (refRRParam.getPosition() != null) {
                        log.warn("replaceParameters position is ignored for real regex params");
                    }
                    for (IRealRegexParam<?> rParam : refRRParam.getValue()) {
                        if (obj.getParameter(rParam.getName()) != null) {
                            obj.putParameter(rParam);
                            count++;
                        } else {
                            log.error("replaceParameters trying to replace a non-existing param " + rParam);
                        }
                    }
                }
            }
        }
        if ((refRegexFlags != null) && (!refRegexFlags.isEmpty())) {
            for (RefParamInteger refRRFlag : refRegexFlags) {
                if (RefType.REPLACE.equals(refRRFlag.getType())) {
                    if (refRRFlag.getValue() == null) {
                        log.error("replaceParameters null value for ref(cannot process this param)\n" + refRRFlag);
                        continue;
                    }
                    if (refRRFlag.getPosition() != null) {
                        log.warn("replaceParameters position is ignored for real regex params");
                    }
                    obj.setRegexFlags(refRRFlag.getValue());
                    count++;
                }
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
                    if (refRRParam.getValue() == null) {
                        log.error("removeParameters null value for ref(cannot process this param)\n" + refRRParam);
                        continue;
                    }
                    if (refRRParam.getPosition() != null) {
                        log.warn("removeParameters position is ignored for real regex params");
                    }
                    for (IRealRegexParam<?> rParam : refRRParam.getValue()) {
                        if (obj.getParameter(rParam.getName()) != null) {
                            obj.removeParameter(rParam.getName(), rParam.getType());
                            count++;
                        } else {
                            log.error("removeParameters trying to remove a non-existing param " + rParam);
                        }
                    }
                }
            }
        }
        if ((refRegexFlags != null) && (!refRegexFlags.isEmpty())) {
            for (RefParamInteger refRRFlag : refRegexFlags) {
                if (RefType.REMOVE.equals(refRRFlag.getType())) {
                    if (refRRFlag.getValue() == null) {
                        log.error("removeParameters null value for ref(cannot process this param)\n" + refRRFlag);
                        continue;
                    }
                    if (refRRFlag.getPosition() != null) {
                        log.warn("removeParameters position is ignored for real regex params");
                    }
                    if (obj.getRegexFlags() != null) {
                        Integer resultFlags = obj.getRegexFlags() & (refRRFlag.getValue() ^ Integer.MAX_VALUE);
                        obj.setRegexFlags(resultFlags);
                        count++;
                    } else {
                        log.warn("removeParameters obj regex flags is null nothing to remove");
                    }
                }
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
    public Predicate<IRealRegex> getDefaultMatchPredicate() {
        return matchPredicate;
    }

}
