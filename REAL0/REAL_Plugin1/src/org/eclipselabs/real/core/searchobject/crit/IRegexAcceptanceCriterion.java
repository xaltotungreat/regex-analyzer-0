package org.eclipselabs.real.core.searchobject.crit;

import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;

public interface IRegexAcceptanceCriterion extends IAcceptanceCriterion {

    public List<IRealRegex> getAcceptanceRegex();
    public void setAcceptanceRegex(List<IRealRegex> acceptanceRegex);
}
