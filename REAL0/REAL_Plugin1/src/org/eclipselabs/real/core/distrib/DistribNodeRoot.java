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
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

class DistribNodeRoot<R,A extends IDistribAccumulator<R,F,E>,F,E> implements IDistribRoot<R, A, F, E> {

    private static final Logger log = LogManager.getLogger(DistribNodeRoot.class);
    private A accumulator;

    private ExecutorService executorService;

    private List<IDistribNode<R, A, F, E>> nodeChildren = Collections.synchronizedList(new ArrayList<>());

    private Runnable lockTask;

    private Runnable unlockTask;

    private List<Runnable> afterLockRunSync;
    private List<Runnable> afterLockRunAsync;

    private List<Runnable> afterExecRunSync;
    private List<Runnable> afterExecRunAsync;

    private TimeUnitWrapper executionTimeout;

    private ExecutorService lockingES = Executors.newSingleThreadExecutor(new NamedThreadFactory("DistribLockThread"));

    public static class OperationRecorder {
        public static final String BEGIN = "BeginTime";
        public static final String AFTER_LOCK = "AfterLock";
        public static final String AFTER_TASKS_SUBMITTED = "AfterTasksSubmitted";
        public static final String AFTER_EXECUTION = "AfterExecution";
        public static final String AFTER_UNLOCK = "AfterUnLock";
        public static final String END = "End";

        private Map<String, Double> timeValues = new HashMap<>();

        private boolean allLocked = false;

        private Throwable execException;

        public void putTime(String key, Double val) {
            timeValues.put(key, val);
        }

        public Double getTime(String key) {
            return timeValues.get(key);
        }

        public boolean isAllLocked() {
            return allLocked;
        }

        public void setAllLocked(boolean allLocked) {
            this.allLocked = allLocked;
        }

        public Throwable getExecException() {
            return execException;
        }

