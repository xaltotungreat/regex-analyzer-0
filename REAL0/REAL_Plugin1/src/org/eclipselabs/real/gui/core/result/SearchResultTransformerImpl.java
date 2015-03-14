package org.eclipselabs.real.gui.core.result;

import java.util.List;

import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;

public class SearchResultTransformerImpl implements ISearchResultTransformer {

    public SearchResultTransformerImpl() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public <R extends IKeyedSearchResult<O>, O extends ISearchResultObject> IDisplayResult transformTextResult(List<R> searchResults, IDisplaySO displaySearchObject, TransformIndexMehod indexCalcMethod, Boolean sortValues) {
        /*IDisplayResult simpleRes = new DisplayResultImpl();
        if ((searchResults != null) && (searchResults.size() > 0)) {
            //simpleRes.setKeyValues(searchResults.get(0));
            List<O> srOList = new ArrayList<O>();
            for(R searchRes : searchResults) {
                srOList.addAll(searchRes.getSRObjects());
            }
            IRealRegex sortRegex = searchResults.get(0).getSortRegex(displaySearchObject.getSortRegexName());
            if (sortRegex != null) {
                Collections.sort(srOList, new SROTextComparator(sortRegex, replaceMap, searchResults.get(0).getRegexFlags()));
            }
            for(O searchRes : srOList) {
                addText(searchRes.getText());
            }
        } else {
            log.error("Constructor null list passed");
        }*/
        return null;
    }

    @Override
    public <R extends IKeyedComplexSearchResult<O, W, X, String>, 
                    O extends IComplexSearchResultObject<W, X, String>, 
                    V extends ISearchObject<W, X>, W extends ISearchResult<X>, 
                    X extends ISearchResultObject> 
                IDisplayResult transformTextResultWithViews(List<R> searchResults, IDisplaySO displaySearchObject,
            TransformIndexMehod indexCalcMethod, Boolean sortValues) {
        // TODO Auto-generated method stub
        return null;
    }

}
