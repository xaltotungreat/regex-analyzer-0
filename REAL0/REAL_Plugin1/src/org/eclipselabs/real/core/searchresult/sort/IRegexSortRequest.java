package org.eclipselabs.real.core.searchresult.sort;

import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;

public interface IRegexSortRequest extends IInternalSortRequest {

    public List<IRealRegex> getSortRegexList();
    public void setSortRegexList(List<IRealRegex> sortRegexList);

    public int getRegexFlags();
    public void setRegexFlags(int regexFlags);
}
