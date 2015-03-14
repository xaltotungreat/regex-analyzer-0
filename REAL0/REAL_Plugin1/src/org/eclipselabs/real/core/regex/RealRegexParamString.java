package org.eclipselabs.real.core.regex;

public class RealRegexParamString extends RealRegexParamImpl<String> {

    RealRegexParamString() {
        super(RealRegexParamType.STRING);
    }
    
    RealRegexParamString(String aName, String aValue) {
        super(RealRegexParamType.STRING, aName, aValue);
    }

}
