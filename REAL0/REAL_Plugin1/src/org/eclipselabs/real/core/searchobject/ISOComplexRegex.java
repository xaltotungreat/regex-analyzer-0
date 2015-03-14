package org.eclipselabs.real.core.searchobject;

import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISRComplexRegex;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegex;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

public interface ISOComplexRegex extends IKeyedComplexSearchObject<ISRComplexRegex, ISROComplexRegex,
        ISOComplexRegexView,ISRComplexRegexView, ISROComplexRegexView,String> {
    public List<IRealRegex> getMainRegexList();
    public void setMainRegexList(List<IRealRegex> mrList);
    
}
