package org.eclipselabs.real.core.searchobject.ref;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.ISearchObjectRepository;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

public abstract class RefKeyedSO<T extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> extends RefImpl<T> {

    private static final Logger log = LogManager.getLogger(RefKeyedSO.class);
    protected SearchObjectType refSearchObjectType;
    //protected String name;
    protected ISearchObjectGroup<String> group;
    protected Map<String,String> tags;
    protected String description;

    protected String refName;
    protected ISearchObjectGroup<String> refGroup;
    protected Map<String,String> refTags;

    protected List<RefReplaceParam> refReplaceParams;
    protected List<RefInternalSortRequest> refSortRequests;
    protected List<RefAcceptanceCriterion> refAcceptanceCriteria;
    protected RefParamInteger refRegexFlags;
    protected RefDateInfo refDateInfo;

    public RefKeyedSO(RefType aType, SearchObjectType soType, String aName) {
        super(aType, aName);
        refSearchObjectType = soType;
    }

    public RefKeyedSO(SearchObjectType soType, String aName) {
        this(RefType.CP_ADD, soType, aName);
        name = aName;
    }

    public RefKeyedSO(SearchObjectType soType, String aName, ISearchObjectGroup<String> aGroup, Map<String,String> aTags) {
        this(RefType.CP_ADD, soType, aName);
        name = aName;
        group = aGroup;
        tags = aTags;
    }

    public RefKeyedSO(RefType aType, SearchObjectType soType, String aName, ISearchObjectGroup<String> aGroup, Map<String,String> aTags) {
        this(aType, soType, aName);
        name = aName;
        group = aGroup;
        tags = aTags;
    }

    public Predicate<SearchObjectKey> getSOKeyMatchPredicate() {
        return new Predicate<SearchObjectKey>() {

            @Override
            public boolean test(SearchObjectKey t) {
                boolean matches = true;
                if (matches && (refName != null) && (!refName.equals(t.getSOName()))) {
                    matches = false;
                }

                if (matches && (refGroup != null) && (!refGroup.equals(t.getSOGroup()))) {
                    matches = false;
                }

                if (matches && (refTags != null)) {
                    for (Map.Entry<String, String> currTag : refTags.entrySet()) {
                        if (!t.getSOTags().containsKey(currTag.getKey())) {
                            matches = false;
                            break;
                        } else {
                            if ((t.getSOTags().get(currTag.getKey()) != null) && (!t.getSOTags().get(currTag.getKey()).equals(currTag.getValue()))) {
                                matches = false;
                                break;
                            } else if ((t.getSOTags().get(currTag.getKey()) == null) && (currTag.getValue() != null)){
                                matches = false;
                                break;
                            }
                        }
                    }
                }
                return matches;
            }
        };
    }

    public Predicate<IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> getSearchObjectMatchPredicate() {
        return new Predicate<IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>() {

            @Override
            public boolean test(IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> t) {
                boolean matches = true;
                if (matches && (refSearchObjectType != null) && (!refSearchObjectType.equals(t.getType()))) {
                    matches = false;
                }
                if (matches && (refName != null) && (!refName.equals(t.getSearchObjectName()))) {
                    matches = false;
                }

                if (matches && (refGroup != null) && (!refGroup.equals(t.getSearchObjectGroup()))) {
                    matches = false;
                }

                if (matches && (refTags != null)) {
                    for (Map.Entry<String, String> currTag : refTags.entrySet()) {
                        if (!t.getSearchObjectTags().containsKey(currTag.getKey())) {
                            matches = false;
                            break;
                        } else {
                            if ((t.getSearchObjectTags().get(currTag.getValue()) != null) && (!t.getSearchObjectTags().get(currTag.getValue()).equals(currTag.getValue()))) {
                                matches = false;
                                break;
                            } else if ((t.getSearchObjectTags().get(currTag.getValue()) == null) && (currTag.getValue() != null)){
                                matches = false;
                                break;
                            }
                        }
                    }
                }
                return matches;
            }
        };
    }



