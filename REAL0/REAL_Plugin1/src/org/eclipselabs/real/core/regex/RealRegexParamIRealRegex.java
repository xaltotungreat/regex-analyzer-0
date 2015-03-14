package org.eclipselabs.real.core.regex;

public class RealRegexParamIRealRegex extends RealRegexParamImpl<IRealRegex> {

    RealRegexParamIRealRegex() {
        super(RealRegexParamType.REGEX);
    }
    
    RealRegexParamIRealRegex(String aName, IRealRegex aValue) {
        super(RealRegexParamType.REGEX, aName, aValue);
    }

}
