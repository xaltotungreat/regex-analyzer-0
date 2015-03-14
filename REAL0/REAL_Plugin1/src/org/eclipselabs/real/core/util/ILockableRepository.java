package org.eclipselabs.real.core.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public interface ILockableRepository {
    public void setGetTimeout(Long timeout, TimeUnit tu);
    public void setPutTimeout(Long timeout, TimeUnit tu);
    public Lock getWriteLock();
    public Lock getReadLock();
}
