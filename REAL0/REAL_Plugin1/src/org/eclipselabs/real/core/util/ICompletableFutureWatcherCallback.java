package org.eclipselabs.real.core.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ICompletableFutureWatcherCallback<V> {

    public List<CompletableFuture<V>> submitTasks(CompletableFutureWatcher<V> watcher);
    public void executionComplete(List<CompletableFuture<V>> currentFutures);
}
