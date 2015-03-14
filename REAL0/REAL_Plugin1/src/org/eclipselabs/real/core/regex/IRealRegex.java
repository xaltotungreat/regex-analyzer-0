package org.eclipselabs.real.core.regex;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.util.ITypedObject;

/**
 * This interface is a wrapper for the Pattern object. 
 * It provides the following features:
 * 1. It can be constructed from the configuration file
 * 2. It can have a name
 * 3. It can create a IMatcherWrapper to search for the pattern(s)
 * @author Vadim Korkin
 *
 */
public interface IRealRegex extends Cloneable, ITypedObject<RealRegexType> {
    
    public static String PARAM_NAME_INSTANCE = "instance";
    public static String PARAM_NAME_MAX_REGION_SIZE = "MaxRegionSize";
    public static String PARAM_NAME_REGION_REGEX = "RegionRegex";
    
    public String getRegexName();
    public void setRegexName(String regexName);
    public String getRegexGroup();
    public void setRegexGroup(String regexGroup);
    
    public void putParameter(IRealRegexParam<?> newParam);
    public void putParameters(List<IRealRegexParam<?>> newParam);
    public void removeParameter(String aName, RealRegexParamType aType);
    public IRealRegexParam<?> getParameter(String aName);
    public Collection<IRealRegexParam<?>> getParameters();
    
    public Integer getRegexFlags();
    public void setRegexFlags(Integer flags);
    
    public IMatcherWrapper getMatcherWrapper(String logText, Map<String, String> replaceTable, Integer externalFlags);
    public String getPatternString(Map<String, String> replaceTable);
    
    public IRealRegex clone() throws CloneNotSupportedException;
}
