package org.eclipselabs.real.core.util;

import java.util.concurrent.locks.Lock;

/**
 * This class represents a named lock. When a lock is passed between several different objects
 * it may be important to know what this lock represents to debug issues when lock acquisition fails
 * @author Vadim Korkin
 *
 */
public class NamedLock {
    protected Lock theLock;
    protected String lockName;

    public NamedLock(Lock aLock) {
        theLock = aLock;
    }

    public NamedLock(Lock aLock, String aName) {
        theLock = aLock;
        lockName = aName;
    }

    public Lock getLock() {
        return theLock;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }
}