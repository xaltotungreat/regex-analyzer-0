package org.eclipselabs.real.core.regex;

import java.util.List;

public interface IRealRegexGroup extends IRealRegex {

    public List<IRealRegex> getRegexList();
    public void setRegexList(List<IRealRegex> newList);
    public void addRegex(IRealRegex addRegex);
    
    public boolean setSelected(String regexName);
    public boolean setSelected(int pos);
    public boolean setSelected(List<IRealRegexParam<?>> matchParamList);
    
    public IRealRegex getSelected();
    public int getSelectedPosition();
}