    @Override
    public Predicate<? super T> getDefaultMatchPredicate() {
        return new Predicate<IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>() {

            @Override
            public boolean test(IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> t) {
                boolean matches = true;
                if (matches && (refSearchObjectType != null) && (!refSearchObjectType.equals(t.getType()))) {
                    matches = false;
                }
                if (matches && (refName != null) && (!refName.equals(t.getSearchObjectName()))) {
                    matches = false;
                }

                if (matches && (refGroup != null) && (!refGroup.equals(t.getSearchObjectGroup()))) {
                    matches = false;
                }

                if (matches && (refTags != null)) {
                    for (Map.Entry<String, String> currTag : refTags.entrySet()) {
                        if (!t.getSearchObjectTags().containsKey(currTag.getKey())) {
                            matches = false;
                            break;
                        } else {
                            if ((t.getSearchObjectTags().get(currTag.getValue()) != null) && (!t.getSearchObjectTags().get(currTag.getValue()).equals(currTag.getValue()))) {
                                matches = false;
                                break;
                            } else if ((t.getSearchObjectTags().get(currTag.getValue()) == null) && (currTag.getValue() != null)){
                                matches = false;
                                break;
                            }
                        }
                    }
                }
                return matches;
            }
        };
    }

    @Override
    public boolean matchByParameters(T obj) {
        boolean matches = true;

        // match replace params by name
        // a ref to replace params contains a list of params -
        // need to handle them differently
        if (matches && (refReplaceParams != null) && (!refReplaceParams.isEmpty())) {
            List<IReplaceParam<?>> allParams = obj.getCloneParamList();
            for (RefReplaceParam refRP : refReplaceParams) {
                if (RefType.MATCH.equals(refRP.getType())) {
                    // check if the list contains some params before checking the main param list
                    if ((refRP.getValue() == null) || (refRP.getValue().isEmpty())) {
                        log.warn("Null or empty replace param list for ref(continue to the next ref)\n" + refRP);
                        continue;
                    }
                    if (allParams == null) {
                        log.warn("matchByParameters replace param list is null, ref not matched (no more matching attempted)\n" + refRP);
                        matches = false;
                        break;
                    }
                    boolean rpMatch = true;
                    for (IReplaceParam<?> rpRef : refRP.getValue()) {
                        boolean currRefParamMatch = false;
                        for (IReplaceParam<?> rpObj : allParams) {
                            if ((rpObj.getName() != null) && (rpObj.getName().equals(rpRef.getName()))) {
                                currRefParamMatch = true;
                                break;
                            }
                        }
                        if (!currRefParamMatch) {
                            rpMatch = false;
                            break;
                        }
                    }

                    if (!rpMatch) {
                        matches = false;
                        log.debug("matchByParameters ReplaceParam not matched " + refRP + " obj name=" + obj.getSearchObjectName());
                        break;
                    }
                }
            }
        }

        // match sort requests by name and type
        if (matches && (refSortRequests != null) && (!refSortRequests.isEmpty())) {
            List<IInternalSortRequest> allObjSortReqs = obj.getSortRequestList();
            for (RefInternalSortRequest refSortReq : refSortRequests) {
                if (RefType.MATCH.equals(refSortReq.getType())) {
                    if (allObjSortReqs == null) {
                        log.warn("matchByParameters sort requests list is null, ref not matched (no more matching attempted)\n" + refSortReq);
                        matches = false;
                        break;
                    }
                    boolean sortReqMatch = false;
                    if (refSortReq.getPosition() != null) {
                        if ((refSortReq.getPosition() >= 0) && (refSortReq.getPosition() < allObjSortReqs.size())) {
                            sortReqMatch = refSortReq.defaultMatch(allObjSortReqs.get(refSortReq.getPosition().intValue()));
                        } else {
                            log.warn("matchByParameters incorrect position for ref sort request " + refSortReq);
                        }
                    } else {
                        for (IInternalSortRequest sortReq : allObjSortReqs) {
                            if (refSortReq.defaultMatch(sortReq)) {
                                sortReqMatch = true;
                                break;
                            }
                        }
                    }
                    if (!sortReqMatch) {
                        matches = false;
                        log.debug("matchByParameters SortRequest not matched " + refSortReq + " obj name=" + obj.getSearchObjectName());
                        break;
                    }
                }
            }
        }

        // match acceptance criteria by name and type
        if (matches && (refAcceptanceCriteria != null) && (!refAcceptanceCriteria.isEmpty())) {
            List<IAcceptanceCriterion> allObjAC = obj.getAcceptanceList();
            for (RefAcceptanceCriterion refAC : refAcceptanceCriteria) {
                if (RefType.MATCH.equals(refAC.getType())) {
                    if (allObjAC == null) {
                        log.warn("matchByParameters sort requests list is null, ref not matched (no more matching attempted)\n" + refAC);
                        matches = false;
                        break;
                    }
                    boolean acMatch = false;
                    if (refAC.getPosition() != null) {
                        if ((refAC.getPosition() >= 0) && (refAC.getPosition() < allObjAC.size())) {
                            acMatch = refAC.defaultMatch(allObjAC.get(refAC.getPosition().intValue()));
                        } else {
                            log.warn("matchByParameters incorrect position for ref acceptance criterion " + refAC);
                        }
                    } else {
                        for (IAcceptanceCriterion ac : allObjAC) {
                            if (refAC.defaultMatch(ac)) {
                                acMatch = true;
                                break;
                            }
                        }
                    }
                    if (!acMatch) {
                        matches = false;
                        log.debug("matchByParameters AcceptanceCriterion not matched " + refAC + " obj name=" + obj.getSearchObjectName());
                        break;
                    }
                }
            }
        }

        if (matches && (refRegexFlags != null) && (RefType.MATCH.equals(refRegexFlags.getType()))) {
            if (refRegexFlags.getValue() != null) {
                if (!refRegexFlags.getValue().equals(obj.getRegexFlags())) {
                    matches = false;
                    log.debug("matchByParameters RegexFlags not matched " + refRegexFlags + " obj name=" + obj.getSearchObjectName());
                }
            } else {
                log.error("matchByParameters param value is null " + refRegexFlags);
            }
        }

        if (matches && (refDateInfo != null) && (RefType.MATCH.equals(refDateInfo.getType()))) {
            log.warn("matchByParameters type match for DateInfo not supported refName=" + getName());
        }
        return matches;
    }

