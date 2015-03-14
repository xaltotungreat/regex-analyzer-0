package org.eclipselabs.real.core.searchobject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipselabs.real.core.util.RealPredicate;

public class SearchProgressMonitorImpl implements ISearchProgressMonitor {

    protected volatile String currentSOName;
    protected volatile AtomicInteger objFound = new AtomicInteger();
     
    protected volatile Integer totalSOCount;
    protected volatile Object completedSOCountLock = new Object();
    protected volatile AtomicInteger completedSOCount = new AtomicInteger(0);
    protected volatile Integer totalSOFilesCount;
    protected volatile Object completedSOFilesCountLock = new Object();
    protected volatile AtomicInteger completedSOFilesCount = new AtomicInteger(0);
    protected volatile Object completeLock = new Object();
    protected volatile AtomicBoolean complete = new AtomicBoolean(false);
    
    protected volatile AtomicBoolean calcelled = new AtomicBoolean(false);
    
    protected RealPredicate<ISearchProgressMonitor> completionPredicate;
    
    protected volatile Map<String, String> customNVP = new ConcurrentHashMap<>();
    
    public SearchProgressMonitorImpl() {
        
    }
    
    public SearchProgressMonitorImpl(Integer totalWorkAmount) {
        totalSOCount = totalWorkAmount;
    }
    
    @Override
    public void setCurrentSOName(String soName) {
        currentSOName = soName;
    }

    @Override
    public String getCurrentSOName() {
        return currentSOName;
    }
    
    @Override
    public synchronized void incrementObjectsFound() {
        objFound.incrementAndGet();
    }

    @Override
    public synchronized void incrementObjectsFound(Integer incrementValue) {
        for (int i = 0; i < incrementValue; i++) {
            objFound.incrementAndGet();
        }
    }

    @Override
    public Integer getObjectsFound() {
        return objFound.get();
    }

    @Override
    public synchronized void setTotalWork(Integer total) {
        totalSOCount = total;
    }
    
    @Override
    public synchronized Integer getTotalWork() {
        return totalSOCount;
    }
    
    @Override
    public void incrementCompletedWork() {
        synchronized(completedSOCountLock) {
            completedSOCount.incrementAndGet();
            if (completionPredicate != null) {
                synchronized (completeLock) {
                    boolean predRes = completionPredicate.test(this);
                    if (predRes != complete.get()) {
                        complete.set(predRes);
                    }
                }
            }
        }
    }

    @Override
    public void incrementCompletedWork(Integer incrementValue) {
        synchronized(completedSOCountLock) {
            for (int i = 0; i < incrementValue; i++) {
                completedSOCount.incrementAndGet();
            }
            if (completionPredicate != null) {
                synchronized (completeLock) {
                    boolean predRes = completionPredicate.test(this);
                    if (predRes != complete.get()) {
                        complete.set(predRes);
                    }
                }
            }
        }
    }

    @Override
    public Integer getCompletedWork() {
        synchronized(completedSOCountLock) {
            return completedSOCount.get();
        }
    }
    
    @Override
    public void resetCompletedWork() {
        synchronized(completedSOCountLock) {
            completedSOCount.compareAndSet(completedSOCount.get(), 0);
        }
    }


    @Override
    public Integer getTotalSOFiles() {
        return totalSOFilesCount;
    }

    @Override
    public void setTotalSOFiles(Integer newFilesCount) {
        totalSOFilesCount = newFilesCount;
    }

    @Override
    public synchronized void incrementCompletedSOFiles() {
        synchronized (completedSOFilesCountLock) {
            completedSOFilesCount.incrementAndGet();
            if (completionPredicate != null) {
                synchronized (completeLock) {
                    boolean predRes = completionPredicate.test(this);
                    if (predRes != complete.get()) {
                        complete.set(predRes);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void incrementCompletedSOFiles(Integer incrementValue) {
        synchronized (completedSOFilesCountLock) {
            for (int i = 0; i < incrementValue; i++) {
                completedSOFilesCount.incrementAndGet();
            }
            if (completionPredicate != null) {
                synchronized (completeLock) {
                    boolean predRes = completionPredicate.test(this);
                    if (predRes != complete.get()) {
                        complete.set(predRes);
                    }
                }
            }
        }
    }
    
    @Override
    public Integer getCompletedSOFiles() {
        synchronized (completedSOFilesCountLock) {
            return completedSOFilesCount.get();
        }
    }
    
    @Override
    public void resetCompletedSOFiles() {
        synchronized (completedSOFilesCountLock) {
            completedSOFilesCount.compareAndSet(completedSOFilesCount.get(), 0);
        }
    }

    @Override
    public Boolean isComplete() {
        synchronized (completeLock) {
            return complete.get();
        }
    }
    
    @Override
    public void setComplete(Boolean newValue) {
        synchronized (completeLock) {
            complete.set(newValue);
        }
    }

    @Override
    public void setCustomNVP(String aName, String aValue) {
        customNVP.put(aName, aValue);
    }

    @Override
    public String getCustomNVPValue(String aName) {
        return customNVP.get(aName);
    }

    @Override
    public Map<String, String> getAllCustomNVPs() {
        return new HashMap<>(customNVP);
    }

    @Override
    public void setCompletionPredicate(RealPredicate<ISearchProgressMonitor> completionPred) {
        completionPredicate = completionPred;
    }

    @Override
    public synchronized boolean isSearchCancelled() {
        return calcelled.get();
    }

    @Override
    public synchronized void setCancelled(boolean newVal) {
        synchronized (completeLock) {
            calcelled = new AtomicBoolean(newVal);
            if (newVal) {
                complete.set(newVal);
            }
        }
    }

    @Override
    public String toString() {
        return "SearchProgressMonitorImpl [currentSOName=" + currentSOName + ", objFound=" + objFound 
                + ", totalSOFilesCount=" + totalSOFilesCount + ", completedSOFilesCount=" + completedSOFilesCount
                + ", complete=" + complete + ", calcelled=" + calcelled 
                + ", completionPredicate=" + completionPredicate + ", customNVP=" + customNVP + "]";
    }

}
