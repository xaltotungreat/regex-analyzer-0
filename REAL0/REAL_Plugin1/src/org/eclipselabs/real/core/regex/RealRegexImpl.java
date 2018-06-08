package org.eclipselabs.real.core.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RealRegexImpl implements IRealRegex {

    private static final Logger log = LogManager.getLogger(RealRegexImpl.class);
    protected RealRegexType type;
    protected String regexName;
    protected String regexGroup;
    protected Integer regexFlags;
    protected Map<String,IRealRegexParam<?>> regexParamMap = Collections.synchronizedMap(new HashMap<String,IRealRegexParam<?>>());

    public RealRegexImpl(String aName) {
        regexName = aName;
    }

    @Override
    public RealRegexType getType() {
        return type;
    }

    @Override
    public String getRegexName() {
        return regexName;
    }

    @Override
    public void setRegexName(String regexName) {
        this.regexName = regexName;
    }

    @Override
    public String getRegexGroup() {
        return regexGroup;
    }

    @Override
    public void setRegexGroup(String regexGroup) {
        this.regexGroup = regexGroup;
    }

    @Override
    public void putParameter(IRealRegexParam<?> newParam) {
        if ((newParam != null) && (newParam.getName() != null)) {
            regexParamMap.put(newParam.getName(), newParam);
        } else {
            log.error("addParameter null value " + newParam);
        }
    }

    @Override
    public void putParameters(List<IRealRegexParam<?>> newParams) {
        if ((newParams != null) && (!newParams.isEmpty())) {
            for (IRealRegexParam<?> curParam : newParams) {
                regexParamMap.put(curParam.getName(), curParam);
            }
        }
    }

    @Override
    public void removeParameter(String aName, RealRegexParamType aType) {
        if (aName != null) {
            IRealRegexParam<?> param = regexParamMap.get(aName);
            if ((param != null) && (param.getType().equals(aType))) {
                regexParamMap.remove(aName);
            }
        } else {
            log.error("removeParameter null Key passed ");
        }
    }

    @Override
    public IRealRegexParam<?> getParameter(String aName) {
        IRealRegexParam<?> returnVal = null;
        if (aName != null) {
            returnVal = regexParamMap.get(aName);
        } else {
            log.error("getParameter null key passed ");
        }
        return returnVal;
    }

    @Override
    public Collection<IRealRegexParam<?>> getParameters() {
        if (regexParamMap != null) {
            return new ArrayList<>(regexParamMap.values());
        } else {
            return null;
        }
    }

    @Override
    public void setParameters(Collection<IRealRegexParam<?>> params) {
        for (IRealRegexParam<?> iRealRegexParam : params) {
            regexParamMap.put(iRealRegexParam.getName(), iRealRegexParam);
        }
    }

    @Override
    public Integer getRegexFlags() {
        return regexFlags;
    }

    @Override
    public void setRegexFlags(Integer regexFlags) {
        this.regexFlags = regexFlags;
    }

    @Override
    public RealRegexImpl clone() throws CloneNotSupportedException {
        return (RealRegexImpl)super.clone();
    }

}
