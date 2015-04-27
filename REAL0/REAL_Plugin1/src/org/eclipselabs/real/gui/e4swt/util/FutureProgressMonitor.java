package org.eclipselabs.real.gui.e4swt.util;

import java.util.concurrent.CompletableFuture;

public class FutureProgressMonitor<V> {

    protected volatile CompletableFuture<V> future;
    protected volatile Integer totalProgress;
    protected volatile Integer currentProgress;

    public FutureProgressMonitor() {
        // TODO Auto-generated constructor stub
    }

    public FutureProgressMonitor(CompletableFuture<V> aFuture, Integer totProg, Integer currProg) {
        future = aFuture;
        totalProgress = totProg;
        currentProgress = currProg;
    }

    public CompletableFuture<V> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<V> future) {
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
