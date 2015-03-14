package org.eclipselabs.real.core.logfile.task;

import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.util.ITaskWatcherCallback;
import org.eclipselabs.real.core.util.TaskWatcher;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

public class LogFileTaskExecutor<V,R> {

    private static final Logger log = LogManager.getLogger(LogFileTaskExecutor.class);

    protected String operationName;
    protected List<? extends LogFileTask<V,R>> theLogTasks;
    protected SettableFuture<R> theMainFuture;
    protected R theResultAggregate;
    protected List<Lock> theLocks;
    protected TimeUnitWrapper theWaitTO;
    protected TimeUnitWrapper theExecutionTO;
    ListeningExecutorService executorService;
    
    public  LogFileTaskExecutor(String opName, ListeningExecutorService execService, List<? extends LogFileTask<V,R>> tasksList, 
            SettableFuture<R> aMainFuture, R emptyResult, 
            List<Lock> locksList,
            TimeUnitWrapper waitTO, TimeUnitWrapper execTO) {
        operationName = opName;
        theLogTasks = tasksList;
        theMainFuture = aMainFuture;
        theResultAggregate = emptyResult;
        theLocks = locksList;
        theWaitTO = waitTO;
        theExecutionTO = execTO;
        executorService = execService;
    }
    
    public void execute() {
        if ((theLogTasks != null) && (!theLogTasks.isEmpty())) {
            final TaskWatcher watcher = new TaskWatcher(operationName, theLocks, new ITaskWatcherCallback() {
                
                @Override
                public void submitTasks(TaskWatcher watcher) {
                    submitLogFileTasks(watcher);
                }
                
                @Override
                public void executionComplete() {
                    log.debug("Execution complete ");
                    theMainFuture.set(theResultAggregate);
                }
            });
            watcher.startWatch(theWaitTO, theExecutionTO);
        } else {
            log.error("No tasks to execute");
        }
    }
    
    public void submitLogFileTasks(final TaskWatcher watcher) {
        for (final LogFileTask<V,R> logTask : theLogTasks) {
            ListenableFuture<V> currFuture = executorService.submit(logTask);
            watcher.incrementAndGetSubmitted();
            log.debug("LogTaskSubmitted " + watcher.getSubmitted());
            Futures.addCallback(currFuture, new FutureCallback<V>() {
                @Override
                public void onSuccess(V arg0) {
                    logTask.getAddTaskResult().addResult(arg0, theResultAggregate);
                    watcher.incrementAndGetFinished();
                    log.debug("LogTaskFinished success submitted=" + watcher.getSubmitted() + " finished=" + watcher.getFinished());
                }

                @Override
                public void onFailure(Throwable arg0) {
                    log.error("Future error", arg0);
                    watcher.incrementAndGetFinished();
                    log.debug("LogTaskFinished failure submitted=" + watcher.getSubmitted() + " finished=" + watcher.getFinished());
                }
            });
        }
    }

}