    @Override
    public Integer addParameters(T obj) {
        Integer count = 0;

        // replace params
        if ((refReplaceParams != null) && (!refReplaceParams.isEmpty())) {
            for (RefReplaceParam currRefRP : refReplaceParams) {
                if (RefType.ADD.equals(currRefRP.getType())) {
                    if (currRefRP.getValue() == null) {
                        log.error("addParameters null value for ref(cannot process this param)\n" + currRefRP);
                        continue;
                    }
                    for (IReplaceParam<?> currParam : currRefRP.getValue()) {
                        if (!obj.paramExists(currParam.getKey())) {
                            obj.addParam(currParam);
                            count++;
                        } else {
                            log.warn("addParameters trying to add already existing parameter " + currParam);
                        }
                    }
                }
            }
        }

        /* sort requests
         * position takes priority over default match.
         */
        if ((refSortRequests != null) && (!refSortRequests.isEmpty())) {
            List<IInternalSortRequest> objSortRequests = obj.getSortRequestList();
            for (RefInternalSortRequest refSortReq : refSortRequests) {
                if (RefType.ADD.equals(refSortReq.getType())) {
                    if (refSortReq.getValue() == null) {
                        log.error("addParameters null value for ref(cannot process this param)\n" + refSortReq);
                        continue;
                    }
                    if (objSortRequests == null) {
                        log.warn("addParameters sort requests list is null, ref not added (no more refs added)\n" + refSortReq);
                        break;
                    }
                    if (refSortReq.getPosition() != null) {
                        if ((refSortReq.getPosition() >= 0)
                                && (refSortReq.getPosition() <= objSortRequests.size())) {
                            objSortRequests.add(refSortReq.getPosition(), refSortReq.getValue());
                        } else {
                            log.warn("addParameters Incorrect sort request position (adding to the end) " + refSortReq.getName() + " " + refSortReq.getPosition() + " size=" + objSortRequests.size());
                            objSortRequests.add(refSortReq.getValue());
                        }
                        count++;
                    } else {
                        /*
                         * Do not add the same AC if the position is null
                         */
                        long matchACCount = objSortRequests.stream().filter(refSortReq.getDefaultMatchPredicate()).count();
                        if (matchACCount > 0) {
                            log.warn("addParameters found a match for ISR (don't add) name=" + refSortReq.getValue().getName()
                                    + " type=" + refSortReq.getValue().getType());
                        } else {
                            objSortRequests.add(refSortReq.getValue());
                            count++;
                        }
                    }
                }
            }
        }

        /* acceptance criteria
         * position takes priority over default match.
         * If the position is not null even the same AC may be added
         * If the position is null then the same (by equals) AC will not be added
         */
        if ((refAcceptanceCriteria != null) && (!refAcceptanceCriteria.isEmpty())) {
            List<IAcceptanceCriterion> objACList = obj.getAcceptanceList();
            for (RefAcceptanceCriterion refAC : refAcceptanceCriteria) {
                if (RefType.ADD.equals(refAC.getType())) {
                    if (refAC.getValue() == null) {
                        log.error("addParameters null value for ref(cannot process this param)\n" + refAC);
                        continue;
                    }
                    if (objACList == null) {
                        log.warn("addParameters acceptance criteria list is null, ref not added (no more refs added)\n" + refAC);
                        break;
                    }

                    if (refAC.getPosition() != null) {
                        if ((refAC.getPosition() >= 0) && (refAC.getPosition() <= objACList.size())) {
                            objACList.add(refAC.getPosition(), refAC.getValue());
                        } else {
                            log.warn("addParameters Incorrect Acceptance criterion position (adding to the end) " + refAC.getName() + " " +  refAC.getPosition() + " size=" + objACList.size());
                            objACList.add(refAC.getValue());
                        }
                        count++;
                    } else {
                        /*
                         * Do not add the same AC if the position is null
                         */
                        long matchACCount = objACList.stream().filter(refAC.getDefaultMatchPredicate()).count();
                        if (matchACCount > 0) {
                            log.warn("addParameters found a match for AC (don't add) name=" + refAC.getValue().getName()
                                    + " type=" + refAC.getValue().getType());
                        } else {
                            objACList.add(refAC.getValue());
                            count++;
                        }
                    }
                }
            }
        }

        // regex flags
        if ((refRegexFlags != null) && (RefType.ADD.equals(refRegexFlags.getType()))) {
            if (refRegexFlags.getValue() != null) {
                Integer resultFlags = obj.getRegexFlags() | refRegexFlags.getValue();
                obj.setRegexFlags(resultFlags);
                count++;
            } else {
                log.error("addParameters param value is null " + refRegexFlags);
            }
        }

        // the date info is added if none existed before
        if ((refDateInfo != null) && (RefType.ADD.equals(refDateInfo.getType()))
                && (obj.getDateInfo() == null) && (refDateInfo.getValue() != null)) {
            obj.setDateInfo(refDateInfo.getValue());
            count++;
            if (refDateInfo.getValue() == null) {
                log.warn("addParameters date info value is null " + refDateInfo);
            }
        }
        return count;
    }

