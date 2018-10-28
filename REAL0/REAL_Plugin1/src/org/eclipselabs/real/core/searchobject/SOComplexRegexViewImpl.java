package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.regex.IMatcherWrapper;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.SRComplexRegexViewImpl;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.SROComplexRegexViewImpl;
import org.eclipselabs.real.core.util.FindTextResult;

public class SOComplexRegexViewImpl extends SearchObjectImpl<ISRComplexRegexView, ISROComplexRegexView> implements ISOComplexRegexView {
    private static final Logger log = LogManager.getLogger(SOComplexRegexViewImpl.class);
    protected List<Object> viewObjects = new ArrayList<>();

    public SOComplexRegexViewImpl(String aName) {
        super(SearchObjectType.COMPLEX_REGEX_VIEW, aName);
    }

    @Override
    public void add(Object viewObject) {
        viewObjects.add(viewObject);
    }

    @Override
    public ISRComplexRegexView performSearch(PerformSearchRequest request) throws IncorrectPatternException {
        // do not calculate the replace table the parent SO will pass it in the request getCustomReplaceTable()
        Integer finalRegexFlags = regexFlags;
        if (request.getCustomRegexFlags() != null) {
            finalRegexFlags = request.getCustomRegexFlags();
        }
        StringBuilder sbRes = new StringBuilder();
        int startPos = request.getText().length();
        int endPos = 0;
        if (viewObjects != null) {
            for (Object viewObj : viewObjects) {
                if (viewObj instanceof String) {
                    sbRes.append(getViewString((String)viewObj, request.getDynamicReplaceParams()));
                } else if (viewObj instanceof IRealRegex) {
                    String lastFound = null;
                    IRealRegex currReg = (IRealRegex)viewObj;
                    try {
                        IMatcherWrapper mtw = currReg.getMatcherWrapper(request.getText(), request.getDynamicReplaceParams(), finalRegexFlags);
                        while (mtw.find()) {
                            FindTextResult foundStr = mtw.getResult();
                            sbRes.append(foundStr.getStrResult());
                            lastFound = foundStr.getStrResult();
                            if (foundStr.getStartPos() < startPos) {
                                startPos = foundStr.getStartPos();
                            }
                            if (foundStr.getEndPos() > endPos) {
                                endPos = foundStr.getEndPos();
                            }
                        }
                    } catch (IncorrectPatternException e) {
                        log.error("Caught exception last statement found " + lastFound
                                + " regex searched " + ((IRealRegex)viewObj).getPatternString(request.getDynamicReplaceParams()), e);
                        throw e;
                    }
                }
            }
        } else {
            log.warn("performSearch viewObjects is null");
        }
        List<ISROComplexRegexView> sroList = new ArrayList<>();
        sroList.add(new SROComplexRegexViewImpl(sbRes.toString(), startPos, endPos));
        return new SRComplexRegexViewImpl(getSearchObjectName(), request.getDynamicReplaceParams(), sroList);
    }

    public String getViewString(String origViewStr, Map<String, String> replaceTable) {
        String tmpRegex = origViewStr;
        if ((replaceTable != null) && (!replaceTable.isEmpty())) {
            for (Map.Entry<String,String> currEntry : replaceTable.entrySet()) {
                tmpRegex = tmpRegex.replace(currEntry.getKey(), currEntry.getValue());
            }
        }
        return tmpRegex;
    }

    @Override
    public List<Object> getViewObjects() {
        return viewObjects;
    }

    @Override
    public void setViewObjects(List<Object> newViewObjects) {
        viewObjects = newViewObjects;
    }

    @Override
    public ISearchObject<ISRComplexRegexView, ISROComplexRegexView> clone() throws CloneNotSupportedException {
        SOComplexRegexViewImpl cloneObj = (SOComplexRegexViewImpl)super.clone();
        if (viewObjects != null) {
            List<Object> newViewObjects = new ArrayList<>();
            for (Object viewObj : viewObjects) {
                if (viewObj instanceof String) {
                    newViewObjects.add(viewObj);
                } else if (viewObj instanceof IRealRegex) {
                    newViewObjects.add(((IRealRegex)viewObj).clone());
                }
            }
            cloneObj.viewObjects = newViewObjects;
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nSOComplexRegexViewImpl [name=" + getSearchObjectName() + "\n");
        for (Object viewObj : viewObjects) {
            sb.append(viewObj);
        }
        sb.append("]");
        return sb.toString();
    }

}
