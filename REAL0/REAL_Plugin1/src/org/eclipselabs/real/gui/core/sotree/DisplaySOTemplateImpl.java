package org.eclipselabs.real.gui.core.sotree;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public class DisplaySOTemplateImpl extends DisplaySOTemplateAbstractImpl implements IDisplaySOTemplate {

    private static final Logger log = LogManager.getLogger(DisplaySOTemplateImpl.class);
    protected List<IDisplaySOTemplateAbstract> selectorList = new ArrayList<IDisplaySOTemplateAbstract>();
    
    public DisplaySOTemplateImpl(String aName) {
        this(aName, false);
    }
    
    public DisplaySOTemplateImpl(String aName, Boolean expandState) {
        super(DisplaySOTreeItemType.TEMPLATE, aName, expandState);
    }
    
    @Override
    public List<IDisplaySO> getSearchTreeItems(List<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> soList) {
        List<IDisplaySO> resultList = new ArrayList<IDisplaySO>();
        for (IDisplaySOTemplateAbstract currSelector : selectorList) {
            List<IDisplaySO> currList = currSelector.getSearchTreeItems(soList);
            resultList.addAll(currList);
        }
        return resultList;
    }
    
    @Override
    public List<IDisplaySO> getSearchTreeItems(int selectorPos, List<IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> soList) {
        List<IDisplaySO> resultList = new ArrayList<IDisplaySO>();
        if ((selectorPos >= 0) && (selectorPos < selectorList.size())) {
            resultList.addAll(selectorList.get(selectorPos).getSearchTreeItems(soList));
        } else {
            log.error("getSearchTreeItems incorrect selectorPos " + selectorPos + " size " + selectorList.size());
        }
        return resultList;
    }
    
    @Override
    public List<IDisplaySOTemplateAbstract> getSelectorList() {
        return selectorList;
    }

    @Override
    public void setSelectorList(List<IDisplaySOTemplateAbstract> selectorList) {
        this.selectorList = selectorList;
        if (this.selectorList != null) {
            for (IDisplaySOTemplateAbstract currSel : this.selectorList) {
                currSel.setParent(this);
            }
        }
    }

    @Override
    public void addSelector(IDisplaySOTemplateAbstract aSelector) {
        selectorList.add(aSelector);
        aSelector.setParent(this);
    }

    @Override
    public boolean matchesSearchObject(IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> searchObj) {
        boolean matchesSO = false;
        if (searchObj != null) {
            for (IDisplaySOTemplateAbstract currSelector : selectorList) {
                if (currSelector.matchesSearchObject(searchObj)) {
                    matchesSO = true;
                    break;
                }
            }
        }
        return matchesSO;
    }

    @Override
    public IDisplaySO getDisplaySearchObject(IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> searchObj) {
        IDisplaySO res = null;
        if (searchObj != null) {
            for (IDisplaySOTemplateAbstract currTemplate : selectorList) {
                if ((DisplaySOTreeItemType.SELECTOR.equals(currTemplate.getType())) && (currTemplate.matchesSearchObject(searchObj))) {
                    IDisplaySOSelector currSelector = (IDisplaySOSelector)currTemplate;
                    res = new DisplaySOImpl(searchObj.getSearchObjectName(), searchObj, false);
                    res.setTextViewName(currSelector.getTextViewName());
                    res.setViewNamePatterns(new ArrayList<String>(currSelector.getViewNamePatterns()));
                    res.setDisplayName(searchObj.getSearchObjectName());
                    break;
                }
            }
        }
        return res;
    }
    
    @Override
    public String toString() {
        return "DisplaySOTemplateImpl [treeItemType=" + treeItemType + ", name=" + name + "]";
    }


}
