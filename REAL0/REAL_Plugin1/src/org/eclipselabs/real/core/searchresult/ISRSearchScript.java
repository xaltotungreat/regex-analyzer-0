package org.eclipselabs.real.core.searchresult;

import java.util.List;

import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISearchProgressMonitor;
import org.eclipselabs.real.core.searchobject.script.SOContainer;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

public interface ISRSearchScript extends IKeyedComplexSearchResult<
        IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>, 
        ISRComplexRegexView, ISROComplexRegexView, String> {

    public ISearchProgressMonitor getProgressMonitor();
    public void setProgressMonitor(ISearchProgressMonitor newMonitor);
    public String getLogText();
    public void setLogText(String logText);
    
    public List<IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>, 
                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>, 
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> 
        getMainRegexList();
    public void setMainRegexList(List<IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>, 
                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>, 
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> mrList);
    
    public IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISRComplexRegexView, ISROComplexRegexView, String>, 
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>, 
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> 
        getInternalSOByName(String complRegName);
    
    public SOContainer getByName(String soName);
    public void setViewOrder(String...viewNames);
}
