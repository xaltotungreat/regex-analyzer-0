package org.eclipselabs.real.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ListeningExecutorService;

public class CompletableFutureWatcher<V> {
    private static final Logger log = LogManager.getLogger(CompletableFutureWatcher.class);
    protected String name;
    private volatile Thread lockingThread;
    protected AtomicInteger tasksSubmitted = new AtomicInteger();
    protected AtomicInteger tasksFinished = new AtomicInteger();
    protected volatile CompletableFutureWatcherState currentState;
    protected List<LockWrapper> theLocks = new ArrayList<LockWrapper>();
    protected List<CompletableFuture<V>> theFuturesList;
    private CompletableFuture<List<CompletableFuture<V>>> theListFuture;
    private ICompletableFutureWatcherCallback<V> theCallback;
    private ListeningExecutorService watcherExecutor;

    public static final String COMPLETABLE_FUTURE_WATCHER_THREAD_NAME = "LFWatcher";

    public static enum CompletableFutureWatcherState {
        INIT,
        SUBMITTING,
        SUBMITION_COMPLETE,
        FINISHED
    }

    public static class LockWrapper {
        protected Lock theLock;
        protected Boolean lockLocked = false;
        public LockWrapper(Lock aLock) {
            theLock = aLock;
        }
        public void setLocked(Boolean locked) {
            lockLocked = locked;
        }

        public Lock getLock() {
            return theLock;
        }

        public Boolean isLocked() {
            return lockLocked;
        }
    }

    class TimeoutLockThread<R> extends Thread {
        private TimeUnitWrapper theExecutionTO;
        private TimeUnitWrapper theWaitTO;
        private CompletableFutureWatcher<R> parentWatcher;
        //private ReentrantLock timedLock;


        public TimeoutLockThread(CompletableFutureWatcher<R> watcher, TimeUnitWrapper execTO, TimeUnitWrapper waitTO) {
            super(COMPLETABLE_FUTURE_WATCHER_THREAD_NAME + "-" + name);
            theExecutionTO = new TimeUnitWrapper(execTO.getTimeout(), execTO.getTimeUnit());
            theWaitTO = waitTO;
            parentWatcher = watcher;
        }

        @Override
        public void run() {
            try {
                double beginTime = System.currentTimeMillis();
                parentWatcher.setCurrentState(CompletableFutureWatcherState.SUBMITTING);
                for (LockWrapper currLock : theLocks) {
                    if (!currLock.lockLocked) {
                        if ((currLock.getLock().tryLock()) || (currLock.getLock().tryLock(theWaitTO.getTimeout(), theWaitTO.getTimeUnit()))) {
                            currLock.setLocked(true);
                            log.debug(name + " TaskWatcher Lock locked " + currLock);
                        } else {
                            log.error(name + " Unable to obtain lock");
                        }
                    }
                }
                double beginAddCallbackTime = System.currentTimeMillis();
                log.info("Time Acquiring locks " + (beginAddCallbackTime - beginTime)/1000);
                double beginSubmitTime = System.currentTimeMillis();
                List<CompletableFuture<R>> resultList = parentWatcher.getCallback().submitTasks(parentWatcher);
                if (resultList != null) {
                    if (parentWatcher.getListFuture() != null) {
                        parentWatcher.getListFuture().complete(resultList);
                    }
                    tasksSubmitted = new AtomicInteger(resultList.size());
                    for (CompletableFuture<R> currFuture : resultList) {
                        currFuture.handle((R arg0, Throwable t) -> {
                            if (t != null) {
                                log.error("Listenable future failed", arg0);
                            }
                            parentWatcher.incrementAndGetFinished();
                            return null;
                        });
                        /*Futures.addCallback(currFuture, new FutureCallback<R>() {

                            @Override
                            public void onSuccess(R arg0) {
                                parentWatcher.incrementAndGetFinished();
                            }

                            @Override
                            public void onFailure(Throwable arg0) {
                                log.error("Listenable future failed", arg0);
                                parentWatcher.incrementAndGetFinished();
                            }
                        });*/
                    }
                    double beginWaitTime = System.currentTimeMillis();
                    log.info("Time Submition " + (beginWaitTime - beginSubmitTime)/1000);
                    parentWatcher.setCurrentState(CompletableFutureWatcherState.SUBMITION_COMPLETE);
                    if (tasksFinished.get() < tasksSubmitted.get()) {
                        synchronized (this) {
                            log.info("Waiting for completion of timeout "
                                    + theExecutionTO.getTimeout() + " " + theExecutionTO.getTimeUnit());
                            theExecutionTO.getTimeUnit().timedWait(this, theExecutionTO.getTimeout());
                        }
                        log.debug(name + " watcher continues calling callback");
                    } else {
                        log.debug("No need to wait tasks already executed");
                    }
                    double beginCallbackTime = System.currentTimeMillis();
                    log.info("Time Execution " + (beginCallbackTime - beginWaitTime)/1000);
                    parentWatcher.getCallback().executionComplete(resultList);
                    double beginUnlockTime = System.currentTimeMillis();
                    log.info("Time Execution " + (beginUnlockTime - beginWaitTime)/1000);
                } else {
                    log.error("Null list of futures returned proceed to unlock");
                }

                for (LockWrapper currLock : theLocks) {
                    if (currLock.isLocked()) {
                        currLock.getLock().unlock();
                        currLock.setLocked(false);
                        log.debug(name + " TaskWatcher Locks unlocked " + currLock);
                    }
                }
                parentWatcher.setCurrentState(CompletableFutureWatcherState.FINISHED);
            } catch (InterruptedException e) {
                log.error(name + " TaskWatcher", e);
            } catch (Exception e) {
                log.error(name + " TaskWatcher", e);
            } finally {
                for (LockWrapper currLock : theLocks) {
                    if (currLock.isLocked()) {
                        currLock.getLock().unlock();
                        currLock.setLocked(false);
                        log.debug(name + " TaskWatcher Finally Lock unlocked " + currLock);
                    }
                }
            }
        }
    }

