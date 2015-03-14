package org.eclipselabs.real.core.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealRegexGroupImpl extends RealRegexImpl implements IRealRegexGroup {

    private static final Logger log = LogManager.getLogger(RealRegexGroupImpl.class);
    protected volatile List<IRealRegex> regexList = Collections.synchronizedList(new ArrayList<IRealRegex>());
    protected volatile IRealRegex selectedRegex;
    protected volatile Integer selectedPos;
    
    public RealRegexGroupImpl(String aName) {
        super(aName);
        type = RealRegexType.REAL_REGEX_GROUP;
    }

    @Override
    public synchronized IMatcherWrapper getMatcherWrapper(String logText, Map<String, String> replaceTable, Integer externalFlags) {
        IMatcherWrapper result = null;
        if (selectedRegex != null) {
            result = selectedRegex.getMatcherWrapper(logText, replaceTable, externalFlags);
        } else {
            log.warn("getMatcherWrapper selected regex is null returning null");
        }
        return result;
    }

    @Override
    public synchronized String getPatternString(Map<String, String> replaceTable) {
        String patternStr = null;
        if (selectedRegex != null) {
            patternStr = selectedRegex.getPatternString(replaceTable);
        }
        return patternStr;
    }

    @Override
    public synchronized List<IRealRegex> getRegexList() {
        return regexList;
    }

    @Override
    public synchronized void setRegexList(List<IRealRegex> newList) {
        regexList = newList;
        if (regexList != null) {
            selectedPos = -1;
            selectedRegex = null;
            if (!regexList.isEmpty()) {
                selectedRegex = regexList.get(0);
                selectedPos = 0;
            }
        }
    }

    @Override
    public synchronized void addRegex(IRealRegex addRegex) {
        if (regexList != null) {
            if (addRegex != null) {
                regexList.add(addRegex);
                if (regexList.size() == 1) {
                    selectedRegex = regexList.get(0);
                    selectedPos = 0;
                }
            } else {
                log.error("addRegex new regex is null");
            }
        } else {
            log.error("addRegex regex list is null");
        }

    }

    @Override
    public synchronized boolean setSelected(String regexName) {
        boolean result = false;
        if (regexList != null) {
            for (int i = 0; i < regexList.size(); i++) {
                IRealRegex currRegex = regexList.get(i);
                if ((currRegex.getRegexName() != null) && (currRegex.getRegexName().equals(regexName))) {
                    selectedRegex = currRegex;
                    selectedPos = i;
                    result = true;
                    break;
                }
            }
        } else {
            log.error("addRegex regex list is null");
        }
        return result;
    }

    @Override
    public synchronized boolean setSelected(int pos) {
        boolean result = false;
        if (regexList != null) {
            if ((pos >= 0) && (pos < regexList.size())) {
                selectedRegex = regexList.get(pos);
                selectedPos = pos;
                result = true;
            } else {
                log.warn("setSelected incorrect pos " + pos + " size " + regexList.size());
            }
        } else {
            log.error("addRegex regex list is null");
        }
        return result;
    }

    @Override
    public synchronized IRealRegex getSelected() {
        return selectedRegex;
    }

    @Override
    public synchronized int getSelectedPosition() {
        return selectedPos;
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RealRegexGroupImpl [selectedRegex=" + selectedRegex + ", selectedPos=" + selectedPos);
        if (regexList != null) {
            for (IRealRegex currReg : regexList) {
                sb.append("\t" + currReg + "\n");
            }
        } else {
            sb.append(", regexlist=null]");
        }
        return sb.toString();
    }

    @Override
    public synchronized boolean setSelected(List<IRealRegexParam<?>> matchParamList) {
        boolean result = false;
        if (matchParamList != null) {
            for (int i = 0; i < regexList.size(); i++) {
                IRealRegex currRegex = regexList.get(i);
                boolean currRegexMatch = true;
                for (IRealRegexParam<?> currParam : matchParamList) {
                    boolean paramMatch = false;
                    IRealRegexParam<?> regexParam = currRegex.getParameter(currParam.getName());
                    if ((regexParam != null) && (regexParam.getType().equals(currParam.getType()))
                            && (regexParam.getValue().equals(currParam.getValue()))) {
                        paramMatch = true;
                    }
                    if (!paramMatch) {
                        currRegexMatch = false;
                        break;
                    }
                }
                if (currRegexMatch) {
                    selectedRegex = currRegex;
                    selectedPos = i;
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
