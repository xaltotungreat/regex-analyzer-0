package org.eclipselabs.real.core.logfile.task;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.util.ITaskWatcherCallback;
import org.eclipselabs.real.core.util.NamedLock;
import org.eclipselabs.real.core.util.TaskWatcher;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public class LogFileTaskExecutor<V,R> {

    private static final Logger log = LogManager.getLogger(LogFileTaskExecutor.class);

    protected String operationName;
    protected List<? extends LogFileTask<V,R>> theLogTasks;
    protected CompletableFuture<R> theMainFuture;
    protected R theResultAggregate;
    protected List<NamedLock> theLocks;
    protected TimeUnitWrapper theWaitTO;
    protected TimeUnitWrapper theExecutionTO;
    private ExecutorService executorService;

    public  LogFileTaskExecutor(String opName, ExecutorService execService, List<? extends LogFileTask<V,R>> tasksList,
            CompletableFuture<R> aMainFuture, R emptyResult,
            List<NamedLock> locksList,
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
                public void submitTasks(TaskWatcher watcher1) {
                    submitLogFileTasks(watcher1);
                }

                @Override
                public void executionComplete() {
                    log.debug("Execution complete ");
                    theMainFuture.complete(theResultAggregate);
                }
            });
            watcher.startWatch(theWaitTO, theExecutionTO);
        } else {
            log.error("No tasks to execute");
        }
    }

    public void submitLogFileTasks(final TaskWatcher watcher) {
        for (final LogFileTask<V,R> logTask : theLogTasks) {
            CompletableFuture<V> currFuture = CompletableFuture.supplyAsync(logTask, executorService);
            watcher.incrementAndGetSubmitted();
            log.debug("LogTaskSubmitted " + watcher.getSubmitted());
            currFuture.handle((V arg0, Throwable t) -> {
                if (arg0 != null) {
                    logTask.getAddTaskResult().addResult(arg0, theResultAggregate);
                    log.debug("LogTaskFinished success submitted=" + watcher.getSubmitted() + " finished=" + watcher.getFinished());
                }
                if (t != null) {
                    log.error("Future error", arg0);
                    log.debug("LogTaskFinished failure submitted=" + watcher.getSubmitted() + " finished=" + watcher.getFinished());
                }
                // increment in any case
                watcher.incrementAndGetFinished();
                return null;
            });
        }
    }

}
