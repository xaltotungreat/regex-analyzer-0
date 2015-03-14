package org.eclipselabs.real.core.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class MultiLineRegexImpl extends RealRegexImpl implements IMultiLineRegex {

    protected List<String> regexStrings = new ArrayList<String>();
    
    public MultiLineRegexImpl(String aName) {
        super(aName);
        type = RealRegexType.MULTILINE_REAL_REGEX;
    }
    
    public MultiLineRegexImpl(String aName, Collection<String> regexLines) {
        this(aName);
        regexStrings.clear();
        regexStrings.addAll(regexLines);
    }
    
    @Override
    public String toString() {
        StringBuilder lineRegexStr = new StringBuilder();
        lineRegexStr.append("MultiLineRegexImpl [regexName=" + regexName + ", regexFlags=" + regexFlags 
                + ", regexParamMap=" + regexParamMap);
        for (String regexLine : regexStrings) {
            lineRegexStr.append("\n ").append(regexLine);
        }
        return lineRegexStr.toString();
    }

    @Override
    public List<String> getRegexStrings() {
        return regexStrings;
    }

    @Override
    public void setRegexStrings(List<String> regexStrings) {
        this.regexStrings = regexStrings;
    }
    
    @Override
    public String getPatternString(Map<String, String> replaceTable) {
        StringBuilder regexStr = new StringBuilder();
        List<String> currRegex = new ArrayList<String>();
        currRegex.addAll(regexStrings);
        if (replaceTable != null) {
            List<String> newRegex = new ArrayList<String>();
            for (String currRegexStr : currRegex) {
                String newRegexStr = currRegexStr;
                for (Map.Entry<String, String> currEntry : replaceTable.entrySet()) {
                    newRegexStr = newRegexStr.replace(currEntry.getKey(), currEntry.getValue());
                }
                newRegex.add(newRegexStr);
            }
            for (String regexLine : newRegex) {
                regexStr.append(regexLine);
            }
        } else {
            for (String regexLine : currRegex) {
                regexStr.append(regexLine);
            }
        }
        return regexStr.toString();
    }

    @Override
    public synchronized IMatcherWrapper getMatcherWrapper(String logText, Map<String, String> replaceTable, Integer externalFlags) {
        Pattern currPattern;
        IMatcherWrapper returnWrapper = null;
        if (regexFlags != null) {
            currPattern = Pattern.compile(getPatternString(replaceTable), regexFlags);
        } else if (externalFlags != null) {
            currPattern = Pattern.compile(getPatternString(replaceTable), externalFlags);
        } else {
            currPattern = Pattern.compile(getPatternString(replaceTable));
        }
        RealRegexParamInteger instNum = (RealRegexParamInteger)getParameter(PARAM_NAME_INSTANCE);
        RealRegexParamInteger maxRegionSize = (RealRegexParamInteger)getParameter(PARAM_NAME_MAX_REGION_SIZE);
        RealRegexParamIRealRegex regionRegex = (RealRegexParamIRealRegex)getParameter(PARAM_NAME_REGION_REGEX);
        if ((instNum != null) && (maxRegionSize == null) && (regionRegex == null)) {
            returnWrapper = new FindStrategyOnePatternInstance(currPattern, logText, instNum.getValue());
        } else if ((instNum == null) && (maxRegionSize != null) && (regionRegex != null)) {
            returnWrapper = new FindStrategyPtRegions(currPattern, logText, regionRegex.getValue(), replaceTable, maxRegionSize.getValue(), externalFlags);
        } else {
            returnWrapper = new FindStrategyOnePattern(currPattern, logText);
        }
        return returnWrapper;
    }

    @Override
    public MultiLineRegexImpl clone() throws CloneNotSupportedException {
        MultiLineRegexImpl newInst = (MultiLineRegexImpl)super.clone();
        List<String> newRegexLines = new ArrayList<>();
        if ((regexStrings != null) && (!regexStrings.isEmpty())) {
            for (String regexStr : regexStrings) {
                newRegexLines.add(regexStr);
            }
        }
        newInst.setRegexStrings(newRegexLines);
        return newInst;
    }

}
