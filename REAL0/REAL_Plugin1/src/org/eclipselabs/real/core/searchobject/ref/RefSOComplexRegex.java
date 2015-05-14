package org.eclipselabs.real.core.searchobject.ref;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

public class RefSOComplexRegex extends RefKeyedComplexSO<
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String,
            ISOComplexRegex> {

    //private static final Logger log = LogManager.getLogger(RefSOComplexRegex.class);
    protected List<RefRealRegex> refMainRegexes;

    public RefSOComplexRegex(SearchObjectType soType, String aName) {
        super(soType, aName);
    }

    public RefSOComplexRegex(RefType aType, SearchObjectType soType, String aName) {
        super(aType, soType, aName);
    }

    public RefSOComplexRegex(SearchObjectType soType, String aName, ISearchObjectGroup<String> aGroup, Map<String, String> aTags) {
        super(soType, aName, aGroup, aTags);
    }

    public void addMainRegex(RefRealRegex newRegex) {
        if (refMainRegexes != null) {
            refMainRegexes.add(newRegex);
        }
    }

    public List<RefRealRegex> getRefMainRegexes() {
        return refMainRegexes;
    }

    public void setRefMainRegexes(List<RefRealRegex> refMainRegexes) {
        this.refMainRegexes = refMainRegexes;
    }

    @Override
    public boolean matchByParameters(ISOComplexRegex obj) {
        boolean matches = super.matchByParameters(obj);
        if (matches) {
            matches = RefUtil.matchRefRealRegex(obj.getMainRegexList(), refMainRegexes);
        }
        return matches;
    }

    @Override
    public Integer addParameters(ISOComplexRegex obj) {
        Integer count =  super.addParameters(obj);
        count += RefUtil.addRefRealRegex(obj.getMainRegexList(), refMainRegexes);
        return count;
    }

    @Override
    public Integer replaceAddParameters(ISOComplexRegex obj) {
        Integer count = super.replaceAddParameters(obj);
        count += RefUtil.replaceAddRefRealRegex(obj.getMainRegexList(), refMainRegexes);
        return count;
    }

    @Override
    public Integer replaceParameters(ISOComplexRegex obj) {
        Integer count = super.replaceParameters(obj);
        count += RefUtil.replaceRefRealRegex(obj.getMainRegexList(), refMainRegexes);
        return count;
    }

    @Override
    public Integer removeParameters(ISOComplexRegex obj) {
        Integer count = super.removeParameters(obj);
        count += RefUtil.removeRefRealRegex(obj.getMainRegexList(), refMainRegexes);
        return count;
    }

    @Override
    public Integer cpAdd(ISOComplexRegex obj) {
        Integer count = super.cpAdd(obj);
        count += RefUtil.cpAddRefRealRegex(obj.getMainRegexList(), refMainRegexes);
        return count;
    }

    @Override
    public Integer cpReplace(ISOComplexRegex obj) {
        Integer count =  super.cpReplace(obj);
        count += RefUtil.cpReplaceRefRealRegex(obj.getMainRegexList(), refMainRegexes);
        return count;
    }

    @Override
    public String toString() {
        return "RefSOComplexRegex [name=" + name + ", group=" + group + ", tags=" + tags + ", refType=" + refType
                + ", refName=" + refName + ", refGroup=" + refGroup + ", refTags=" + refTags
                + ", refReplaceParams=" + refReplaceParams + ", refSortRequests=" + refSortRequests
                + ", refRegexFlags=" + refRegexFlags + ", refDateInfo=" + refDateInfo
                + ", refViewList=" + refViewList + ", refMainRegexes=" + refMainRegexes
                + ", refAcceptanceCriteria=" + refAcceptanceCriteria + "]";
    }


}
