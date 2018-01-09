package org.eclipselabs.real.core.searchobject.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISOSearchScript;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.ISearchObjectRepository;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public class RefSOSearchScript extends RefKeyedComplexSO<ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String, ISOSearchScript> implements IRefKeyedSOContainer {

    private static final Logger log = LogManager.getLogger(RefSOSearchScript.class);
    protected RefParamString refScriptText;
    protected List<RefSOComplexRegex> refKeyedComplexSOList = new ArrayList<>();

    protected IRefKeyedSOContainer refContainer = new RefKeyedSOContainerImpl();

    public RefSOSearchScript(SearchObjectType soType, String aName) {
        super(soType, aName);
    }

    public RefSOSearchScript(RefType aType, SearchObjectType soType, String aName) {
        super(aType, soType, aName);
    }

    public RefSOSearchScript(SearchObjectType soType, String aName, ISearchObjectGroup<String> aGroup, Map<String, String> aTags) {
        super(soType, aName, aGroup, aTags);
    }

    @Override
    public boolean matchByParameters(ISOSearchScript obj) {
        boolean matches = super.matchByParameters(obj);
        if (matches && (refScriptText != null) && (RefType.MATCH.equals(refScriptText.getType()))) {
            if ((refScriptText.getValue() != null) && (!refScriptText.getValue().equals(obj.getScriptText()))) {
                matches = false;
            }
        }
        if (matches && (refKeyedComplexSOList != null) && (!refKeyedComplexSOList.isEmpty())) {
            List<ISOComplexRegex> objRegexList = obj.getMainRegexList();
            for (RefSOComplexRegex refKeyedComplexSO : refKeyedComplexSOList) {
                if (matches && (RefType.MATCH.equals(refKeyedComplexSO.getType()))) {
                    if (objRegexList == null) {
                        /* there is at least one MATCH parameter but the regex list is null
                         * assign matches = false and leave the loop
                         */
                        matches = false;
                        log.warn("matchByParameters objRegexListis null the ref is not matched (no more matching attempted)\n" + refKeyedComplexSO);
                        break;
                    }
                    for (ISOComplexRegex currObjSO : objRegexList) {
                        if (refKeyedComplexSO.defaultMatch(currObjSO)) {
                            matches = refKeyedComplexSO.matchByParameters(currObjSO);
                        }
                    }
                    if (!matches) {
                        log.debug("matchByParameters RefKeyedComplexSO not matched " + refKeyedComplexSO.getName());
                        break;
                    }
                } else {
                    // just in case
                    break;
                }
            }
        }
        return matches;
    }

    @Override
    public Integer addParameters(ISOSearchScript obj) {
        Integer count =  super.addParameters(obj);
        if ((refScriptText != null) && (RefType.ADD.equals(refScriptText.getType()))) {
            // replace the script text
            if (refScriptText.getValue() != null) {
                StringBuilder tmpBuffer = new StringBuilder();
                if (obj.getScriptText() != null) {
                    tmpBuffer.append(obj.getScriptText());
                }
                tmpBuffer.append(refScriptText.getValue());
                obj.setScriptText(tmpBuffer.toString());
                count++;
            }
        }

        if ((refKeyedComplexSOList != null) && (!refKeyedComplexSOList.isEmpty())) {
            /* get a reference to the main list
             * no null check is necessary because this list must always be not null
             */
            List<ISOComplexRegex> objRegexList = obj.getMainRegexList();
            for (RefSOComplexRegex refKeyedComplexSO : refKeyedComplexSOList) {
                if (RefType.ADD.equals(refKeyedComplexSO.getType())) {
                    if (objRegexList == null) {
                        /* there is at least one ADD parameter but the regex list is null
                         * assign matches = false and leave the loop
                         */
                        log.warn("addParameters objRegexListis null the ref cannot be added (no params added)\n" + refKeyedComplexSO);
                        break;
                    }
                    if (refKeyedComplexSO.getValue() == null) {
                        log.error("addParameters ref value is null for ADD (the param is not processed)\n" + refKeyedComplexSO);
                        continue;
                    }
                    if (refKeyedComplexSO.getPosition() != null) {
                        if ((refKeyedComplexSO.getPosition() >= 0) && (refKeyedComplexSO.getPosition() <= objRegexList.size())) {
                            objRegexList.add(refKeyedComplexSO.getPosition(), refKeyedComplexSO.getValue());
                        } else {
                            log.warn("addParameters incorrect position " + refKeyedComplexSO.getPosition()
                                    + " size " + objRegexList.size() + " for ref (adding to the end)\n" + refKeyedComplexSO);
                            objRegexList.add(refKeyedComplexSO.getValue());
                        }
                        count++;
                    } else {
                        boolean matched = false;
                        for (ISOComplexRegex currObjSO : objRegexList) {
                            if (refKeyedComplexSO.defaultMatch(currObjSO)) {
                                log.warn("The REF has the type ADD but matches another object(not adding to the list) \nRef\n" + refKeyedComplexSO
                                        + "\nMatching object\n" + currObjSO);
                                matched = true;
                                break;
                            }
                        }
                        // only add objects that were not matched
                        if (!matched) {
                            objRegexList.add(refKeyedComplexSO.getValue());
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Integer replaceAddParameters(ISOSearchScript obj) {
        Integer count = super.replaceAddParameters(obj);
        if ((refScriptText != null) && (RefType.REPLACE_ADD.equals(refScriptText.getType()))) {
            // replace the script text
            if (refScriptText.getValue() != null) {
                obj.setScriptText(refScriptText.getValue());
                count++;
            }
        }

        if ((refKeyedComplexSOList != null) && (!refKeyedComplexSOList.isEmpty())) {
            /* get a reference to the main list
             * no null check is necessary because this list must always be not null
             */
            List<ISOComplexRegex> objRegexList = obj.getMainRegexList();
            for (RefSOComplexRegex refKeyedComplexSO : refKeyedComplexSOList) {
                if (RefType.REPLACE_ADD.equals(refKeyedComplexSO.getType())) {
                    if (objRegexList == null) {
                        /* there is at least one REPLACE_ADD parameter but the regex list is null
                         * assign matches = false and leave the loop
                         */
                        log.warn("replaceAddParameters objRegexListis null the ref cannot be added/replaced (no params added/replaced)\n" + refKeyedComplexSO);
                        break;
                    }
                    if (refKeyedComplexSO.getValue() == null) {
                        log.error("addParameters ref value is null for REPLACE_ADD (the param is not processed)\n" + refKeyedComplexSO);
                        continue;
                    }
                    if (refKeyedComplexSO.getPosition() != null) {
                        // need to use < size for setting
                        if ((refKeyedComplexSO.getPosition() >= 0) && (refKeyedComplexSO.getPosition() < objRegexList.size())) {
                            objRegexList.set(refKeyedComplexSO.getPosition(), refKeyedComplexSO.getValue());
                        } else {
                            log.warn("replaceAddParameters incorrect position " + refKeyedComplexSO.getPosition()
                                    + " size " + objRegexList.size() + " for ref (adding to the end)\n" + refKeyedComplexSO);
                            objRegexList.add(refKeyedComplexSO.getValue());
                        }
                        count++;
                    } else {
                        int setPos = -1;
                        for (int i = 0; i < objRegexList.size(); i++) {
                            ISOComplexRegex currObjSO = objRegexList.get(i);
                            //if (matchPredicate.test(currObjSO)) {
                            if (refKeyedComplexSO.defaultMatch(currObjSO)) {
                                setPos = i;
                                break;
                            }
                        }
                        if (setPos > 0) {
                            objRegexList.set(setPos, refKeyedComplexSO.getValue());
                        } else {
                            objRegexList.add(refKeyedComplexSO.getValue());
                        }
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Integer replaceParameters(ISOSearchScript obj) {
        Integer count = super.replaceAddParameters(obj);
        if ((refScriptText != null) && (RefType.REPLACE_ADD.equals(refScriptText.getType()))) {
            // replace the script text
            if (refScriptText.getValue() != null) {
                obj.setScriptText(refScriptText.getValue());
                count++;
            }
        }

        if ((refKeyedComplexSOList != null) && (!refKeyedComplexSOList.isEmpty())) {
            /* get a reference to the main list
             * no null check is necessary because this list must always be not null
             */
            List<ISOComplexRegex> objRegexList = obj.getMainRegexList();
            for (RefSOComplexRegex refKeyedComplexSO : refKeyedComplexSOList) {
                if (RefType.REPLACE.equals(refKeyedComplexSO.getType())) {
                    if (objRegexList == null) {
                        /* there is at least one REPLACE parameter but the regex list is null
                         * assign matches = false and leave the loop
                         */
                        log.warn("replaceParameters objRegexListis null the ref cannot be replaced (no params replaced)\n" + refKeyedComplexSO);
                        break;
                    }
                    if (refKeyedComplexSO.getValue() == null) {
                        log.error("addParameters ref value is null for REPLACE (the param is not processed)\n" + refKeyedComplexSO);
                        continue;
                    }
                    if (refKeyedComplexSO.getPosition() != null) {
                        if ((refKeyedComplexSO.getPosition() >= 0) && (refKeyedComplexSO.getPosition() < objRegexList.size())) {
                            objRegexList.set(refKeyedComplexSO.getPosition(), refKeyedComplexSO.getValue());
                            count++;
                        } else {
                            log.warn("replaceParameters incorrect position " + refKeyedComplexSO.getPosition()
                                    + " size " + objRegexList.size() + " for ref (no action)\n" + refKeyedComplexSO);
                        }
                    } else {
                        int setPos = -1;
                        // loop through the so in the object list
                        for (int i = 0; i < objRegexList.size(); i++) {
                            ISOComplexRegex currObjSO = objRegexList.get(i);
                            //if (matchPredicate.test(currObjSO)) {
                            if (refKeyedComplexSO.defaultMatch(currObjSO)) {
                                setPos = i;
                                break;
                            }
                        }
                        if (setPos > 0) {
                            objRegexList.set(setPos, refKeyedComplexSO.getValue());
                            count++;
                        } else {
                            log.warn("replaceParameters Ref value not replaced\n" + refKeyedComplexSO);
                        }
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Integer removeParameters(ISOSearchScript obj) {
        Integer count = super.removeParameters(obj);
        if ((refScriptText != null) && (RefType.REMOVE.equals(refScriptText.getType()))) {
            if ((refScriptText.getValue() != null) && (obj.getScriptText() != null)) {
                String resultScript = obj.getScriptText().replace(refScriptText.getValue(), "");
                obj.setScriptText(resultScript);
                count++;
            }
        }
        if ((refKeyedComplexSOList != null) && (!refKeyedComplexSOList.isEmpty())) {
            /* get a reference to the main list
             * no null check is necessary because this list must always be not null
             */
            List<ISOComplexRegex> objRegexList = obj.getMainRegexList();
            for (RefSOComplexRegex refKeyedComplexSO : refKeyedComplexSOList) {
                /* do not perform a non null check for the Value
                 * because it may be not necessary for remove the default match predicate
                 * should handle this
                 */
                if (RefType.REMOVE.equals(refKeyedComplexSO.getType())) {
                    if (objRegexList == null) {
                        /* there is at least one REMOVE parameter but the regex list is null
                         * assign matches = false and leave the loop
                         */
                        log.warn("removeParameters objRegexListis null the ref cannot be remove (no params removed)\n" + refKeyedComplexSO);
                        break;
                    }
                    if (refKeyedComplexSO.getPosition() != null) {
                        if ((refKeyedComplexSO.getPosition() >= 0) && (refKeyedComplexSO.getPosition() < objRegexList.size())) {
                            objRegexList.remove(refKeyedComplexSO.getPosition().intValue());
                            count++;
                        } else {
                            log.warn("removeParameters incorrect position " + refKeyedComplexSO.getPosition()
                                    + " size " + objRegexList.size() + " for ref (no action)\n" + refKeyedComplexSO);
                        }
                    } else {
                        int remPos = -1;
                        // loop through the so in the object list
                        for (int i = 0; i < objRegexList.size(); i++) {
                            ISOComplexRegex currObjSO = objRegexList.get(i);
                            //if (matchPredicate.test(currObjSO)) {
                            if (refKeyedComplexSO.defaultMatch(currObjSO)) {
                                remPos = i;
                                break;
                            }
                        }
                        if (remPos > 0) {
                            objRegexList.remove(remPos);
                            count++;
                        } else {
                            log.warn("removeParameters cannot find ref (no action)\n" + refKeyedComplexSO);
                        }
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Integer cpAdd(ISOSearchScript obj) {
        Integer count = super.cpAdd(obj);
        if ((refKeyedComplexSOList != null) && (!refKeyedComplexSOList.isEmpty())) {
            /* get a reference to the main list
             * no null check is necessary because this list must always be not null
             */
            List<ISOComplexRegex> objRegexList = obj.getMainRegexList();
            // the resolved object
            ISOComplexRegex newAddObj = null;
            for (RefSOComplexRegex refKeyedComplexSO : refKeyedComplexSOList) {
                if (RefType.CP_ADD.equals(refKeyedComplexSO.getType())) {
                    if (objRegexList == null) {
                        /* there is at least one CP_ADD parameter but the regex list is null
                         * assign matches = false and leave the loop
                         */
                        log.warn("cpAdd objRegexListis null the ref cannot be added (CP_ADD) (no params added)\n" + refKeyedComplexSO);
                        break;
                    }
                    newAddObj = null;
                    // loop through the so in the object list
                    for (ISOComplexRegex currObjSO : objRegexList) {
                        //if (matchPredicate.test(currObjSO)) {
                        if (refKeyedComplexSO.defaultMatch(currObjSO)) {
                            newAddObj = refKeyedComplexSO.resolve(currObjSO);
                            break;
                        }
                    }
                    if (newAddObj == null) {
                        obj.addRef(refKeyedComplexSO);
                        log.info("cpAdd ref not found for cpAdd (adding ref) " + refKeyedComplexSO);
                    } else {
                        if (refKeyedComplexSO.getPosition() != null) {
                            if ((refKeyedComplexSO.getPosition() >= 0) && (refKeyedComplexSO.getPosition() <= objRegexList.size())) {
                                objRegexList.add(refKeyedComplexSO.getPosition(), newAddObj);
                            } else {
                                log.warn("cpAdd incorrect position " + refKeyedComplexSO.getPosition()
                                        + " size " + objRegexList.size() + " for ref (adding to the end)\n" + refKeyedComplexSO);
                                objRegexList.add(newAddObj);
                            }
                        } else {
                            objRegexList.add(newAddObj);
                        }
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Integer cpReplace(ISOSearchScript obj) {
        Integer count = super.cpReplace(obj);
        if ((refKeyedComplexSOList != null) && (!refKeyedComplexSOList.isEmpty())) {
            /* get a reference to the main list
             * no null check is necessary because this list must always be not null
             */
            List<ISOComplexRegex> objRegexList = obj.getMainRegexList();
            // the resolved object
            ISOComplexRegex newReplObj = null;
            for (RefSOComplexRegex refKeyedComplexSO : refKeyedComplexSOList) {
                if (RefType.CP_REPLACE.equals(refKeyedComplexSO.getType())) {
                    if (objRegexList == null) {
                        /* there is at least one CP_REPLACE parameter but the regex list is null
                         * assign matches = false and leave the loop
                         */
                        log.warn("cpAdd objRegexListis null the ref cannot be relaced (CP_REPLACE) (no params replaced)\n" + refKeyedComplexSO);
                        break;
                    }
                    newReplObj = null;
                    int setPos = -1;
                    for (int i = 0; i < objRegexList.size(); i++) {
                        ISOComplexRegex currObjSO = objRegexList.get(i);
                        if (refKeyedComplexSO.defaultMatch(currObjSO)) {
                            newReplObj = refKeyedComplexSO.resolve(currObjSO);
                            setPos = i;
                        }
                    }
                    if (newReplObj == null) {
                        obj.addRef(refKeyedComplexSO);
                        log.info("cpReplace ref not found for cpAdd (adding ref) " + refKeyedComplexSO);
                    } else {
                        if (refKeyedComplexSO.getPosition() != null) {
                            if ((refKeyedComplexSO.getPosition() >= 0) && (refKeyedComplexSO.getPosition() < objRegexList.size())) {
                                objRegexList.set(refKeyedComplexSO.getPosition(), newReplObj);
                            } else {
                                log.warn("cpReplace incorrect position " + refKeyedComplexSO.getPosition()
                                        + " size " + objRegexList.size() + " for ref (no action)\n" + refKeyedComplexSO);
                            }
                        } else if (setPos > 0) {
                            objRegexList.set(setPos, newReplObj);
                        } else {
                            log.error("cpReplace Unknown error setPos " + setPos + " size " + objRegexList.size()
                                    + " Ref\n" + refKeyedComplexSO);
                        }
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public RefParamString getRefScriptText() {
        return refScriptText;
    }

    public void setRefScriptText(RefParamString refScriptText) {
        this.refScriptText = refScriptText;
    }

    public void addMainRegex(RefSOComplexRegex newRegex) {
        if (refKeyedComplexSOList != null) {
            refKeyedComplexSOList.add(newRegex);
        }
    }

    public List<RefSOComplexRegex> getMainRegexList() {
        return refKeyedComplexSOList;
    }

    public void setMainRegexList(List<RefSOComplexRegex> mainRegexList) {
        this.refKeyedComplexSOList = mainRegexList;
    }

    @Override
    public List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> getRefList() {
        return refContainer.getRefList();
    }

    @Override
    public void setRefList(List<RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> newRefList) {
        refContainer.setRefList(newRefList);
    }

    @Override
    public <F extends RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>> void addRef(F newRef) {
        refContainer.addRef(newRef);
    }

    @Override
    public List<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> resolveRefs(ISearchObjectRepository rep) {
        return refContainer.resolveRefs(rep);
    }

    @Override
    public boolean isAllRefResolved() {
        return refContainer.isAllRefResolved();
    }

    @Override
    public boolean isContainRefs() {
        return refContainer.isContainRefs();
    }

    @Override
    public String toString() {
        return "RefSOSearchScript [name=" + name + ", group=" + group + ", tags=" + tags + ", refType=" + refType
                + ", refName=" + refName + ", refGroup=" + refGroup + ", refTags=" + refTags
                + ", refReplaceParams=" + refReplaceParams + ", refSortRequests=" + refSortRequests
                + ", refAcceptanceCriteria=" + refAcceptanceCriteria + ", refRegexFlags=" + refRegexFlags
                + ", refDateInfo=" + refDateInfo + ", refViewList=" + refViewList + ", refScriptText=" + refScriptText
                + ", refKeyedComplexSOList=" + refKeyedComplexSOList + ", refContainer=" + refContainer + "]";
    }

}
