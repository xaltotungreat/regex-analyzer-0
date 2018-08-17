package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionStage;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.ref.IRefKeyedSOContainer;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.ISRSearchScript;
import org.eclipselabs.real.core.searchresult.SRSearchScriptImpl;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * This is the implementation of the search script. The methods of {@link IRefKeyedSOContainer} are not implemented directly
 * but an internal (proxy) object is used that implements all the methods.
 *
 * @author Vadim Korkin
 *
 */
public class SOSearchScript extends KeyedComplexSearchObjectImpl<ISRSearchScript,
        IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
        ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> implements ISOSearchScript {

    private static final Logger log = LogManager.getLogger(SOSearchScript.class);
    protected String scriptText;
    // the internal search objects list
    protected List<ISOComplexRegex> mainRegexList = Collections.synchronizedList(new ArrayList<ISOComplexRegex>());

    public SOSearchScript(String aName) {
        super(SearchObjectType.SEARCH_SCRIPT, aName);
    }

    @Override
    public ISRSearchScript performSearch(PerformSearchRequest request) {
        log.info("performSearch " + this);
        log.info("performSearch request " + request);
        if (request == null) {
            log.error("performSearch request is null");
            return null;
        }
        if (scriptText != null) {
            request.getProgressMonitor().setCurrentSOName(getSearchObjectName());
            // init the search result
            // do not load the cached replace table - it is used for fine tuning the tables in child complex regexes
            Map<String,String> cachedReplaceTable = new HashMap<>();
            Map<ReplaceableParamKey, IReplaceableParam<?>> allReplaceParams = getAllReplaceParams(request.getStaticReplaceParams());
            List<ISOComplexRegex> clonedMainRegexes = Collections.synchronizedList(new ArrayList<ISOComplexRegex>());
            for (ISOComplexRegex iKeyedComplexSearchObject : mainRegexList) {
                try {
                    ISOComplexRegex clonedMainSO = (ISOComplexRegex)iKeyedComplexSearchObject.clone();
                    clonedMainRegexes.add(clonedMainSO);
                } catch (CloneNotSupportedException e) {
                    log.error("performSearch ", e);
                }
            }
            ISRSearchScript result = new SRSearchScriptImpl(getSearchObjectName(), getCloneSortRequestList(),
                    clonedMainRegexes, cachedReplaceTable, request.getStaticReplaceParams(), allReplaceParams, getSearchObjectGroup(),
                    getSearchObjectTags(), request.getProgressMonitor());
            result.setViewOrder(viewOrder);
            if (request.getCustomRegexFlags() != null) {
                result.setRegexFlags(request.getCustomRegexFlags());
            }
            if (getDateInfos() != null) {
                try {
                    List<ISearchObjectDateInfo> newInfos = new ArrayList<>();
                    for (ISearchObjectDateInfo di : getDateInfos()) {
                        newInfos.add(di.clone());
                    }
                    result.setDateInfos(newInfos);
                } catch (CloneNotSupportedException e) {
                    log.error("performSearch",e);
                }
            }
            // clone all stages after SEARCH (not containing search)
            List<IAcceptanceCriterion> mergeAC = getCloneAcceptanceList(new Predicate<IAcceptanceCriterion>() {

                @Override
                public boolean test(IAcceptanceCriterion t) {
                    return (((t.getStages().size() == 1) && (t.getStages().contains(AcceptanceCriterionStage.MERGE)))
                            || ((t.getStages().contains(AcceptanceCriterionStage.MERGE)) && (!t.isAccumulating())));
                }
            });
            if (mergeAC != null) {
                result.getAcceptanceList().addAll(mergeAC);
            }
            /* make a list of acceptance clones for the SEARCH stage because the acceptance objects
             * may be changed during the search (some criteria may accumulate results to perform certain functions)
             */
            List<IAcceptanceCriterion> searchAC = getCloneAcceptanceList(new Predicate<IAcceptanceCriterion>() {

                @Override
                public boolean test(IAcceptanceCriterion t) {
                    return t.getStages().contains(AcceptanceCriterionStage.SEARCH);
                }
            });
            /* add to the result acceptances as well to save the accumulated information
             * (some acceptances may accumulate information) while accepting
             * for example storing distinct values for example.
             */
            if (searchAC != null) {
                for (IAcceptanceCriterion ac : searchAC) {
                    if ((ac.getStages().contains(AcceptanceCriterionStage.MERGE)) && (ac.isAccumulating())) {
                        result.getAcceptanceList().add(ac);
                    }
                }
            }
            // log text for a search in current
            if (request.getText() != null) {
                result.setLogText(request.getText());
            }
            // the Binding object keeps the references to the actual java objects
            // in the script they can be referenced by these names
            Binding groovyBind = new Binding();
            groovyBind.setVariable("SearchScriptObject", this);
            groovyBind.setVariable("SearchScriptResult", result);
            groovyBind.setVariable("scriptResult", result);
            GroovyShell shell = new GroovyShell(groovyBind);
            // execute the script
            shell.evaluate(scriptText);
            // return null if no objects found
            if (result.getSRObjects().isEmpty()) {
                result = null;
            }
            System.gc();
            return result;
        } else {
            log.error("performSearch script text is null");
            return null;
        }
    }

    @Override
    public String getScriptText() {
        return scriptText;
    }

    @Override
    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    @Override
    public List<ISOComplexRegex> getMainRegexList() {
        return mainRegexList;
    }

    @Override
    public void setMainRegexList(List<ISOComplexRegex> mrList) {
        synchronized(mainRegexList) {
            mainRegexList.clear();
            mainRegexList.addAll(mrList);
        }
    }

    @Override
    public ISOSearchScript clone() throws CloneNotSupportedException {
        SOSearchScript cloneObj = (SOSearchScript)super.clone();
        if (viewMap != null) {
            Map<String, ISOComplexRegexView> newViewMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, ISOComplexRegexView> currView : viewMap.entrySet()) {
                newViewMap.put(currView.getKey(), (ISOComplexRegexView)currView.getValue().clone());
            }
            cloneObj.viewMap = newViewMap;
        }
        if (viewOrder != null) {
            List<String> newViewOrder = Collections.synchronizedList(new ArrayList<String>());
            newViewOrder.addAll(viewOrder);
            cloneObj.viewOrder = newViewOrder;
        }
        if (mainRegexList != null) {
            List<ISOComplexRegex> newMainRegexList = Collections.synchronizedList(new ArrayList<ISOComplexRegex>());
            // according to Java documentation for the synchronized list it is
            // "imperative that the user manually synchronize on the returned list
            // when iterating over it"
            synchronized(mainRegexList) {
                for (ISOComplexRegex currSO : mainRegexList) {
                    newMainRegexList.add((ISOComplexRegex)currSO.clone());
                }
            }
            cloneObj.setMainRegexList(newMainRegexList);
        }
        return cloneObj;
    }

    @Override
    public ISOComplexRegex getInternalSOByName(String complRegName) {
        ISOComplexRegex result = null;
        if ((mainRegexList != null) && (!mainRegexList.isEmpty())) {
            // according to Java documentation for the synchronized list it is
            // "imperative that the user manually synchronize on the returned list
            // when iterating over it"
            synchronized(mainRegexList) {
                for (ISOComplexRegex currComplRegex : mainRegexList) {
                    if ((currComplRegex.getSearchObjectName() != null) && (currComplRegex.getSearchObjectName().equals(complRegName))) {
                        result = currComplRegex;
                        break;
                    }
                }
            }
        } else {
            log.error("The Complex regex Array is null for script name=" + getSearchObjectName()
                    + " group=" + getSearchObjectGroup());
        }
        return result;
    }

    @Override
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup) {
        super.setSearchObjectGroup(newGroup);
        synchronized(mainRegexList) {
            for (IKeyedComplexSearchObject<
                    ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                        ISRComplexRegexView, ISROComplexRegexView, String>,
                    ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> currSO : mainRegexList) {
                try {
                    currSO.setSearchObjectGroup(newGroup.clone());
                } catch (CloneNotSupportedException e) {
                    log.error("setSearchObjectGroup ", e);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nSOSearchScript name=").append(getSearchObjectName()).append(" group=").append(getSearchObjectGroup()).append(" tags=").append(getSearchObjectTags());
        sb.append("\nReplaceParams ").append(getCloneParamList());
        sb.append("\nDate info ").append(getDateInfos());
        sb.append("\nScript text: \n").append(scriptText);
        sb.append("\nMain complex SO:");
        for (IKeyedComplexSearchObject<
                ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                        ISRComplexRegexView, ISROComplexRegexView, String>,
                    ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> currReg : mainRegexList) {
            sb.append("\n\t").append(currReg);
        }
        sb.append("\nAcceptance:");
        for (IAcceptanceCriterion currCrit : acceptanceList) {
            sb.append("\n\t").append(currCrit);
        }
        sb.append("\nViews:");
        for (ISOComplexRegexView currView : viewMap.values()) {
            sb.append("\n\t").append(currView);
        }
        return sb.toString();
    }

}
