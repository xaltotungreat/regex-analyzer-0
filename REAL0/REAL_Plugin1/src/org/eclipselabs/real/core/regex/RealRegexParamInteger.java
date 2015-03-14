package org.eclipselabs.real.core.regex;

public class RealRegexParamInteger extends RealRegexParamImpl<Integer> {

    RealRegexParamInteger() {
        super(RealRegexParamType.INTEGER);
    }
    
    RealRegexParamInteger(String aName, String strValue) throws NumberFormatException {
        super(RealRegexParamType.INTEGER, aName, Integer.parseInt(strValue));
    }
    
    RealRegexParamInteger(String aName, Integer intValue) {
        super(RealRegexParamType.INTEGER, aName, intValue);
    }

}
