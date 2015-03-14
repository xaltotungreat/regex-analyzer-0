package org.eclipselabs.real.core.searchobject;

import java.util.Map;

import org.eclipselabs.real.core.util.RealPredicate;

public interface ISearchProgressMonitor {

    public static final String CUSTOM_PROGRESS_KEY_OUT_OF_ORDER = "OutOfOrder";
    
    public void setCurrentSOName(String soName);
    public String getCurrentSOName();
    public void setCompletionPredicate(RealPredicate<ISearchProgressMonitor> completionPred);
    
    public void incrementObjectsFound();
    public void incrementObjectsFound(Integer incrementValue);
    public Integer getObjectsFound();
    
    public void setTotalWork(Integer total);
    public Integer getTotalWork();
    
    public void incrementCompletedWork();
    public void incrementCompletedWork(Integer incrementValue);
    public Integer getCompletedWork();
    public void resetCompletedWork();
    
    public Integer getTotalSOFiles();
    public void setTotalSOFiles(Integer newFilesCount);
    
    public void incrementCompletedSOFiles();
    public void incrementCompletedSOFiles(Integer incrementValue);
    public Integer getCompletedSOFiles();
    public void resetCompletedSOFiles();
    
    public Boolean isComplete();
    public void setComplete(Boolean newValue);
    public void setCustomNVP(String aName, String aValue);
    public String getCustomNVPValue(String aName);
    public Map<String,String> getAllCustomNVPs();
    
    public boolean isSearchCancelled();
    public void setCancelled(boolean newVal);
}
