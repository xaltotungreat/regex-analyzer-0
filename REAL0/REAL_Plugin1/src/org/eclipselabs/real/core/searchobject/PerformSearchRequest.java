package org.eclipselabs.real.core.searchobject;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;

public class PerformSearchRequest implements Cloneable {

    private static final Logger log = LogManager.getLogger(PerformSearchRequest.class);
    protected String text;
    protected Map<String, String> dynamicReplaceParams;
    protected Map<ReplaceableParamKey, IReplaceableParam<?>> staticReplaceParams;
    protected ISearchProgressMonitor progMonitor;
    protected Integer customRegexFlags;
    
    public PerformSearchRequest(String logText, Map<String, String> replTable, ISearchProgressMonitor monitor, Integer flags) {
        this(logText, monitor, null, replTable, flags);
    }
    
    public PerformSearchRequest(String logText, Map<String, String> replTable, ISearchProgressMonitor monitor) {
        this(logText, replTable, monitor, null);
    }
    
    public PerformSearchRequest(String logText,  ISearchProgressMonitor monitor, Map<ReplaceableParamKey, IReplaceableParam<?>> replTable, Integer flags) {
        this(logText, monitor, replTable, null, flags);
    }
    
    public PerformSearchRequest(String logText,  ISearchProgressMonitor monitor, Map<ReplaceableParamKey, IReplaceableParam<?>> replParams, Map<String, String> replTable,
              Integer flags) {
        text = logText;
        staticReplaceParams = replParams;
        dynamicReplaceParams = replTable;
        progMonitor = monitor;
        customRegexFlags = flags;
    }
    
    public PerformSearchRequest(String logText,  ISearchProgressMonitor monitor, Map<ReplaceableParamKey, IReplaceableParam<?>> replTable) {
        this(logText,monitor, replTable, null);
    }
    
    public PerformSearchRequest getSharedMonitorCopy() {
        PerformSearchRequest clonedObj = null;
        try {
            clonedObj = (PerformSearchRequest) super.clone();
            if (dynamicReplaceParams != null) {
                Map<String, String> clonedReplaceTable = new HashMap<>();
                clonedReplaceTable.putAll(dynamicReplaceParams);
                clonedObj.setDynamicReplaceParams(clonedReplaceTable);
            }
            if (staticReplaceParams != null) {
                Map<ReplaceableParamKey, IReplaceableParam<?>> clonedParams = new HashMap<>();
                for (Map.Entry<ReplaceableParamKey, IReplaceableParam<?>> currEntry : staticReplaceParams.entrySet()) {
                    clonedParams.put(currEntry.getKey().clone(), currEntry.getValue().clone());
                }
                clonedObj.setStaticReplaceParams(clonedParams);
            }
        } catch (CloneNotSupportedException e) {
            log.error("getSharedMonitorCopy ", e);
        }
        return clonedObj;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getDynamicReplaceParams() {
        return dynamicReplaceParams;
    }

    public void setDynamicReplaceParams(Map<String, String> customReplaceTable) {
        this.dynamicReplaceParams = customReplaceTable;
    }

    public Map<ReplaceableParamKey, IReplaceableParam<?>> getStaticReplaceParams() {
        return staticReplaceParams;
    }

    public void setStaticReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> replaceParams) {
        this.staticReplaceParams = replaceParams;
    }

    public ISearchProgressMonitor getProgressMonitor() {
        return progMonitor;
    }

    public void setProgressMonitor(ISearchProgressMonitor progMonitor) {
        this.progMonitor = progMonitor;
    }

    public Integer getCustomRegexFlags() {
        return customRegexFlags;
    }

    public void setCustomRegexFlags(Integer customRegexFlags) {
        this.customRegexFlags = customRegexFlags;
    }

    @Override
    public String toString() {
        return "PerformSearchRequest [dynamicReplaceParams=" + dynamicReplaceParams 
                + ", staticReplaceParams=" + staticReplaceParams + ", progMonitor=" + progMonitor 
                + ", customRegexFlags=" + customRegexFlags + "]";
    }

}
