package org.eclipselabs.real.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.LogFileSearchException;

/**
 * This utility class is used to submit and execute tasks in a sequential manner.
 * Usually this class is used if the tasks require some locks (on some arrays or repositories)
 * to be locked during execution. (to avoid modification while the tasks are run)
 * This class allows to specify the wait time (to lock all locks) and execution time
 * (to wait for the tasks to be executed)
 *
 * The sequence of actions in the internal thread is the following:
 * 1. Lock the lock necessary to proceed (passed in the constructor)
 * 2. Submit the tasks via the taskwatcher callback
 * 3. Wait for the specified timeout or until all the tasks are executed
 *      (whichever comes first)
 * 4. Execute the completion callback via the taskwatcher callback
 * 5. Unlock the locks
 *
 * This class uses the strict policy by default - do not submit and execute tasks if
 * even one of the locks wasn't successfully locked.
 *
 * @author Vadim Korkin
 *
 */
public class TaskWatcher {
    private static final Logger log = LogManager.getLogger(TaskWatcher.class);
    protected String name;
    private volatile ReentrantLock theLock = new ReentrantLock();
    private volatile Thread lockingThread;
    protected AtomicInteger tasksSubmitted = new AtomicInteger();
    protected AtomicInteger tasksFinished = new AtomicInteger();
    protected AtomicInteger tasksInError = new AtomicInteger();
    protected List<LogFileSearchException> excList = new ArrayList<>();
    protected volatile TaskWatcherState currentState;
    protected List<LockWrapper> theLocks;
    protected ITaskWatcherCallback theCallback;
    protected volatile Long lastFinishedTaskTime;
    protected boolean proceedIfNotLocked = false;

    public static final String TASK_WATCHER_THREAD_NAME = "TaskWatcher";

    public enum TaskWatcherState {
        INIT,
        SUBMITTING,
        SUBMISSION_COMPLETE,
        FINISHED
    }

    public TaskWatcher(String aName, List<NamedLock> locksList, ITaskWatcherCallback completionCallback) {
        // strict policy by default - do not execute anything if one of the locks is not locked
        this(aName, locksList, completionCallback, false);
    }

    public TaskWatcher(String aName, List<NamedLock> locksList, ITaskWatcherCallback completionCallback, boolean proceed) {
        name = aName;
        currentState = TaskWatcherState.INIT;
        theCallback = completionCallback;
        proceedIfNotLocked = proceed;
        theLocks = new ArrayList<>();
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
                boolean allLocksLocked = true;
                for (LockWrapper currLock : theLocks) {
                    if (!currLock.isLocked()) {
                        if ((currLock.getLock().tryLock()) || (currLock.getLock().tryLock(theWaitTO.getTimeout(), theWaitTO.getTimeUnit()))) {
                            currLock.setLocked(true);
                            log.debug(name + " TaskWatcher Lock locked " + currLock.getLockName());
                        } else {
                            log.error(name + " Unable to obtain lock " + currLock.getLockName() + " timeout " + theWaitTO.getTimeout() + theWaitTO.getTimeUnit());
                            allLocksLocked = false;
                        }
                    }
                }
                double beginSubmitTime = System.currentTimeMillis();
                log.info("Time Acquiring locks " + (beginSubmitTime - beginTime)/1000);
                if (allLocksLocked || (!allLocksLocked && proceedIfNotLocked)) {
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
                } else {
                    log.debug("Some locks were not locked, the tasks were not submitted and not executed, proceed to unlock");
                }
                for (LockWrapper currLock : theLocks) {
                    if (currLock.isLocked()) {
                        currLock.getLock().unlock();
                        currLock.setLocked(false);
                        log.debug(name + " TaskWatcher Locks unlocked " + currLock.getLockName());
                    }
                }
                parentWatcher.setCurrentState(TaskWatcherState.FINISHED);
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

    public synchronized int getSubmitted() {
        return tasksSubmitted.get();
    }

    public synchronized int incrementAndGetSubmitted() {
        tasksSubmitted.incrementAndGet();
        log.debug(name + " Tasks submitted " + tasksSubmitted.get());
        return tasksSubmitted.get();

    }

    public synchronized int getFinished() {
        return tasksFinished.get();
    }

    public synchronized int getInError() {
        return tasksInError.get();
    }

    public synchronized int incrementAndGetFinished() {
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

    public synchronized int incrementAndGetInError(LogFileSearchException excObj) {
        tasksInError.incrementAndGet();
        tasksFinished.incrementAndGet();
        excList.add(excObj);
        lastFinishedTaskTime = System.currentTimeMillis();
        log.debug(name + " Tasks finished " + tasksFinished.get());
        synchronized (lockingThread) {
            if ((currentState == TaskWatcherState.SUBMISSION_COMPLETE) && (tasksFinished.get() == tasksSubmitted.get())) {
                log.debug(name + " ############ Notify came! submitted=" + tasksSubmitted.get() + " finished=" + tasksFinished.get());
                lockingThread.notify();
            }
        }
        return tasksInError.get();
    }

    public synchronized void timedUnlockObtain() {
        synchronized(lockingThread) {
            lockingThread.notify();
        }
    }

    public synchronized ReentrantLock getLock() {
        return theLock;
    }

    public synchronized TaskWatcherState getCurrentState() {
        return currentState;
    }

    public synchronized void setCurrentState(TaskWatcherState currentState) {
        this.currentState = currentState;
    }
}
