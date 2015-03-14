package org.eclipselabs.real.core.searchobject;

import java.util.List;

import org.eclipselabs.real.core.searchobject.ref.IRefKeyedSOContainer;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.ISRSearchScript;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

public interface ISOSearchScript extends IKeyedComplexSearchObject<ISRSearchScript, 
        IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>, 
        ISOComplexRegexView,ISRComplexRegexView, ISROComplexRegexView,String>, IRefKeyedSOContainer {

    public String getScriptText();
    public void setScriptText(String scriptText);
    
    public List<IKeyedComplexSearchObject<
        ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String>, 
        ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>, 
        ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> getMainRegexList();
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
    
    
}
