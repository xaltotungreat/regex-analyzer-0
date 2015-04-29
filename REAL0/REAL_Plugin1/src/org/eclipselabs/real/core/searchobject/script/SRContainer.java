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

    public boolean isEmpty() {
        boolean emptyRes = false;
        if ((searchResult == null) || (searchResult.getSRObjects() == null) || (searchResult.getSRObjects().isEmpty())) {
            emptyRes = true;
        }
        return emptyRes;
    }

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

    public int getResultsCount() {
        int res = 0;
        if ((searchResult != null) && (searchResult.getSRObjects() != null)) {
            res = searchResult.getSRObjects().size();
        } else {
            log.warn("getResultsCount searchresult is null or the SR List is null");
        }
        return res;
    }

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

    public void updateReplaceMap(int objNumber, String...viewNames) {
        if (viewNames != null) {
            updateReplaceMap(objNumber, Arrays.asList(viewNames));
        } else {
            log.warn("updateReplaceMap resNumber=" + objNumber + " no view names nothing to update");
        }
    }

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

    public void pushResultObjects(String...viewNames) {
        if (viewNames != null) {
            pushResultObjects(Arrays.asList(viewNames));
        } else {
            pushResultObjects((List<String>)null);
        }
    }

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

    public void pushResultObject(int objNumber, String...viewNames) {
        if (viewNames != null) {
            pushResultObject(objNumber, Arrays.asList(viewNames));
        } else {
            pushResultObject(objNumber, (List<String>)null);
        }
    }

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
