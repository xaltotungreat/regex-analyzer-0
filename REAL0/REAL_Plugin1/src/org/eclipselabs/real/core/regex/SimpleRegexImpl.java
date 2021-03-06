package org.eclipselabs.real.core.regex;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipselabs.real.core.exception.IncorrectPatternException;

class SimpleRegexImpl extends RealRegexImpl implements ISimpleRegex {
    //private static final Logger log = LogManager.getLogger(SimpleRegexImpl.class);
    protected String regexStr;

    public SimpleRegexImpl(String aName) {
        super(aName);
        type = RealRegexType.SIMPLE_REAL_REGEX;
    }

    public SimpleRegexImpl(String aName, String aRegexStr) {
        this(aName);
        regexStr = aRegexStr;
    }

    @Override
    public String getPatternString(Map<String, String> replaceTable) {
        String tmpRegex = regexStr;
        if ((replaceTable != null) && (!replaceTable.isEmpty())) {
            for (Map.Entry<String,String> currEntry : replaceTable.entrySet()) {
                tmpRegex = tmpRegex.replace(currEntry.getKey(), currEntry.getValue());
            }
        }
        return tmpRegex;
    }

    @Override
    public String getRegexStr() {
        return regexStr;
    }

    @Override
    public void setRegexStr(String regexStr) {
        this.regexStr = regexStr;
    }

    @Override
    public IMatcherWrapper getMatcherWrapper(String logText, Map<String, String> replaceTable, Integer externalFlags) throws IncorrectPatternException {
        Pattern currPattern;
        IMatcherWrapper returnWrapper = null;
        String patternStr = getPatternString(replaceTable);
        try {
            if (regexFlags != null) {
                currPattern = Pattern.compile(patternStr, regexFlags);
            } else if (externalFlags != null) {
                currPattern = Pattern.compile(patternStr, externalFlags);
            } else {
                currPattern = Pattern.compile(patternStr);
            }
            RealRegexParamInteger instNum = (RealRegexParamInteger)getParameter(PARAM_NAME_INSTANCE);
            RealRegexParamInteger lastInstNum = (RealRegexParamInteger)getParameter(PARAM_NAME_LAST_INSTANCE);
            if (instNum != null) {
                returnWrapper = new FindStrategyOnePatternInstance(currPattern, logText, instNum.getValue());
            } else if (lastInstNum != null) {
                returnWrapper = new FindStrategyOnePatternLastInstance(currPattern, logText, lastInstNum.getValue());
            } else {
                returnWrapper = new FindStrategyOnePattern(currPattern, logText);
            }
        } catch (PatternSyntaxException pse) {
            throw new IncorrectPatternException("Incorrect pattern " + patternStr, pse);
        }
        return returnWrapper;
    }

    @Override
    public SimpleRegexImpl clone() throws CloneNotSupportedException {
        return (SimpleRegexImpl)super.clone();
    }

    @Override
    public String toString() {
        return "SimpleRegexImpl [regexName=" + regexName + ", regexFlags=" + regexFlags
                + ", regexParamMap=" + regexParamMap + " regexStr=" + regexStr + "]";
    }

}
