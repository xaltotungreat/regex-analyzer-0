package org.eclipselabs.real.core.util;

public interface ITaskWatcherCallback {
    public void submitTasks(TaskWatcher watcher);
    public void executionComplete();
}