        public void setExecException(Throwable execException) {
            this.execException = execException;
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
        executorService = Executors.newFixedThreadPool(threadNum, new NamedThreadFactory(threadName));
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
        OperationRecorder recorder = new OperationRecorder();

        CompletableFuture<Void> lockFT;
        if (lockTask != null) {
            lockFT = CompletableFuture.runAsync(() -> {
                        double beginTime = System.currentTimeMillis();
                        log.debug("execute Start time " + beginTime);
                        recorder.putTime(OperationRecorder.BEGIN, beginTime);
                    }, lockingES)
                .thenRunAsync(lockTask, lockingES)
                .whenCompleteAsync((Void runbl, Throwable t) -> {
                    double afterLockTime = System.currentTimeMillis();
                    recorder.putTime(OperationRecorder.AFTER_LOCK, afterLockTime);
                    if (t != null) {
                        recorder.setAllLocked(false);
                        recorder.setExecException(t);
                        log.debug("execute Time locks not acquired " + (afterLockTime - recorder.getTime(OperationRecorder.BEGIN))/1000);
                    } else {
                        recorder.setAllLocked(true);
                        log.debug("execute Time acquiring locks " + (afterLockTime - recorder.getTime(OperationRecorder.BEGIN))/1000);
                    }
                }, lockingES);
        } else {
            lockFT = CompletableFuture.runAsync(() -> {}, lockingES);
        }

        CompletableFuture<Void> childrenExec = lockFT.thenRunAsync(new Runnable() {
            @Override
            public void run() {
                if (recorder.isAllLocked()) {
                    // run the auxiliary tasks after the locks have been locked
                    if ((afterLockRunSync != null) && (!afterLockRunSync.isEmpty())) {
                        for (Runnable rn : afterLockRunSync) {
                            rn.run();
                        }
                    }
                    CompletableFuture<Void> afterLockCF = CompletableFuture.completedFuture(null);
                    if ((afterLockRunAsync != null) && (!afterLockRunAsync.isEmpty())) {
                        for (Runnable rn : afterLockRunAsync) {
                            afterLockCF = afterLockCF.thenRunAsync(rn, executorService);
                        }
                    }
                    try {
                        afterLockCF.get();
                    } catch (InterruptedException e) {
                        log.error("", e);
                        // Restore interrupted state in accordance with the Sonar rule squid:S2142
                        Thread.currentThread().interrupt();
                    } catch(ExecutionException e1) {
                        log.error("", e1);
                    }
                    CompletableFuture<Void>[] subFuturesNodes = new CompletableFuture[nodeChildren.size()];
                    for (int i = 0; i < nodeChildren.size(); i++) {
                        subFuturesNodes[i] = nodeChildren.get(i).executeChildren();
                    }
                    CompletableFuture<Void> ftNodes = CompletableFuture.allOf(subFuturesNodes);
                    double afterSubmitTime = System.currentTimeMillis();
                    recorder.putTime(OperationRecorder.AFTER_TASKS_SUBMITTED, afterSubmitTime);
                    log.debug("execute Time submitting tasks " + (afterSubmitTime - recorder.getTime(OperationRecorder.AFTER_LOCK))/1000);
                    try {
                        if (executionTimeout != null) {
                            log.debug("Execute timeout " + executionTimeout);
                            ftNodes.get(executionTimeout.getTimeout(), executionTimeout.getTimeUnit());
                        } else {
                            log.debug("Execution no timeout");
                            ftNodes.get();
                        }
                    } catch (InterruptedException e) {
                        log.error("", e);
                        // Restore interrupted state in accordance with the Sonar rule squid:S2142
                        Thread.currentThread().interrupt();
                    } catch(ExecutionException e1) {
                        log.error("", e1);
                    } catch (TimeoutException e) {
                        log.error("execute Execution not completed in time " + executionTimeout.getTimeout(), e);
                        /*
                         * Cancel all the futures that have not completed before the timeout
                         */
                        recorder.setExecException(e);
                        for (CompletableFuture<Void> ftr : subFuturesNodes) {
                            if (!ftr.isDone()) {
                                ftr.cancel(true);
                            }
                        }
                    }
                    // run the auxiliary tasks after the locks have been locked
                    if ((afterExecRunSync != null) && (!afterExecRunSync.isEmpty())) {
                        for (Runnable rn : afterExecRunSync) {
                            rn.run();
                        }
                    }
                    CompletableFuture<Void> afterExecCF = CompletableFuture.completedFuture(null);
                    if ((afterExecRunAsync != null) && (!afterExecRunAsync.isEmpty())) {
                        for (Runnable rn : afterExecRunAsync) {
                            afterExecCF = afterLockCF.thenRunAsync(rn, executorService);
                        }
                    }
                    try {
                        afterExecCF.get();
                    } catch (InterruptedException e) {
                        log.error("", e);
                        // Restore interrupted state in accordance with the Sonar rule squid:S2142
                        Thread.currentThread().interrupt();
                    } catch(ExecutionException e1) {
                        log.error("", e1);
                    }
                    double afterExecTime = System.currentTimeMillis();
                    recorder.putTime(OperationRecorder.AFTER_EXECUTION, afterExecTime);
                    log.debug("execute Time executing " + (afterExecTime - afterSubmitTime)/1000);
                } else {
                    log.error("execute Not finished execution", recorder.getExecException());
                }
            }
        }, lockingES);

        CompletableFuture<Void> unlockFT;
        if (unlockTask != null) {
            // execute unlock even if the previous stage completed exceptionally
            unlockFT = childrenExec.whenCompleteAsync((Void x, Throwable t) -> unlockTask.run(), lockingES);
        } else {
            unlockFT = childrenExec;
        }
        return unlockFT.thenApplyAsync(t -> accumulator, lockingES);
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

    @Override
    public void setAfterLockRun(List<Runnable> afterLockRunSync, List<Runnable> afterLockRunAsync) {
        this.afterLockRunSync = afterLockRunSync;
        this.afterLockRunAsync = afterLockRunAsync;
    }

    @Override
    public void setAfterExecRun(List<Runnable> afterExecRunS, List<Runnable> afterExecRunA) {
        this.afterExecRunSync = afterExecRunS;
        this.afterExecRunAsync = afterExecRunA;
    }

    @Override
    public A getAccumulator() {
        return accumulator;
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
        lockingES.shutdown();
    }


}
