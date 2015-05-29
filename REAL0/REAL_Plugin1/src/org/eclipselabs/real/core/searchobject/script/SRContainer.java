package org.eclipselabs.real.core.searchobject.script;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.ISRSearchScript;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

/**
 * This is a container for a search result.
 * This container is necessary for 2 reasons:
 * - the search object itself has a lot of generic parameters, these parameters are unwieldy to use in scripts
 * - the container provides additional API to work with search object parameters.
 * Among the most important API is the methods to add more result objects and update the replace map
 * from the views.
 *
 * @author Vadim Korkin
 *
 */
public class SRContainer {
    private static final Logger log = LogManager.getLogger(SRContainer.class);

    protected volatile IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String> searchResult;

    protected volatile ISRSearchScript scriptResult;

    public SRContainer(IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String> res, ISRSearchScript scrRes) {
        if ((res != null) && (res.getSRObjects() != null) && (!res.getSRObjects().isEmpty())) {
            searchResult = res;
        } else {
            log.error("Empty search result");
        }
        if (scrRes != null) {
            scriptResult = scrRes;
        } else {
            log.error("Empty script result");
        }
    }

    /**
     * This method returns true if this container contains a null result or no result objects
     * @return true if this container contains a null result or no result objects false otherwise
     */
    public boolean isEmpty() {
        boolean emptyRes = false;
        if ((searchResult == null) || (searchResult.getSRObjects() == null) || (searchResult.getSRObjects().isEmpty())) {
            emptyRes = true;
        }
        return emptyRes;
    }

