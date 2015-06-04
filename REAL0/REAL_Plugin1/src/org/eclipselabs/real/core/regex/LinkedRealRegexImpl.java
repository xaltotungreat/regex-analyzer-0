package org.eclipselabs.real.core.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class LinkedRealRegexImpl extends RealRegexImpl implements ILinkedRealRegex {

    private static final Logger log = LogManager.getLogger(LinkedRealRegexImpl.class);
    protected List<IRealRegex> regexList = new ArrayList<>();

    public LinkedRealRegexImpl(String aName) {
        super(aName);
        type = RealRegexType.LINKED_REAL_REGEX;
    }

    @Override
    public IMatcherWrapper getMatcherWrapper(String logText, Map<String, String> replaceTable, Integer externalFlags) {
        IMatcherWrapper returnWrapper = null;
        List<Pattern> ptList = new ArrayList<>();
        for (IRealRegex currReg : getRegexList()) {
            if (regexFlags != null) {
                ptList.add(Pattern.compile(currReg.getPatternString(replaceTable), regexFlags));
            } else if (externalFlags != null) {
                ptList.add(Pattern.compile(currReg.getPatternString(replaceTable), externalFlags));
            } else {
                ptList.add(Pattern.compile(currReg.getPatternString(replaceTable)));
            }
        }
        RealRegexParamInteger instNum = (RealRegexParamInteger)getParameter(PARAM_NAME_INSTANCE);
        if (instNum != null) {
            returnWrapper = new FindStrategyManyPatternsInstance(ptList, logText, instNum.getValue());
        } else {
            returnWrapper = new FindStrategyManyPatterns(ptList, logText);
        }
        return returnWrapper;
    }

    @Override
    public String getPatternString(Map<String, String> replaceTable) {
        log.warn("getPatternString called on a LinkedRegex. Returning the pattern string for the first IRealRegex");
        String ptStr = null;
        if (!getRegexList().isEmpty()) {
            ptStr = getRegexList().get(0).getPatternString(replaceTable);
        }
        return ptStr;
    }

    @Override
    public void addRealRegex(IRealRegex newReg) {
        getRegexList().add(newReg);
    }

    @Override
    public List<IRealRegex> getRegexList() {
        return regexList;
    }

    @Override
    public void setRegexList(List<IRealRegex> regexList) {
        this.regexList = regexList;
    }

    @Override
    public LinkedRealRegexImpl clone() throws CloneNotSupportedException {
        LinkedRealRegexImpl newInst = (LinkedRealRegexImpl)super.clone();
        List<IRealRegex> newRegList = new ArrayList<>();
        if ((regexList != null) && (!regexList.isEmpty())) {
            for (IRealRegex currReg : regexList) {
                try {
                    newRegList.add(currReg.clone());
                } catch(CloneNotSupportedException cnse) {
                    log.error("Clone not suporrted while cloning LinkedRealRegex", cnse);
                }
            }
        }
        newInst.setRegexList(newRegList);
        return newInst;
    }

    @Override
    public String toString() {
        return "LinkedRealRegexImpl [regexName=" + regexName + ", regexFlags=" + regexFlags + ", regexParamMap=" + regexParamMap
                + " regexList=" + regexList + "]";
    }

}
