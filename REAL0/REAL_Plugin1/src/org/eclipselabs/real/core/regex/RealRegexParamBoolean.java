package org.eclipselabs.real.core.regex;

public class RealRegexParamBoolean extends RealRegexParamImpl<Boolean> {

    RealRegexParamBoolean() {
        super(RealRegexParamType.BOOLEAN);
    }
    
    RealRegexParamBoolean(String aName, String aValue) {
        super(RealRegexParamType.BOOLEAN, aName, Boolean.parseBoolean(aValue));
    }
    
    RealRegexParamBoolean(String aName, Boolean aValue) {
        super(RealRegexParamType.BOOLEAN, aName, aValue);
    }

}
