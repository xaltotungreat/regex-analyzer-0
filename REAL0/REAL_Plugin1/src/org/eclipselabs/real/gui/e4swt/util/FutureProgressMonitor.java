package org.eclipselabs.real.gui.e4swt.util;

import com.google.common.util.concurrent.ListenableFuture;

public class FutureProgressMonitor<V> {

    protected volatile ListenableFuture<V> future;
    protected volatile Integer totalProgress;
    protected volatile Integer currentProgress;
    
    public FutureProgressMonitor() {
        // TODO Auto-generated constructor stub
    }
    
    public FutureProgressMonitor(ListenableFuture<V> aFuture, Integer totProg, Integer currProg) {
        future = aFuture;
        totalProgress = totProg;
        currentProgress = currProg;
    }

    public ListenableFuture<V> getFuture() {
        return future;
    }

    public void setFuture(ListenableFuture<V> future) {
        this.future = future;
    }

    public synchronized Integer getTotalProgress() {
        return totalProgress;
    }

    public synchronized void setTotalProgress(Integer totalProgress) {
        this.totalProgress = totalProgress;
    }

    public synchronized Integer getCurrentProgress() {
        return currentProgress;
    }

    public synchronized void setCurrentProgress(Integer currentProgress) {
        this.currentProgress = currentProgress;
    }

}
