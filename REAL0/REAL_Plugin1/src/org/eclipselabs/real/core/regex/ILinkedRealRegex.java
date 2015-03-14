package org.eclipselabs.real.core.regex;

import java.util.List;

public interface ILinkedRealRegex extends IRealRegex {

    public void addRealRegex(IRealRegex newReg);
    public List<IRealRegex> getRegexList();
    public void setRegexList(List<IRealRegex> regexList);
}