    public CompletableFutureWatcher(String aName, List<Lock> locksList, List<CompletableFuture<V>> futuresList) {
        name = aName;
        theFuturesList = futuresList;
        if (locksList != null) {
            for (Lock currLock : locksList) {
                theLocks.add(new LockWrapper(currLock));
            }
            log.debug(name + " Locks number " + theLocks.size());
        }
        tasksSubmitted = new AtomicInteger(theFuturesList.size());
    }

    public CompletableFutureWatcher(String aName, List<Lock> locksList, ICompletableFutureWatcherCallback<V> aCallback,
            CompletableFuture<List<CompletableFuture<V>>> listFuture, ListeningExecutorService aWatcherExecutor) {
        name = aName;
        setWatcherExecutor(aWatcherExecutor);
        setCallback(aCallback);
        setListFuture(listFuture);
        if (locksList != null) {
            for (Lock currLock : locksList) {
                theLocks.add(new LockWrapper(currLock));
            }
            log.debug(name + " Locks number " + theLocks.size());
        }
        //tasksSubmitted = new AtomicInteger(theFuturesList.size());
    }

    public void startWatch(TimeUnitWrapper waitTO, TimeUnitWrapper execTO) {
        TimeoutLockThread<V> tlt = new TimeoutLockThread<V>(this, execTO, waitTO);
        lockingThread = tlt;
        currentState = CompletableFutureWatcherState.INIT;
        tlt.start();
    }

    public int getSubmitted() {
        return tasksSubmitted.get();
    }

    public int getFinished() {
        return tasksFinished.get();
    }

    public int incrementAndGetFinished() {
        tasksFinished.incrementAndGet();
        //lastFinishedTaskTime = System.currentTimeMillis();
        log.debug(name + " Tasks finished " + tasksFinished.get());
        synchronized (lockingThread) {
            if ((currentState == CompletableFutureWatcherState.SUBMITION_COMPLETE) && (tasksFinished.get() == tasksSubmitted.get())) {
                log.debug(name + " ############ Notify came!");
                lockingThread.notify();
            }
        }
        return tasksFinished.get();
    }

    public CompletableFutureWatcherState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CompletableFutureWatcherState currentState) {
        this.currentState = currentState;
    }

    public ICompletableFutureWatcherCallback<V> getCallback() {
        return theCallback;
    }

    public void setCallback(ICompletableFutureWatcherCallback<V> theCallback) {
        this.theCallback = theCallback;
    }

    public ListeningExecutorService getWatcherExecutor() {
        return watcherExecutor;
    }

    public void setWatcherExecutor(ListeningExecutorService watcherExecutor) {
        this.watcherExecutor = watcherExecutor;
    }

    public CompletableFuture<List<CompletableFuture<V>>> getListFuture() {
        return theListFuture;
    }

    public void setListFuture(CompletableFuture<List<CompletableFuture<V>>> theListFuture) {
        this.theListFuture = theListFuture;
    }


}
