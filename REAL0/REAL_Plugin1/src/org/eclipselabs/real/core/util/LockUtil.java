package org.eclipselabs.real.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LockUtil {

    private static final Logger log = LogManager.getLogger(LockUtil.class);

    public static LockWrapper getWrapper(Lock lk, String name) {
        return new LockWrapper(lk, name);
    }

    public static List<LockWrapper> getWrappers(List<Lock> lks, List<String> names) {
        List<LockWrapper> lws = new ArrayList<>();
        if ((names != null) && (!names.isEmpty())) {
            for (int i = 0; i < lks.size(); i++) {
                lws.add(getWrapper(lks.get(i), names.get(i)));
            }
        } else {
            lks.stream().forEach(lk -> lws.add(getWrapper(lk, null)));
        }
        return lws;
    }

    public static boolean lockAll(List<LockWrapper> allLocks, TimeUnitWrapper tuw) {
        boolean allLocked = true;
        try {
            for (LockWrapper currLock : allLocks) {
                if (!currLock.isLocked()) {
                    if ((currLock.getLock().tryLock()) || (currLock.getLock().tryLock(tuw.getTimeout(), tuw.getTimeUnit()))) {
                        currLock.setLocked(true);
                        log.debug("Lock locked " + currLock.getLockName());
                    } else {
                        log.error("Unable to obtain lock " + currLock.getLockName() + " timeout " + tuw.getTimeout() + tuw.getTimeUnit());
                        allLocked = false;
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("Wait for lock interrupted", e);
        } finally {
            if (!allLocked) {
                log.error("Unable to lock all - unlocking those already locked");
                unlockAll(allLocks);
            }
        }
        return allLocked;
    }

    public static void unlockAll(List<LockWrapper> allLocks) {
        allLocks.stream().filter(LockWrapper::isLocked).forEach(l -> {
            l.getLock().unlock();
            l.setLocked(false);
        });
    }

    public static Runnable getLockRunnable(List<LockWrapper> allLocks, TimeUnitWrapper waitTimeWrapper) {
        return () -> lockAll(allLocks, waitTimeWrapper);
    }

    public static Runnable getUnlockRunnable(List<LockWrapper> allLocks) {
        return () -> unlockAll(allLocks);
    }
}
