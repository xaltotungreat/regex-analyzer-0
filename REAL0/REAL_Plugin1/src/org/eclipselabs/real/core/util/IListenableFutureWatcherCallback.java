package org.eclipselabs.real.core.util;

import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;

public interface IListenableFutureWatcherCallback<V> {

    public List<ListenableFuture<V>> submitTasks(ListenableFutureWatcher<V> watcher);
    public void executionComplete(List<ListenableFuture<V>> currentFutures);
}