    /**
     * Returns the view object for the specified position in the list of search result objects and the name.
     * @param objNumber the position of the object in the list 0-based
     * @param viewKey the "name" of the view
     * @return the view object for the specified position in the list of search result objects and the name.
     */
    public ISRComplexRegexView getView(int objNumber, String viewKey) {
        ISRComplexRegexView viewRes = null;
        if (viewKey != null) {
            if ((searchResult != null) && (searchResult.getSRObjects() != null)
                    && (objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
                IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> resObj = searchResult.getSRObjects().get(objNumber);
                viewRes = resObj.getView(viewKey);
            } else {
                log.warn("getView incorrect searchResult " + searchResult + " or objNumber " + objNumber);
            }
        } else {
            log.warn("getView null viewKey");
        }
        return viewRes;
    }

    /**
     * Adds a new view to the specified SRO (by position) with the specified key (viewKey)
     * @param objNumber the position of the object in the list 0-based
     * @param viewKey the "name" of the view
     * @param newView the new view object
     */
    public void addView(int objNumber, String viewKey, ISRComplexRegexView newView) {
        if ((viewKey != null) && (newView != null)) {
            if ((searchResult != null) && (searchResult.getSRObjects() != null)
                    && (objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
                IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> resObj = searchResult.getSRObjects().get(objNumber);
                resObj.addView(viewKey, newView);
            } else {
                log.warn("addView incorrect searchResult " + searchResult + " or objNumber " + objNumber);
            }
        } else {
            log.warn("addView viewKey " + viewKey + " or view " + newView);
        }
    }

    /**
     * Returns the text of the specified SRO (by position in the list)
     * @param objNumber the position of the object in the list 0-based
     * @return the text of the specified SRO (by position in the list)
     */
    public String getText(int objNumber) {
        String res = null;
        if ((searchResult != null) && (searchResult.getSRObjects() != null)
                && (objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
            IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> resObj = searchResult.getSRObjects().get(objNumber);
            res = resObj.getText();
        } else {
            log.warn("getText incorrect searchResult " + searchResult + " or objNumber " + objNumber);
        }
        return res;
    }

    /**
     * Appends the specified text to the specified SRO (by position in the list)
     * @param objNumber the position of the object in the list 0-based
     * @param txt the text to append
     */
    public void appendText(int objNumber, String txt) {
        if (txt != null) {
            if ((searchResult != null) && (searchResult.getSRObjects() != null)
                    && (objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
                IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> resObj = searchResult.getSRObjects().get(objNumber);
                resObj.appendText(txt);
            }
        } else {
            log.warn("appendText null text passed");
        }
    }

    /**
     * Returns the number of SR objects in this container
     * @return the number of SR objects in this container
     */
    public int getResultsCount() {
        int res = 0;
        if ((searchResult != null) && (searchResult.getSRObjects() != null)) {
            res = searchResult.getSRObjects().size();
        } else {
            log.warn("getResultsCount searchresult is null or the SR List is null");
        }
        return res;
    }

    /**
     * Updates the dynamic replace map (that is stored in the script result and has higher priority over the default calculated map).
     * This method does the following:
     * - obtains the search result object at the specified position in the list (0-based)
     * - creates a Map<String,String> where the keys are view names from that SRO and values are view texts from that SRO
     * - puts the values from the map to the dynamic replace table
     * @param objNumber the position of the object in the list 0-based
     */
    public void updateReplaceMap(int objNumber) {
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            if ((objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
                log.debug("updateReplaceMap resNumber=" + objNumber + " viewNames=" + searchResult.getSRObjects().get(objNumber).getViewsText());
                scriptResult.putToReplaceTable(searchResult.getSRObjects().get(objNumber).getViewsText());
            } else {
                log.error("pushResultObject Incorrect objNumber " + objNumber);
            }
        } else {
            log.error("Either searchResult is null or it has no SR Objects");
        }
    }

    /**
     * Updates the dynamic replace map (that is stored in the script result and has higher priority over the default calculated map).
     * This method does the following:
     * - obtains the search result object at the specified position in the list (0-based)
     * - creates a Map<String,String> where the keys are view names from that SRO and values are view texts from that SRO. Only the views
     * which names are in viewNames are selected
     * - puts the values from the map to the dynamic replace table
     * The method with varargs is provided for convenience in scripts
     * @param objNumber the position of the object in the list 0-based
     * @param viewNames the list of names which are selected to update the dynamic map
     */
    public void updateReplaceMap(int objNumber, String...viewNames) {
        if (viewNames != null) {
            updateReplaceMap(objNumber, Arrays.asList(viewNames));
        } else {
            log.warn("updateReplaceMap resNumber=" + objNumber + " no view names nothing to update");
        }
    }

    /**
     * Updates the dynamic replace map (that is stored in the script result and has higher priority over the default calculated map).
     * This method does the following:
     * - obtains the search result object at the specified position in the list (0-based)
     * - creates a Map<String,String> where the keys are view names from that SRO and values are view texts from that SRO. Only the views
     * which names are in viewNames are selected
     * - puts the values from the map to the dynamic replace table
     * @param objNumber the position of the object in the list 0-based
     * @param viewNames the list of names which are selected to update the dynamic map
     */
    public void updateReplaceMap(int objNumber, List<String> viewNames) {
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty()) && (viewNames != null)) {
            log.debug("updateReplaceMap resNumber=" + objNumber + " viewNames=" + viewNames);
            if ((objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
                Map<String,String> viewsText = searchResult.getSRObjects().get(objNumber).getViewsText();
                for (String viewName : viewNames) {
                    if (viewsText.containsKey(viewName)) {
                        log.debug("updateReplaceMap viewName=" + viewName + " value=" + viewsText.get(viewName));
                        scriptResult.putToReplaceTable(viewName, viewsText.get(viewName));
                    }
                }
            } else {
                log.error("pushResultObject Incorrect objNumber " + objNumber);
            }
        } else {
            log.error("Either searchResult is null " + searchResult + " or viewNames is null " + viewNames);
        }
    }

    /**
     * This method pushes all the result objects from this search result to the script result. It means
     * these SROs are added to the list of SROs in the script result.
     */
    public void pushResultObjects() {
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            log.debug("pushResultObjects SOName=" + searchResult.getSearchObjectName() + " objCount=" + searchResult.getSRObjects().size());
            for (IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> currSRO : searchResult.getSRObjects()) {
                scriptResult.addSRObject(currSRO);
            }
            if (searchResult.getFoundYears() != null) {
                log.debug("pushResultObjects adding found years " + searchResult.getFoundYears());
                scriptResult.getFoundYears().addAll(searchResult.getFoundYears());
            }
        } else {
            log.error("pushResultObjects Either searchResult is null or it has no SR Objects");
        }
    }

    /**
     * This method pushes all the SRO from this search result to the script result.
     * But this method removes all the views which names are not in viewNames.
     * For example:
     * In this search result we have 3 SRO
     * 1 SRO Views [AA, BB, CC]
     * 2 SRO Views [AA, BB, CC]
     * 3 SRO Views [AA, BB, CC]
     *
     * viewNames: [AA, BB]
     * The existing SROs will be cloned and in the clones the views CC wil be removed. The following SROs
     * will be pushed to the script result (added to the SRO in the script result):
     * 1 SRO Views [AA, BB]
     * 2 SRO Views [AA, BB]
     * 3 SRO Views [AA, BB]
     * The method with varargs is provided for convenience in scripts
     * @param viewNames the names of the views to keep in the SROs
     */
    public void pushResultObjects(String...viewNames) {
        if (viewNames != null) {
            pushResultObjects(Arrays.asList(viewNames));
        } else {
            pushResultObjects((List<String>)null);
        }
    }

    /**
     * This method pushes all the SRO from this search result to the script result.
     * But this method removes all the views which names are not in viewNames.
     * For example:
     * In this search result we have 3 SRO
     * 1 SRO Views [AA, BB, CC]
     * 2 SRO Views [AA, BB, CC]
     * 3 SRO Views [AA, BB, CC]
     *
     * viewNames: [AA, BB]
     * The existing SROs will be cloned and in the clones the views CC wil be removed. The following SROs
     * will be pushed to the script result (added to the SRO in the script result):
     * 1 SRO Views [AA, BB]
     * 2 SRO Views [AA, BB]
     * 3 SRO Views [AA, BB]
     * @param viewNames the names of the views to keep in the SROs
     */
    public void pushResultObjects(List<String> viewNames) {
        if (viewNames == null) {
            log.warn("pushResultObjects viewNames is null all views will be removed");
        }
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            log.debug("pushResultObjects viewNames=" + viewNames);
            for (IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> currSRO : searchResult.getSRObjects()) {
                try {
                    IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> cloneSRO
                        = (IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>)currSRO.clone();
                    List<String> allViewNames = cloneSRO.getViewKeys();
                    if (viewNames != null) {
                        for (String viewName : allViewNames) {
                            if (!viewNames.contains(viewName)) {
                                cloneSRO.removeView(viewName);
                            }
                        }
                    } else {
                        cloneSRO.removeViews(allViewNames);
                    }
                    scriptResult.addSRObject(cloneSRO);
                } catch (CloneNotSupportedException e) {
                    log.error("Clone not supported?",e);
                }
            }
            if (searchResult.getFoundYears() != null) {
                log.debug("pushResultObjects adding found years " + searchResult.getFoundYears());
                scriptResult.getFoundYears().addAll(searchResult.getFoundYears());
            }

        } else {
            log.error("pushResultObjects Either searchResult is null or it has no SR Objects");
        }
    }

    /**
     * This method does the following:
     * - Obtains the SRO with the specified position (objNumber) in the list
     * - pushes it the script results (adds to the SRO in the script result)
     * @param objNumber the position of the object in the list 0-based
     */
    public void pushResultObject(int objNumber) {
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            log.debug("pushResultObject SOName=" + searchResult.getSearchObjectName() + " number=" + objNumber);
            if ((objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
                IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> objToPush = searchResult.getSRObjects().get(objNumber);
                scriptResult.addSRObject(objToPush);
                if (objToPush.getDate() != null) {
                    log.debug("pushResultObject adding found years " + searchResult.getFoundYears());
                    scriptResult.getFoundYears().add(objToPush.getDate().getYear());
                }
            } else {
                log.error("pushResultObject Incorrect objNumber " + objNumber);
            }
        } else {
            log.error("pushResultObject Either searchResult is null or it has no SR Objects");
        }
    }

    /**
     * This method does the following:
     * - Obtains the SRO with the specified position (objNumber) in the list
     * - clones it. Removes all the views which names are not in viewNames
     * - pushes it the script results (adds to the SRO in the script result)
     * The method with varargs is provided for convenience in scripts
     * @param objNumber the position of the object in the list 0-based
     * @param viewNames the list of names to keep in the current SRO other views are deleted
     */
    public void pushResultObject(int objNumber, String...viewNames) {
        if (viewNames != null) {
            pushResultObject(objNumber, Arrays.asList(viewNames));
        } else {
            pushResultObject(objNumber, (List<String>)null);
        }
    }

    /**
     * This method does the following:
     * - Obtains the SRO with the specified position (objNumber) in the list
     * - clones it. Removes all the views which names are not in viewNames
     * - pushes it the script results (adds to the SRO in the script result)
     * @param objNumber the position of the object in the list 0-based
     * @param viewNames the list of names to keep in the current SRO other views are deleted
     */
    public void pushResultObject(int objNumber, List<String> viewNames) {
        if (viewNames == null) {
            log.warn("pushResultObjects viewNames is null all views will be removed");
        }
        if ((searchResult != null) && (searchResult.getSRObjects() != null) && (!searchResult.getSRObjects().isEmpty())) {
            if ((objNumber >= 0) && (objNumber < searchResult.getSRObjects().size())) {
                try {
                    IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String> cloneSRO
                        = (IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>)((IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>)searchResult.getSRObjects().get(objNumber)).clone();
                    List<String> allViewNames = cloneSRO.getViewKeys();
                    if (viewNames != null) {
                        for (String viewName : allViewNames) {
                            if (!viewNames.contains(viewName)) {
                                cloneSRO.removeView(viewName);
                            }
                        }
                    } else {
                        cloneSRO.removeViews(allViewNames);
                    }
                    scriptResult.addSRObject(cloneSRO);
                    if (cloneSRO.getDate() != null) {
                        log.debug("pushResultObject adding found years " + searchResult.getFoundYears());
                        scriptResult.getFoundYears().add(cloneSRO.getDate().getYear());
                    }
                } catch (CloneNotSupportedException e) {
                    log.error("",e);
                }

            } else {
                log.error("pushResultObject Incorrect objNumber " + objNumber);
            }
        } else {
            log.error("pushResultObject Either searchResult is null or it has no SR Objects");
        }
    }
}