    @Override
    public Integer replaceAddParameters(T obj) {
        Integer count = 0;
        // replacing name, group and tags
        obj.setSearchObjectName(name);
        obj.setSearchObjectGroup(group);
        obj.setSearchObjectTags(tags);

        /* replace params
         *
         */
        if ((refReplaceParams != null) && (!refReplaceParams.isEmpty())) {
            for (RefReplaceParam currRefRP : refReplaceParams) {
                if (RefType.REPLACE_ADD.equals(currRefRP.getType())) {
                    if (currRefRP.getValue() == null) {
                        log.error("replaceAddParameters null value for ref(cannot process this param)\n" + currRefRP);
                        continue;
                    }
                    for (IReplaceParam<?> currParam : currRefRP.getValue()) {
                        obj.addParam(currParam);
                        count++;
                    }
                }
            }
        }

        /* sort requests
         * position takes priority over default match.
         */
        if ((refSortRequests != null) && (!refSortRequests.isEmpty())) {
            List<IInternalSortRequest> objSortRequests = obj.getSortRequestList();
            for (RefInternalSortRequest refSortReq : refSortRequests) {
                if (RefType.REPLACE_ADD.equals(refSortReq.getType())) {
                    if (refSortReq.getValue() == null) {
                        log.error("replaceAddParameters null value for ref(cannot process this param)\n" + refSortReq);
                        continue;
                    }
                    if (objSortRequests == null) {
                        log.warn("replaceAddParameters sort requests list is null, ref not replaced/added (no more refs replaced/added)\n" + refSortReq);
                        break;
                    }
                    if (refSortReq.getPosition() != null) {
                        if ((refSortReq.getPosition() >= 0) && (refSortReq.getPosition() < objSortRequests.size())) {
                            objSortRequests.set(refSortReq.getPosition(), refSortReq.getValue());
                        } else {
                            log.error("replaceAddParameters Incorrect sort request position add to the end " + refSortReq);
                            objSortRequests.add(refSortReq.getValue());
                        }
                        count++;
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        for (int i = 0; i < objSortRequests.size(); i++) {
                            if (refSortReq.defaultMatch(objSortRequests.get(i))) {
                                setPos = i;
                                break;
                            }
                        }

                        if (setPos > -1) {
                            objSortRequests.set(setPos, refSortReq.getValue());
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.warn("replaceAddParameters add to the end " + refSortReq);
                            objSortRequests.add(refSortReq.getValue());
                        }
                    }
                }
            }
        }

        /* acceptance criteria
         * position takes priority over default match.
         */
        if ((refAcceptanceCriteria != null) && (!refAcceptanceCriteria.isEmpty())) {
            List<IAcceptanceCriterion> objACList = obj.getAcceptanceList();
            for (RefAcceptanceCriterion refAC : refAcceptanceCriteria) {
                if (RefType.REPLACE_ADD.equals(refAC.getType())) {
                    if (refAC.getValue() == null) {
                        log.error("replaceAddParameters null value for ref(cannot process this param)\n" + refAC);
                        continue;
                    }
                    if (objACList == null) {
                        log.warn("replaceAddParameters acceptance criteria list is null, ref not replaced/added (no more refs replaced/added)\n" + refAC);
                        break;
                    }
                    if (refAC.getPosition() != null) {
                        if ((refAC.getPosition() >= 0) && (refAC.getPosition() < objACList.size())) {
                            objACList.set(refAC.getPosition(), refAC.getValue());
                        } else {
                            log.error("replaceAddParameters Incorrect acceptance criteria position add to the end " + refAC);
                            objACList.add(refAC.getValue());
                        }
                        count++;
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        for (int i = 0; i < objACList.size(); i++) {
                            if (refAC.defaultMatch(objACList.get(i))) {
                                setPos = i;
                                break;
                            }
                        }
                        if (setPos > -1) {
                            objACList.set(setPos, refAC.getValue());
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.warn("replaceAddParameters acceptance criteria not found add to the end " + refAC);
                            objACList.add(refAC.getValue());
                        }
                    }
                }
            }
        }

        // here no sense to replace regex flags consequently - add them
        if ((refRegexFlags != null) && (RefType.REPLACE_ADD.equals(refRegexFlags.getType()))) {
            if (refRegexFlags.getValue() != null) {
                Integer resultFlags = obj.getRegexFlags() | refRegexFlags.getValue();
                obj.setRegexFlags(resultFlags);
                count++;
            } else {
                log.error("replaceAddParameters param value is null " + refRegexFlags);
            }
        }

        // the date info is replaced
        if ((refDateInfo != null) && (RefType.REPLACE_ADD.equals(refDateInfo.getType()))
                && (refDateInfo.getValue() != null)) {
            obj.setDateInfo(refDateInfo.getValue());
            count++;
            if (refDateInfo.getValue() == null) {
                log.warn("replaceAddParameters date info value is null " + refDateInfo);
            }
        }
        return count;
    }

