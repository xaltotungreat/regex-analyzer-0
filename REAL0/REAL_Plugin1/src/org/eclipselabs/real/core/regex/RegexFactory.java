package org.eclipselabs.real.core.regex;

public enum RegexFactory {
    INSTANCE;
    
    public ISimpleRegex getSimpleRegex(String aName) {
        return new SimpleRegexImpl(aName);
    }
    
    public IMultiLineRegex getMultiLineRegex(String aName) {
        return new MultiLineRegexImpl(aName);
    }
    
    public ILinkedRealRegex getLinkedRegex(String aName) {
        return new LinkedRealRegexImpl(aName);
    }
    
    public IRealRegexGroup getRegexGroup(String aName) {
        return new RealRegexGroupImpl(aName);
    }
    
    public RealRegexParamInteger getRealRegexIntegerParam() {
        return new RealRegexParamInteger();
    }
    
    public RealRegexParamString getRealRegexStringParam() {
        return new RealRegexParamString();
    }
    
    public RealRegexParamBoolean getRealRegexBooleanParam() {
        return new RealRegexParamBoolean();
    }
    
    public RealRegexParamIRealRegex getRealRegexIRealRegexParam() {
        return new RealRegexParamIRealRegex();
    }
}
