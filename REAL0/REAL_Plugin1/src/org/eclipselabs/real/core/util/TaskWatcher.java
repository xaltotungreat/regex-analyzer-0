package org.eclipselabs.real.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskWatcher {
    private static final Logger log = LogManager.getLogger(TaskWatcher.class);
    protected String name;
    private volatile ReentrantLock theLock = new ReentrantLock();
    private volatile Thread lockingThread;
    protected AtomicInteger tasksSubmitted = new AtomicInteger();
    protected AtomicInteger tasksFinished = new AtomicInteger();
    protected volatile TaskWatcherState currentState;
    protected List<LockWrapper> theLocks;
    public static final String TASK_WATCHER_THREAD_NAME = "TaskWatcher";
    protected ITaskWatcherCallback theCallback;
    protected volatile Long lastFinishedTaskTime;

    public static enum TaskWatcherState {
        INIT,
        SUBMITTING,
        SUBMISSION_COMPLETE,
        FINISHED
    }

    public TaskWatcher(String aName, List<NamedLock> locksList, ITaskWatcherCallback completionCallback) {
        name = aName;
        currentState = TaskWatcherState.INIT;
        theCallback = completionCallback;
        theLocks = new ArrayList<LockWrapper>();
        if (locksList != null) {
            for (NamedLock currLock : locksList) {
                theLocks.add(new LockWrapper(currLock.getLock(), currLock.getLockName()));
            }
            log.debug(name + " Locks number " + theLocks.size());
        }
    }

    class TimeoutLockThread extends Thread {
        private TimeUnitWrapper theExecutionTO;
        private TimeUnitWrapper theWaitTO;
        private TaskWatcher parentWatcher;

        public TimeoutLockThread(TaskWatcher watcher, TimeUnitWrapper execTO, TimeUnitWrapper waitTO) {
            super(TASK_WATCHER_THREAD_NAME + "-" + name);
            theExecutionTO = new TimeUnitWrapper(execTO.getTimeout(), execTO.getTimeUnit());
            theWaitTO = waitTO;
            parentWatcher = watcher;
        }

        @Override
        public void run() {
            try {
                double beginTime = System.currentTimeMillis();
                for (LockWrapper currLock : theLocks) {
                    if (!currLock.lockLocked) {
                        if ((currLock.getLock().tryLock()) || (currLock.getLock().tryLock(theWaitTO.getTimeout(), theWaitTO.getTimeUnit()))) {
                            currLock.setLocked(true);
                            log.debug(name + " TaskWatcher Lock locked " + currLock.getLockName());
                        } else {
                            log.error(name + " Unable to obtain lock" + currLock.getLockName());
                        }
                    }
                }
                double beginSubmitTime = System.currentTimeMillis();
                log.info("Time Acquiring locks " + (beginSubmitTime - beginTime)/1000);
                theCallback.submitTasks(parentWatcher);
                double beginWaitTime = System.currentTimeMillis();
                log.info("Time Submition " + (beginWaitTime - beginSubmitTime)/1000);
                parentWatcher.setCurrentState(TaskWatcherState.SUBMISSION_COMPLETE);
                if (tasksFinished.get() < tasksSubmitted.get()) {
                    synchronized (this) {
                        log.info("Waiting for completion of timeout " + theExecutionTO);
                        theExecutionTO.getTimeUnit().timedWait(this, theExecutionTO.getTimeout());
                    }
                    log.debug(name + " TaskWatcher continues calling callback");
                    log.debug("After timeout All tasks " + tasksSubmitted.get() + " Finished " + tasksFinished.get());
                } else {
                    log.debug("No need to wait tasks already executed");
                }
                double beginCallbackTime = System.currentTimeMillis();
                log.info("Time Execution " + (beginCallbackTime - beginWaitTime)/1000);
                theCallback.executionComplete();
                double beginUnlockTime = System.currentTimeMillis();
                log.info("Time Callback " + (beginUnlockTime - beginCallbackTime)/1000);
                for (LockWrapper currLock : theLocks) {
                    if (currLock.isLocked()) {
                        currLock.getLock().unlock();
                        currLock.setLocked(false);
                        log.debug(name + " TaskWatcher Locks unlocked " + currLock.getLockName());
                    }
                }
                parentWatcher.setCurrentState(TaskWatcherState.FINISHED);
            } catch (InterruptedException e) {
                log.error(name + " TaskWatcher", e);
            } catch (Exception e) {
                log.error(name + " TaskWatcher", e);
            } finally {
                for (LockWrapper currLock : theLocks) {
                    if (currLock.isLocked()) {
                        currLock.getLock().unlock();
                        currLock.setLocked(false);
                        log.debug(name + " TaskWatcher Finally Lock unlocked " + currLock.getLockName());
                    }
                }
            }
        }
    }

    public void startWatch(TimeUnitWrapper waitTO, TimeUnitWrapper execTO) {
        TimeoutLockThread tlt = new TimeoutLockThread(this, execTO, waitTO);
        lockingThread = tlt;
        currentState = TaskWatcherState.SUBMITTING;
        tlt.start();
    }

    public int getSubmitted() {
        return tasksSubmitted.get();
    }

    public int incrementAndGetSubmitted() {
        tasksSubmitted.incrementAndGet();
        log.debug(name + " Tasks submitted " + tasksSubmitted.get());
        return tasksSubmitted.get();

    }

    public int getFinished() {
        return tasksFinished.get();
    }

    public int incrementAndGetFinished() {
        tasksFinished.incrementAndGet();
        lastFinishedTaskTime = System.currentTimeMillis();
        log.debug(name + " Tasks finished " + tasksFinished.get());
        synchronized (lockingThread) {
            if ((currentState == TaskWatcherState.SUBMISSION_COMPLETE) && (tasksFinished.get() == tasksSubmitted.get())) {
                log.debug(name + " ############ Notify came! submitted=" + tasksSubmitted.get() + " finished=" + tasksFinished.get());
                lockingThread.notify();
            }
        }
        return tasksFinished.get();
    }

    public synchronized void timedUnlockObtain() {
        synchronized(lockingThread) {
            lockingThread.notify();
        }
    }

    public ReentrantLock getLock() {
        return theLock;
    }

    public TaskWatcherState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(TaskWatcherState currentState) {
        this.currentState = currentState;
    }
}
