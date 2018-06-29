package org.eclipselabs.real.core.distrib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public class DistribNodeRoot<R,A extends IDistribAccumulator<R,F,E>,F,E> implements IDistribRoot<R, A, F, E> {

    private static final Logger log = LogManager.getLogger(DistribNodeRoot.class);
    private A accumulator;

    private ExecutorService executorService;

    private List<IDistribNode<R, A, F, E>> nodeChildren = Collections.synchronizedList(new ArrayList<>());

    private Runnable lockTask;

    private Runnable unlockTask;

    private TimeUnitWrapper executionTimeout;

    private ExecutorService lockingES = Executors.newSingleThreadExecutor(new NamedThreadFactory("DistribLockThread"));

    public static class TimeRecorder {
        public static final String BEGIN = "BeginTime";
        public static final String AFTER_LOCK = "AfterLock";
        public static final String AFTER_TASKS_SUBMITTED = "AfterTasksSubmitted";
        public static final String AFTER_EXECUTION = "AfterExecution";
        public static final String AFTER_UNLOCK = "AfterUnLock";
        public static final String END = "End";

        private Map<String, Double> timeValues = new HashMap<>();

        public void putTime(String key, Double val) {
            timeValues.put(key, val);
        }

        public Double getTime(String key) {
            return timeValues.get(key);
        }
    }

    public DistribNodeRoot(A acc) {
        accumulator = acc;
    }

    /**
     * Run with a preconfigured executor service
     * @param es - the preconfigured executor service that this distribution system will use
     */
    public DistribNodeRoot(A acc, ExecutorService es) {
        this(acc);
        executorService = es;
    }

    /**
     * A thread pool will be configured with the specified number.
     * Currently a cached thread pool is configured with
     *    core threads = threadNum
     *    max threads  = threadNum*2
     *    the name format "Distrib-"
     * @param threadNum
     */
    public DistribNodeRoot(A acc, int threadNum, String threadName) {
        this(acc);
        executorService = new ThreadPoolExecutor(threadNum, threadNum*2, 120, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new NamedThreadFactory(threadName));
    }

    @Override
    public synchronized List<IDistribNode<R, A, F, E>> getNodeChildren() {
        return nodeChildren;
    }

    @Override
    public synchronized void addNodeChildren(List<IDistribNode<R, A, F, E>> lst) {
        nodeChildren.addAll(lst);
    }

    @Override
    public synchronized void setNodeChildren(List<IDistribNode<R, A, F, E>> lst) {
        nodeChildren.clear();
        nodeChildren.addAll(lst);
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public CompletableFuture<A> execute() {
        TimeRecorder recorder = new TimeRecorder();

        CompletableFuture<Void> lockFT;
        if (lockTask != null) {
            lockFT = CompletableFuture.runAsync(() -> {
                        double beginTime = System.currentTimeMillis();
                        log.debug("Start time " + beginTime);
                        recorder.putTime(TimeRecorder.BEGIN, beginTime);
                    }, lockingES)
                .thenRunAsync(lockTask, lockingES)
                .thenRunAsync(() -> {
                        double afterLockTime = System.currentTimeMillis();
                        recorder.putTime(TimeRecorder.AFTER_LOCK, afterLockTime);
                        log.debug("Time acquiring locks " + (afterLockTime - recorder.getTime(TimeRecorder.BEGIN))/1000);
                });
        } else {
            lockFT = CompletableFuture.runAsync(() -> {} , lockingES);
        }
        CompletableFuture<Void> childrenExec = lockFT.thenRunAsync(new Runnable() {
            @Override
            public void run() {
                CompletableFuture<Void>[] subFuturesNodes = new CompletableFuture[nodeChildren.size()];
                for (int i = 0; i < nodeChildren.size(); i++) {
                    subFuturesNodes[i] = nodeChildren.get(i).executeChildren();
                }
                CompletableFuture<Void> ftNodes = CompletableFuture.allOf(subFuturesNodes);
                double afterSubmitTime = System.currentTimeMillis();
                recorder.putTime(TimeRecorder.AFTER_TASKS_SUBMITTED, afterSubmitTime);
                log.debug("Time submitting tasks " + (afterSubmitTime - recorder.getTime(TimeRecorder.AFTER_LOCK))/1000);
                try {
                    if (executionTimeout != null) {
                        ftNodes.get(executionTimeout.getTimeout(), executionTimeout.getTimeUnit());
                    } else {
                        ftNodes.get();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Nodes future not executed", e);
                } catch (TimeoutException e) {
                    log.error("Execution not completed in time " + executionTimeout.getTimeout(), e);
                    /*
                     * Cancel all the futures that have not completed before the timeout
                     */
                    for (CompletableFuture<Void> ftr : subFuturesNodes) {
                        if (!ftr.isDone()) {
                            ftr.cancel(true);
                        }
                    }
                }
                double afterExecTime = System.currentTimeMillis();
                recorder.putTime(TimeRecorder.AFTER_EXECUTION, afterExecTime);
                log.debug("Time executing " + (afterExecTime - afterSubmitTime)/1000);
            }
        });

        CompletableFuture<Void> unlockFT;
        if (unlockTask != null) {
            // execute unlock even if the previous stage completed exceptionally
            unlockFT = childrenExec.whenCompleteAsync((Void x, Throwable t) -> {
                unlockTask.run();
            });
        } else {
            unlockFT = childrenExec.thenRunAsync(() -> {});
        }

        return unlockFT.thenApplyAsync(t -> accumulator);
    }

    @Override
    public void addLockingParams(Runnable lockFn, Runnable unlockFn) {
        lockTask = lockFn;
        unlockTask = unlockFn;
    }

    @Override
    public int getLeafCount() {
        return nodeChildren.stream().mapToInt(IDistribNode::getLeafCount).sum();
    }

    @Override
    public int getTotalTasks() {
        return getLeafCount();
    }

    public TimeUnitWrapper getExecutionTimeout() {
        return executionTimeout;
    }

    @Override
    public void setExecutionTimeout(TimeUnitWrapper executionTimeout) {
        this.executionTimeout = executionTimeout;
    }


}

