package org.eclipselabs.real.gui.core.sotree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.TagRef;
import org.eclipselabs.real.gui.core.sort.SortRequestKey;

public class DisplaySOSelector extends DisplaySOTemplateAbstractImpl implements IDisplaySOSelector {

    private static final Logger log = LogManager.getLogger(DisplaySOSelector.class);
    protected List<IRealRegex> nameRegexes = new ArrayList<IRealRegex>();
    protected List<IRealRegex> groupRegexes = new ArrayList<IRealRegex>();
    protected List<TagRef> tagsRegexes = new ArrayList<TagRef>();

    protected String textViewName;
    protected List<String> shortViewPatterns = new ArrayList<String>();
    protected List<SortRequestKey> sortRequestKeys;


    public DisplaySOSelector(String aName) {
        super(DisplaySOTreeItemType.SELECTOR, aName);
    }

    public DisplaySOSelector(String aName, List<IRealRegex> nameRegList, List<IRealRegex> groupRegList,
            List<TagRef> tagsRegList) {
        this(aName);
        nameRegexes = nameRegList;
        groupRegexes = groupRegList;
        tagsRegexes = tagsRegList;
    }

    @Override
    public boolean matchesSearchObject(IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> searchObject) {
        boolean soMatches = true;
        if (soMatches && (nameRegexes != null)) {
            for (IRealRegex nameReg : nameRegexes) {
                IMatcherWrapper mtWr = nameReg.getMatcherWrapper(searchObject.getSearchObjectName(), null, null);
                if (!mtWr.matches()) {
                    soMatches = false;
                    break;
                }
            }
        }
        if (soMatches && (groupRegexes != null)) {
            for (IRealRegex groupReg : groupRegexes) {
                IMatcherWrapper mtWr = groupReg.getMatcherWrapper(searchObject.getSearchObjectGroup().getString(), null, null);
                if (!mtWr.matches()) {
                    soMatches = false;
                    break;
                }
            }
        }
        if (soMatches && (tagsRegexes != null)) {
            for (TagRef tagReg : tagsRegexes) {
                //log.debug("Matching tags soName=" + searchObject.getSearchObjectName() + " tag=" + tagReg);
                boolean tagMatches = false;
                if ((searchObject.getSearchObjectTags() != null) && searchObject.getSearchObjectTags().isEmpty()) {
                    tagMatches = false;
                }
                for (Map.Entry<String, String> currTag : searchObject.getSearchObjectTags().entrySet()) {
                    //log.debug("Matching tags soName=" + searchObject.getSearchObjectName() + " tag=" + tagReg + " tagName=" + currTagname);
                    IMatcherWrapper mtWrName = tagReg.getNameRegex().getMatcherWrapper(currTag.getKey(), null, null);
                    if (mtWrName.matches()) {
                        //log.debug("Matching tags soName=" + searchObject.getSearchObjectName() + " tag=" + tagReg + " tagName=" + currTagname + " MATCHES");
                        IMatcherWrapper mtWrvalue = tagReg.getValueRegex().getMatcherWrapper(currTag.getValue(), null, null);
                        if (mtWrvalue.matches()) {
                            tagMatches = true;
                            break;
                        } else {
                            tagMatches = false;
                        }
                    }
                }
                switch(tagReg.getType()) {
                case MATCH:
                    if (!tagMatches) {
                        soMatches = false;
                    }
                    break;
                case NOT_MATCH:
                    if (tagMatches) {
                        soMatches = false;
                    }
                    break;
                default:
                    log.error("matchesSearchObject unknown tagreg type " + tagReg.getType());
                    break;
                }
                if (!soMatches) {
                    break;
                }
            }
        }
        return soMatches;
    }

    @Override
    public List<IDisplaySO> getSearchTreeItems(List<IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> soList) {
        List<IDisplaySO> resultList = new ArrayList<IDisplaySO>();
        for (IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> searchObj : soList) {
            if (matchesSearchObject(searchObj)) {
                IDisplaySO displaySearchObj = new DisplaySOImpl(searchObj.getSearchObjectName(), searchObj, false);
                displaySearchObj.setTextViewName(getTextViewName());
                displaySearchObj.setViewNamePatterns(new ArrayList<String>(getViewPatterns()));
                List<SortRequestKey> copyList = null;
                if ((getSortRequestKeys() != null) && (!getSortRequestKeys().isEmpty())) {
                    copyList = new ArrayList<>();
                    for (SortRequestKey srKey : getSortRequestKeys()) {
                        try {
                            copyList.add(srKey.clone());
                        } catch (CloneNotSupportedException e) {
                            log.error("getSearchTreeItems Clone not supported",e);
                        }
                    }
                    displaySearchObj.setSortRequestKeys(copyList);
                }
                displaySearchObj.setDisplayName(searchObj.getSearchObjectName());
                displaySearchObj.setDescription(searchObj.getSearchObjectDescription());
                if ((getParent() != null) && (getParent().getGuiProperties() != null)) {
                    displaySearchObj.setGuiProperties(new HashMap<>(getParent().getGuiProperties()));
                }
                if (getGuiProperties() != null) {
                    if (displaySearchObj.getGuiProperties() != null) {
                        displaySearchObj.getGuiProperties().putAll(getGuiProperties());
                    } else {
                        displaySearchObj.setGuiProperties(new HashMap<>(getGuiProperties()));
                    }
                }
                resultList.add(displaySearchObj);
            }
        }
        return resultList;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<IRealRegex> getNameRegexes() {
        return nameRegexes;
    }

    @Override
    public void setNameRegexes(List<IRealRegex> nameRegexes) {
        this.nameRegexes = nameRegexes;
    }

    @Override
    public List<IRealRegex> getGroupRegexes() {
        return groupRegexes;
    }

    @Override
    public void setGroupRegexes(List<IRealRegex> groupRegexes) {
        this.groupRegexes = groupRegexes;
    }

    @Override
    public List<TagRef> getTagsRegexes() {
        return tagsRegexes;
    }

    @Override
    public void setTagsRegexes(List<TagRef> tagsRegexes) {
        this.tagsRegexes = tagsRegexes;
    }

    @Override
    public String getTextViewName() {
        return textViewName;
    }

    @Override
    public List<String> getViewPatterns() {
        return shortViewPatterns;
    }

    @Override
    public void setTextViewName(String textViewName) {
        this.textViewName = textViewName;
    }

    @Override
    public void setViewPatterns(List<String> shortViewNames) {
        this.shortViewPatterns = shortViewNames;
    }

    @Override
    public void addViewPattern(String aViewName) {
        shortViewPatterns.add(aViewName);
    }

    @Override
    public List<SortRequestKey> getSortRequestKeys() {
        return sortRequestKeys;
    }

    @Override
    public void setSortRequestKeys(List<SortRequestKey> sortRequestKeys) {
        this.sortRequestKeys = sortRequestKeys;
    }

}