    @Override
    public Integer replaceParameters(T obj) {
        Integer count = 0;
        // replacing name, group and tags
        obj.setSearchObjectName(name);
        obj.setSearchObjectGroup(group);
        obj.setSearchObjectTags(tags);
        if ((refReplaceParams != null) && (!refReplaceParams.isEmpty())) {
            for (RefReplaceParam currRefRP : refReplaceParams) {
                if (RefType.REPLACE.equals(currRefRP.getType())) {
                    if (currRefRP.getValue() == null) {
                        log.error("replaceParameters null value for ref(cannot process this param)\n" + currRefRP);
                        continue;
                    }
                    for (IReplaceParam<?> currParam : currRefRP.getValue()) {
                        if (obj.paramExists(currParam.getKey())) {
                            obj.addParam(currParam);
                            count++;
                        } else {
                            log.error("replaceParameters trying to replace a non-existing parameter " + currParam);
                        }
                    }
                }
            }
        }

        if ((refSortRequests != null) && (!refSortRequests.isEmpty())) {
            List<IInternalSortRequest> objSortRequests = obj.getSortRequestList();
            for (RefInternalSortRequest refSortReq : refSortRequests) {
                if (RefType.REPLACE.equals(refSortReq.getType())) {
                    if (refSortReq.getValue() == null) {
                        log.error("replaceParameters null value for ref(cannot process this param)\n" + refSortReq);
                        continue;
                    }
                    if (objSortRequests == null) {
                        log.warn("replaceParameters sort requests list is null, ref not replaced (no more refs replaced)\n" + refSortReq);
                        break;
                    }
                    if (refSortReq.getPosition() != null) {
                        if ((refSortReq.getPosition() >= 0) && (refSortReq.getPosition() < objSortRequests.size())) {
                            objSortRequests.set(refSortReq.getPosition(), refSortReq.getValue());
                            count++;
                        } else {
                            log.error("replaceParameters Incorrect sort request position cannot replace " + refSortReq);
                        }
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        for (int i = 0; i < objSortRequests.size(); i++) {
                            if (refSortReq.defaultMatch(objSortRequests.get(i))) {
                                setPos = i;
                                break;
                            }
                        }

                        if (setPos > -1) {
                            objSortRequests.set(setPos, refSortReq.getValue());
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.error("replaceParameters not matched " + refSortReq);
                        }
                    }
                }
            }
        }

        if ((refAcceptanceCriteria != null) && (!refAcceptanceCriteria.isEmpty())) {
            List<IAcceptanceCriterion> objACList = obj.getAcceptanceList();
            for (RefAcceptanceCriterion refAC : refAcceptanceCriteria) {
                if (RefType.REPLACE.equals(refAC.getType())) {
                    if (refAC.getValue() == null) {
                        log.error("replaceParameters null value for ref(cannot process this param)\n" + refAC);
                        continue;
                    }
                    if (objACList == null) {
                        log.warn("replaceParameters acceptance criteria list is null, ref not replaced (no more refs replaced)\n" + refAC);
                        break;
                    }
                    if (refAC.getPosition() != null) {
                        if ((refAC.getPosition() >= 0) && (refAC.getPosition() < objACList.size())) {
                            objACList.set(refAC.getPosition(), refAC.getValue());
                            count++;
                        } else {
                            log.error("replaceParameters Incorrect acceptance criteria position cannot replace " + refAC);
                        }
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        for (int i = 0; i < objACList.size(); i++) {
                            if (refAC.defaultMatch(objACList.get(i))) {
                                setPos = i;
                                break;
                            }
                        }
                        if (setPos > -1) {
                            objACList.set(setPos, refAC.getValue());
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.error("replaceParameters not matched " + refAC);
                        }
                    }
                }
            }
        }

        // it makes no sense to replace regex flags - no action

        // the date info may be replaced
        if ((refDateInfo != null) && (RefType.REPLACE.equals(refDateInfo.getType()))) {
            if (obj.getDateInfo() != null) {
                obj.setDateInfo(refDateInfo.getValue());
                count++;
            } else {
                log.warn("replaceParameters dateInfo doesn't exist cannot replace");
            }
            if (refDateInfo.getValue() == null) {
                log.warn("replaceParameters date info value is null " + refDateInfo);
            }
        }
        return count;
    }

