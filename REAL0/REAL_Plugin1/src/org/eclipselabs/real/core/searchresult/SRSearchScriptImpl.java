package org.eclipselabs.real.core.searchresult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.ISearchProgressMonitor;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.script.SOContainer;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;

/**
 * This class holds the result of the search script execution.
 * Currently is serves the context role keeping references to the search regexes,
 * result objects stored by the script etc.
 * The script is stored in a simple String.
 * This class also holds the link to the progress monitor for this script
 *
 * IMPORTANT: if this object is cloned the progress monitor is not cloned. The progress monitor
 * is meant to be unique for a search operation not a specific search result.
 *
 * @author Vadim Korkin
 *
 */
public class SRSearchScriptImpl extends KeyedComplexSearchResultImpl<
        IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
        ISRComplexRegexView, ISROComplexRegexView, String> implements ISRSearchScript {

    private static final Logger log = LogManager.getLogger(SRSearchScriptImpl.class);
    protected ISearchProgressMonitor progressMonitor;
    /**
     * For scripts that search only in one log the search in current functionality
     * is enabled. In this case this variable contains the current log text
     */
    protected String logText;

    protected List<IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISRComplexRegexView, ISROComplexRegexView, String>,
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>>
        mainRegexList = Collections.synchronizedList(new ArrayList<IKeyedComplexSearchObject<
                ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                        ISRComplexRegexView, ISROComplexRegexView, String>,
                    ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>>());

    public SRSearchScriptImpl(String srName, List<IInternalSortRequest> aSortRequestList, Map<String,String> replaceTable) {
        super(srName, aSortRequestList, replaceTable);
    }

    public SRSearchScriptImpl(String aSOKey, List<IInternalSortRequest> aSortRequestList,
            List<IKeyedComplexSearchObject<
                ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>,
                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> execList,
            Map<String,String> replaceTable, ISearchObjectGroup<String> aGroup, Map<String,String> soTags,
            ISearchProgressMonitor monitor) {
        this(aSOKey, aSortRequestList, execList, replaceTable, null, null, aGroup, soTags, monitor);
    }

    public SRSearchScriptImpl(String aSOKey, List<IInternalSortRequest> aSortRequestList,
            List<IKeyedComplexSearchObject<
                ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>,
                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> execList,
            Map<String,String> replaceTable, Map<ReplaceParamKey, IReplaceParam<?>> customParams, Map<ReplaceParamKey, IReplaceParam<?>> allParams,
            ISearchObjectGroup<String> aGroup, Map<String,String> soTags, ISearchProgressMonitor monitor) {
        super(aSOKey, aSortRequestList, replaceTable, customParams, allParams, aGroup, soTags);
        progressMonitor = monitor;
        if (execList != null) {
            mainRegexList.addAll(execList);
        }
    }

    public SRSearchScriptImpl(ISRSearchScript copyObj) {
        super(copyObj);
    }

    public SRSearchScriptImpl(String aSOKey, List<IInternalSortRequest> aSortRequestList,
            Map<String,String> replaceTable, List<IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>> initSRList) {
        super(aSOKey, aSortRequestList, replaceTable, initSRList);
    }



    @Override
    public ISearchResult<IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>> getInstance() {
        return new SRSearchScriptImpl(this);
    }

    @Override
    public ISearchProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    @Override
    public void setProgressMonitor(ISearchProgressMonitor newMonitor) {
        progressMonitor = newMonitor;
    }

    @Override
    public List<IKeyedComplexSearchObject<
        ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISRComplexRegexView, ISROComplexRegexView, String>,
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> getMainRegexList() {
        return new ArrayList<IKeyedComplexSearchObject<
                ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                        ISRComplexRegexView, ISROComplexRegexView, String>,
                    ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>>(mainRegexList);
    }

    @Override
    public void setMainRegexList(List<IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>,
                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> mrList) {
        mainRegexList.clear();
        mainRegexList.addAll(mrList);
    }

    @Override
    public String getLogText() {
        return logText;
    }

    @Override
    public void setLogText(String logText) {
        this.logText = logText;
    }

    @Override
    public IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                            ISRComplexRegexView, ISROComplexRegexView, String>,
                        ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>
                getInternalSOByName(String complRegName) {
        IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>, ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> result = null;
        if ((mainRegexList != null) && (!mainRegexList.isEmpty())) {
            for (IKeyedComplexSearchObject<? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                        ISRComplexRegexView, ISROComplexRegexView, String>, ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> currComplRegex : mainRegexList) {
                if ((currComplRegex.getSearchObjectName() != null) && (currComplRegex.getSearchObjectName().equals(complRegName))) {
                    result = currComplRegex;
                    break;
                }
            }
        } else {
            log.error("The Complex regex Array is null for script name=" + getSearchObjectName() + " group=" + getSearchObjectGroup());
        }
        return result;
    }

    @Override
    public SOContainer getByName(String soNamePar) {
        return new SOContainer(getInternalSOByName(soNamePar), this, logText);
    }

    @Override
    public void setViewOrder(String... viewNames) {
        if (viewNames != null) {
            setViewOrder(Arrays.asList(viewNames));
        } else {
            setViewOrder((List<String>)null);
        }
    }

    @Override
    public ISearchResult<IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>> clone() throws CloneNotSupportedException {
        SRSearchScriptImpl cloneObj = (SRSearchScriptImpl)super.clone();
        if (viewOrder != null) {
            List<String> newViewOrder = new ArrayList<>();
            newViewOrder.addAll(viewOrder);
            cloneObj.viewOrder = newViewOrder;
        }
        if (mainRegexList != null) {
            List<IKeyedComplexSearchObject<
                        ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                            ISRComplexRegexView, ISROComplexRegexView, String>,
                        ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                        ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>>
                newMainRegexList = Collections.synchronizedList(new ArrayList<IKeyedComplexSearchObject<
                            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                                    ISRComplexRegexView, ISROComplexRegexView, String>,
                                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>>());
            for (IKeyedComplexSearchObject<
                    ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                        ISRComplexRegexView, ISROComplexRegexView, String>,
                    ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> currSO : mainRegexList) {
                newMainRegexList.add((IKeyedComplexSearchObject<
                        ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                                ISRComplexRegexView, ISROComplexRegexView, String>,
                            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>)currSO.clone());
            }
            cloneObj.mainRegexList = newMainRegexList;
        }
        return cloneObj;
    }

}
