package org.eclipselabs.real.core.util;

import java.util.concurrent.locks.Lock;

/**
 * This class exists to check if the lock has been locked or not.
 * For unknown reasons the default Lock interface provides no way to check
 * if the lock is already locked or not. This check will only work if the flag is set every time
 * the lock is locked.
 *
 * @author Vadim Korkin
 *
 */
public class LockWrapper {
    protected Lock theLock;
    protected Boolean lockLocked = false;
    protected String lockName;
    public LockWrapper(Lock aLock) {
        theLock = aLock;
    }

    public LockWrapper(Lock aLock, String aName) {
        theLock = aLock;
        lockName = aName;
    }

    public Lock getLock() {
        return theLock;
    }

    public synchronized void setLocked(Boolean locked) {
        lockLocked = locked;
    }

    public synchronized Boolean isLocked() {
        return lockLocked;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }
}