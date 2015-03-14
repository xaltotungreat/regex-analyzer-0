package org.eclipselabs.real.core.searchobject;

import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;

public interface ISearchObjectDateInfo extends Cloneable {

    public String getDateFormat();
    public void setDateFormat(String dateFormat);

    public List<IRealRegex> getRegexList();
    public void setRegexList(List<IRealRegex> regexList);
    
    public ISearchObjectDateInfo clone() throws CloneNotSupportedException;
}
