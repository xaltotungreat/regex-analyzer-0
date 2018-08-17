package org.eclipselabs.real.core.searchresult.sort;

import java.util.Comparator;
import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchresult.IComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.SROTextComparator;
import org.eclipselabs.real.core.searchresult.resultobject.SROViewComparator;

public class RegexComplexSortRequest<Q> extends RegexSortRequest implements IRegexComplexSortRequest<Q> {

    //private static final Logger log = LogManager.getLogger(RegexComplexSortRequest.class);
    protected Q viewName;

    public RegexComplexSortRequest() {
    }

    public RegexComplexSortRequest(String aName) {
        super(aName);
    }

    public RegexComplexSortRequest(SortingApplicability appl, List<IRealRegex> sortList, String aName) {
        super(appl, sortList, aName);
    }

    public RegexComplexSortRequest(SortingApplicability appl, List<IRealRegex> sortList, String aName, Q vName) {
        super(appl, sortList, aName);
        viewName = vName;
    }

    public RegexComplexSortRequest(RegexComplexSortRequest<Q> copyObj) {
        super(copyObj);
        viewName = copyObj.getViewName();
    }

    @Override
    public RegexComplexSortRequest<Q> clone() throws CloneNotSupportedException {
        RegexComplexSortRequest<Q> newInst = (RegexComplexSortRequest<Q>)super.clone();
        newInst.viewName = this.viewName;
        return newInst;
    }

    //@Override
    public <R extends IComplexSearchResult<O,W,X,Q>,
                        O extends IComplexSearchResultObject<W,X,Q>,
                        V extends ISearchObject<W,X>,W extends ISearchResult<X>, X extends ISearchResultObject>
            Comparator<O> getComparator(R searchRes) {
        Comparator<O> resComp = null;
        if ((searchRes != null) && (searchRes.getSRObjects() != null) && (!searchRes.getSRObjects().isEmpty())) {
            if (viewName != null) {
                resComp = new SROViewComparator<>(sortRegexList, searchRes.getCachedReplaceTable(), viewName, regexFlags);
            } else {
                resComp = new SROTextComparator<>(sortRegexList, searchRes.getCachedReplaceTable(), regexFlags);
            }
        }
        return resComp;
    }

    @Override
    public Q getViewName() {
        return viewName;
    }

    @Override
    public void setViewName(Q viewName) {
        this.viewName = viewName;
    }

    @Override
    public String toString() {
        return "RegexComplexSortRequest [name=" + name + ", type=" + type + ", sortApplicability=" + sortApplicability
                + ", sortRegexList=" + sortRegexList + ", regexFlags=" + regexFlags + ", viewName=" + viewName + "]";
    }

}