    @Override
    public Integer removeParameters(T obj) {
        Integer count = 0;
        if ((refReplaceParams != null) && (!refReplaceParams.isEmpty())) {
            for (RefReplaceParam currRefRP : refReplaceParams) {
                if (RefType.REMOVE.equals(currRefRP.getType())) {
                    if (currRefRP.getValue() == null) {
                        log.error("removeParameters null value for ref(cannot process this param)\n" + currRefRP);
                        continue;
                    }
                    for (IReplaceParam<?> rp : currRefRP.getValue()) {
                        if (obj.removeParam(rp.getKey())) {
                            count++;
                        } else {
                            log.warn("removeParameters replace param not removed " + rp);
                        }
                    }
                }
            }
        }

        if ((refSortRequests != null) && (!refSortRequests.isEmpty())) {
            List<IInternalSortRequest> objSortRequests = obj.getSortRequestList();
            for (RefInternalSortRequest refSortReq : refSortRequests) {
                if (RefType.REMOVE.equals(refSortReq.getType())) {
                    if (objSortRequests == null) {
                        log.warn("removeParameters sort requests list is null, ref not removed (no more refs removed)\n" + refSortReq);
                        break;
                    }
                    if (refSortReq.getPosition() != null) {
                        if ((refSortReq.getPosition() >= 0) && (refSortReq.getPosition() < objSortRequests.size())) {
                            objSortRequests.remove(refSortReq.getPosition().intValue());
                            count++;
                        } else {
                            log.error("removeParameters Incorrect sort request position cannot remove " + refSortReq);
                        }
                    } else {
                        int setPos = -1;
                        boolean replaced = false;
                        for (int i = 0; i < objSortRequests.size(); i++) {
                            if (refSortReq.defaultMatch(objSortRequests.get(i))) {
                                setPos = i;
                                break;
                            }
                        }

                        if (setPos > -1) {
                            objSortRequests.remove(setPos);
                            replaced = true;
                            count++;
                        }
                        if (!replaced) {
                            log.error("removeParameters not matched " + refSortReq);
                        }
                    }
                }
            }
        }

        if ((refAcceptanceCriteria != null) && (!refAcceptanceCriteria.isEmpty())) {
            List<IAcceptanceCriterion> objACList = obj.getAcceptanceList();
            for (RefAcceptanceCriterion refAC : refAcceptanceCriteria) {
                if (RefType.REMOVE.equals(refAC.getType())) {
                    if (objACList == null) {
                        log.warn("removeParameters acceptance criteria list is null, ref not removed (no more refs removed)\n" + refAC);
                        break;
                    }
                    if (refAC.getPosition() != null) {
                        if ((refAC.getPosition() >= 0) && (refAC.getPosition() < objACList.size())) {
                            objACList.remove(refAC.getPosition().intValue());
                            count++;
                        } else {
                            log.error("removeParameters Incorrect acceptance criterion position cannot remove " + refAC);
                        }
                    } else {
                        int setPos = -1;
                        boolean removed = false;
                        for (int i = 0; i < objACList.size(); i++) {
                            if (refAC.defaultMatch(objACList.get(i))) {
                                setPos = i;
                                break;
                            }
                        }

                        if (setPos > -1) {
                            objACList.remove(setPos);
                            removed = true;
                            count++;
                        }
                        if (!removed) {
                            log.error("removeParameters not matched " + refAC);
                        }
                    }
                }
            }
        }

        // remove the flags by values
        if ((refRegexFlags != null) && (RefType.REMOVE.equals(refRegexFlags.getType()))) {
            if (refRegexFlags.getValue() != null) {
                Integer resultFlags = obj.getRegexFlags() & (refRegexFlags.getValue() ^ Integer.MAX_VALUE);
                obj.setRegexFlags(resultFlags);
                count++;
            } else {
                log.error("removeParameters param value is null " + refRegexFlags);
            }
        }

        // even though it may be unusual the date info may also be removed
        if ((refDateInfo != null) && (RefType.REMOVE.equals(refDateInfo.getType()))) {
            log.warn("removeParameters RefDateInfo with type REMOVE. Setting dateInfo to null");
            obj.setDateInfo(null);
            count++;
        }
        return count;
    }

