package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISRRegex;
import org.eclipselabs.real.core.searchresult.resultobject.ISRORegex;

public interface ISORegex extends IKeyedSearchObject<ISRRegex, ISRORegex> {

    public IRealRegex getRegex();
    public void setRegex(IRealRegex regex);
}
