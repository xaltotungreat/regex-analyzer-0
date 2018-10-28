package org.eclipselabs.real.gui.core.sotree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.gui.core.sort.SortRequestKey;

public class DisplaySOImpl extends DisplaySOTreeItemImpl implements IDisplaySO {
    
    protected IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> theSearchObject;
    protected String textViewName;
    protected List<String> viewNamePatterns = new ArrayList<String>();
    protected List<SortRequestKey> sortRequestKeys = new ArrayList<>();
    
    protected String displayName;
    protected String description;
    protected Map<String,Object> guiProperties = new HashMap<String,Object>();
    
    public DisplaySOImpl(String aName, IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so, Boolean expandState) {
        super(DisplaySOTreeItemType.SEARCH_OBJECT, aName, expandState);
        theSearchObject = so;
    }

    @Override
    public IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> getSearchObject() {
        return theSearchObject;
    }
    
    @Override
    public String getIconPath(String identifier) {
        String iconPath = null;
        Map<String,String> iconPathsMap = (Map<String,String>)getGuiProperties().get(getSearchObjectType().name());
        if (iconPathsMap == null) {
            iconPathsMap = (Map<String,String>)getGuiProperties().get(IDisplaySOConstants.GUI_PROPERTY_DEFAULT_ICON_SET);
        }

        if ((iconPathsMap != null) && (identifier != null)) {
            iconPath = iconPathsMap.get(identifier);
        }
        return iconPath;
    }

    @Override
    public String getTextViewName() {
        return textViewName;
    }

    @Override
    public List<String> getViewNamePatterns() {
        return viewNamePatterns;
    }

    public void setTextViewName(String textViewName) {
        this.textViewName = textViewName;
    }

    public void setViewNamePatterns(List<String> aShortViewPatterns) {
        this.viewNamePatterns = aShortViewPatterns;
    }

    @Override
    public void addViewNamePattern(String aViewPattern) {
        viewNamePatterns.add(aViewPattern);
    }
    @Override
    public SearchObjectType getSearchObjectType() {
        return theSearchObject.getType();
    }
    @Override
    public List<SortRequestKey> getSortRequestKeys() {
        return sortRequestKeys;
    }
    @Override
    public void setSortRequestKeys(List<SortRequestKey> sortRequestKeys) {
        this.sortRequestKeys = sortRequestKeys;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
    @Override
    public void setDisplayName(String newDisplayName) {
        displayName = newDisplayName;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    @Override
    public void setDescription(String newLongName) {
        description = newLongName;
    }

    @Override
    public Map<String,Object> getGuiProperties() {
        return guiProperties;
    }
    @Override
    public void setGuiProperties(Map<String,Object> guiProperties) {
        this.guiProperties = guiProperties;
    }

    


}