    /**
     * This method exists for compound parameters that themselves contain parameters
     * In this case the compound parameter class overrides the method resolve
     * and returns a new object.
     * @return the number of compound parameters added
     */
    @Override
    public Integer cpAdd(T obj) {
        return 0;
    }

    /**
     * This method exists for compound parameters that themselves contain parameters
     * In this case the compound parameter class overrides the method resolve
     * and returns a new object.
     * @return the number of compound parameters replaced
     */
    @Override
    public Integer cpReplace(T obj) {
        return 0;
    }

    public T resolve(ISearchObjectRepository rep) {
        List<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> filteredList = rep.getValues(getSOKeyMatchPredicate());
        T matchedObj = null;
        if ((filteredList != null) && (!filteredList.isEmpty())) {
            Iterator<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> filteredIter = filteredList.iterator();
            while (matchedObj == null) {
                if (filteredIter.hasNext()) {
                    IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> currSO = filteredIter.next();
                    if (currSO.getType().equals(refSearchObjectType)) {
                        log.debug("CurrSo Type=" + currSO.getType() + " refSOType=" + refSearchObjectType);
                        log.debug("CurrSo name=" + currSO.getSearchObjectName() + " group=" + currSO.getSearchObjectGroup());
                        T currObj = (T)currSO;
                        if (matchByParameters(currObj)) {
                            log.debug("Ref \n" + this + "\nMatched \n" + currObj);
                            matchedObj = currObj;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            if (matchedObj != null) {
                return resolve(matchedObj);
            } else {
                log.warn("No match by parameters found for " + this);
            }
        } else {
            log.warn("No match by predicate found for " + this);
        }
        return null;
    }

    @Override
    protected T getClone(T obj) {
        T clonedObj = null;
        try {
            clonedObj = (T)obj.clone();
        } catch (CloneNotSupportedException e) {
            log.error("Clone should be supported",e);
        }
        return clonedObj;
    }

    public SearchObjectType getRefSearchObjectType() {
        return refSearchObjectType;
    }

    public void setRefSearchObjectType(SearchObjectType refSearchObjectType) {
        this.refSearchObjectType = refSearchObjectType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public ISearchObjectGroup<String> getGroup() {
        return group;
    }

    public void setGroup(ISearchObjectGroup<String> group) {
        this.group = group;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public ISearchObjectGroup<String> getRefGroup() {
        return refGroup;
    }

    public void setRefGroup(ISearchObjectGroup<String> refGroup) {
        this.refGroup = refGroup;
    }

    public Map<String, String> getRefTags() {
        return refTags;
    }

    public void setRefTags(Map<String, String> refTags) {
        this.refTags = refTags;
    }

    public List<RefReplaceParam> getRefReplaceParams() {
        return refReplaceParams;
    }

    public void setRefReplaceParams(List<RefReplaceParam> refReplaceParams) {
        this.refReplaceParams = refReplaceParams;
    }

    public List<RefInternalSortRequest> getRefSortRequests() {
        return refSortRequests;
    }

    public void setRefSortRequests(List<RefInternalSortRequest> refSortRequests) {
        this.refSortRequests = refSortRequests;
    }

    public List<RefAcceptanceCriterion> getRefAcceptanceCriteria() {
        return refAcceptanceCriteria;
    }

    public void setRefAcceptanceCriteria(List<RefAcceptanceCriterion> refAcceptanceCriteria) {
        this.refAcceptanceCriteria = refAcceptanceCriteria;
    }

    public RefParamInteger getRefRegexFlags() {
        return refRegexFlags;
    }

    public void setRefRegexFlags(RefParamInteger refRegexFlags) {
        this.refRegexFlags = refRegexFlags;
    }

    public RefDateInfo getRefDateInfo() {
        return refDateInfo;
    }

    public void setRefDateInfo(RefDateInfo refDateInfo) {
        this.refDateInfo = refDateInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((tags == null) ? 0 : tags.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RefKeyedSO<?> other = (RefKeyedSO<?>) obj;
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (tags == null) {
            if (other.tags != null) {
                return false;
            }
        } else if (!tags.equals(other.tags)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RefKeyedSO [name=" + name + ", group=" + group + ", tags=" + tags + "]";
    }

}
