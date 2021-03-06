package org.eclipselabs.real.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.AsyncEventBus;

/**
 * This is a reusable software component that is a synchronized map.
 * It allows for storing values with keys. There two main differences from
 * a simple ConcurrentHashMap:
 * 1. This class allows to select values for which the keys satisfy a filter. Currently the class uses
 * Predicate<K> to specify the filter.
 * 2. This class allows its subclasses to initialize a EventBus to receive events from the repository
 * It is important to remember though that if the order of the events is important (as it is usually
 * with the event that are fired after the size of the repository changes) the EventBus should be either
 * a sync EventBus or with a single-threaded executor.
 *
 * @author Vadim Korkin
 *
 * @param <K> the type of the key in the repository
 * @param <V> the type of the values in the repository
 */
public class KeyedObjectRepositoryImpl<K, R, W extends R> implements IKeyedObjectRepository<K,R, W> {
    private static final Logger log = LogManager.getLogger(KeyedObjectRepositoryImpl.class);
    protected Map<K,W> objMap = new ConcurrentHashMap<>();

    protected Long getTimeout = DEFAULT_READ_TIMEOUT;
    protected TimeUnit getTimeUnit = DEFAULT_READ_TIME_UNIT;
    protected Long putTimeout = DEFAULT_WRITE_TIMEOUT;
    protected TimeUnit putTimeUnit = DEFAULT_WRITE_TIME_UNIT;

    public static final Long DEFAULT_READ_TIMEOUT = (long)15;
    public static final TimeUnit DEFAULT_READ_TIME_UNIT = TimeUnit.SECONDS;

    public static final Long DEFAULT_WRITE_TIMEOUT = (long)15;
    public static final TimeUnit DEFAULT_WRITE_TIME_UNIT = TimeUnit.SECONDS;

    protected volatile ReentrantReadWriteLock repositoryLock = new ReentrantReadWriteLock();

    protected AsyncEventBus repositoryEventBus;

    public KeyedObjectRepositoryImpl() {

    }

    public KeyedObjectRepositoryImpl(AsyncEventBus repEvBus) {
        setRepositoryEventBus(repEvBus);
    }

    @Override
    public Lock getReadLock() {
        return repositoryLock.readLock();
    }

    @Override
    public Lock getWriteLock() {
        return repositoryLock.writeLock();
    }

    @Override
    public void add(K objKey, W obj, TimeUnitWrapper timeout) {
        boolean lockObtained = false;
        try {
            if (repositoryLock.writeLock().tryLock() || repositoryLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                int oldSize = objMap.size();
                log.debug("KeyedObjectRepositoryImpl Add new object key=" + objKey + "\n" + this);
                objMap.put(objKey, obj);
                int newSize = objMap.size();
                if ((getRepositoryEventBus() != null) && (oldSize != newSize)) {
                    final RepositorySizeChangedEvent addEvent = new RepositorySizeChangedEvent(oldSize, newSize);
                    getRepositoryEventBus().post(addEvent);
                }
            }
        } catch (InterruptedException e) {
            log.error("AddError", e);
        } finally {
            if (lockObtained && repositoryLock.isWriteLockedByCurrentThread()) {
                repositoryLock.writeLock().unlock();
            }
        }
    }

    @Override
    public void add(K objKey, W obj) {
        add(objKey, obj, new TimeUnitWrapper(5*getTimeout, getTimeUnit));
    }


    @Override
    public void addAll(Map<K, W> valMap, TimeUnitWrapper timeout) {
        boolean lockObtained = false;
        try {
            if (valMap != null) {
                if (repositoryLock.writeLock().tryLock() || repositoryLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                    lockObtained = true;
                    int oldSize = objMap.size();
                    log.debug("KeyedObjectRepositoryImpl Adding " + valMap.size() + " elements" + "\n" + this);
                    objMap.putAll(valMap);
                    int newSize = objMap.size();
                    if ((getRepositoryEventBus() != null) && (oldSize != newSize)) {
                        final RepositorySizeChangedEvent addEvent = new RepositorySizeChangedEvent(oldSize, newSize);
                        getRepositoryEventBus().post(addEvent);
                    }
                }
            } else {
                log.error("KeyedObjectRepositoryImpl Adding NULL map");
            }
        } catch (InterruptedException e) {
            log.error("AddError", e);
        } finally {
            if (lockObtained && repositoryLock.isWriteLockedByCurrentThread()) {
                repositoryLock.writeLock().unlock();
            }
        }
    }

    @Override
    public void addAll(Map<K, W> valMap) {
        addAll(valMap, new TimeUnitWrapper(5*getTimeout, getTimeUnit));
    }


    @Override
    public R get(K objKey, TimeUnitWrapper timeout) {
        R result = null;
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                log.debug("KeyedObjectRepositoryImpl Get object key=" + objKey);
                result = objMap.get(objKey);
            } else {
                log.info("KeyedObjectRepositoryImpl Timeout expired trying to get " + objKey);
            }
        } catch (InterruptedException e) {
            log.error("get Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public R get(K soKey) {
        return get(soKey, new TimeUnitWrapper(putTimeout, putTimeUnit));
    }

    @Override
    public W getFull(K objKey, TimeUnitWrapper timeout) {
        W result = null;
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                log.debug("KeyedObjectRepositoryImpl Get object key=" + objKey);
                result = objMap.get(objKey);
            } else {
                log.info("KeyedObjectRepositoryImpl Timeout expired trying to get " + objKey);
            }
        } catch (InterruptedException e) {
            log.error("get Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public W getFull(K soKey) {
        return getFull(soKey, new TimeUnitWrapper(putTimeout, putTimeUnit));
    }

    @Override
    public List<R> getAllValues(TimeUnitWrapper timeout) {
        List<R> result = new ArrayList<>();
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                result.addAll(objMap.values());
            } else {
                log.info("getAllValues Timeout expired trying to get ALL Keyed Objects");
            }
        } catch (InterruptedException e) {
            log.error("getAll Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public List<R> getAllValues() {
        return getAllValues(new TimeUnitWrapper(putTimeout, putTimeUnit));
    }

    @Override
    public List<W> getAllValuesFull(TimeUnitWrapper timeout) {
        List<W> result = new ArrayList<>();
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                result.addAll(objMap.values());
            } else {
                log.info("getAllValues Timeout expired trying to get ALL Keyed Objects");
            }
        } catch (InterruptedException e) {
            log.error("getAll Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public List<W> getAllValuesFull() {
        return getAllValuesFull(new TimeUnitWrapper(putTimeout, putTimeUnit));
    }

    @Override
    public List<R> getValues(Predicate<K> keyFilter, TimeUnitWrapper timeout) {
        // return an empty list in the case of error
        List<R> result = new ArrayList<>();
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                result = objMap.entrySet().stream()
                    .filter(e -> keyFilter.test(e.getKey()))
                    .map(e -> e.getValue())
                    .collect(Collectors.toList());
            } else {
                log.info("getValues Timeout expired trying to get ALL Keyed Objects");
            }
        } catch (InterruptedException e) {
            log.error("getValues Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public List<R> getValues(Predicate<K> keyFilter) {
        return getValues(keyFilter, new TimeUnitWrapper(putTimeout, putTimeUnit));
    }

    @Override
    public List<R> getValues(Predicate<K> keyFilter, Predicate<R> valueFilter, TimeUnitWrapper timeout) {
        // return an empty list in the case of error
        List<R> result = new ArrayList<>();
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                // the commented out code did the same left it just in case
                result = objMap.entrySet().stream()
                    .filter(e -> keyFilter.test(e.getKey()))
                    .filter(e -> valueFilter.test(e.getValue()))
                    .map(e -> e.getValue())
                    .collect(Collectors.toList());
            }
        } catch (InterruptedException e) {
            log.error("getValues Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public List<R> getValues(Predicate<K> keyFilter, Predicate<R> valueFilter) {
        return getValues(keyFilter, valueFilter, new TimeUnitWrapper(putTimeout, putTimeUnit));
    }

    @Override
    public R getFirstValue(Predicate<K> keyFilter, TimeUnitWrapper timeout) {
        R result = null;
        List<R> allMatchingValues = getValues(keyFilter, timeout);
        if ((allMatchingValues != null) && (!allMatchingValues.isEmpty())) {
            result = allMatchingValues.get(0);
        }
        return result;
    }

    @Override
    public R getFirstValue(Predicate<K> keyFilter) {
        return getFirstValue(keyFilter, new TimeUnitWrapper(putTimeout, putTimeUnit));
    }


    @Override
    public Boolean remove(K soKey, TimeUnitWrapper timeout) {
        Boolean result = false;
        boolean lockObtained = false;
        try {
            if (repositoryLock.writeLock().tryLock() || repositoryLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                if (objMap.containsKey(soKey)) {
                    int oldSize = objMap.size();
                    objMap.remove(soKey);
                    result = true;
                    int newSize = objMap.size();
                    log.debug("remove " + soKey + " oldSize=" + oldSize + " newSize=" + newSize);
                    if ((getRepositoryEventBus() != null) && (oldSize != newSize)) {
                        RepositorySizeChangedEvent addEvent = new RepositorySizeChangedEvent(oldSize, newSize);
                        getRepositoryEventBus().post(addEvent);
                    }
                } else {
                    result = false;
                }
            } else {
                log.info("remove Timeout expired trying to remove " + soKey);
            }
        } catch (InterruptedException e) {
            log.error("remove Error", e);
        } finally {
            if (lockObtained && repositoryLock.isWriteLockedByCurrentThread()) {
                repositoryLock.writeLock().unlock();
            }
        }
        return result;
    }

    @Override
    public Boolean remove(K soKey) {
        return remove(soKey, new TimeUnitWrapper(putTimeout, putTimeUnit));
    }

    @Override
    public Integer remove(Predicate<K> keyFilter, TimeUnitWrapper timeout) {
        Integer soCount = 0;
        boolean lockObtained = false;
        try {
            if (repositoryLock.writeLock().tryLock() || repositoryLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                int oldSize = objMap.size();
                for (K currKey : objMap.keySet()) {
                    if (keyFilter.test(currKey)) {
                        soCount++;
                        objMap.remove(currKey);
                    }
                }
                int newSize = objMap.size();
                if ((getRepositoryEventBus() != null) && (oldSize != newSize)) {
                    final RepositorySizeChangedEvent addEvent = new RepositorySizeChangedEvent(oldSize, newSize);
                    getRepositoryEventBus().post(addEvent);
                }
            } else {
                log.info("remove Timeout expired trying to remove ALL Keyed Objects");
            }
        } catch (InterruptedException e) {
            log.error("remove interrupted", e);
        } finally {
            if (lockObtained && repositoryLock.isWriteLockedByCurrentThread()) {
                repositoryLock.writeLock().unlock();
            }
        }
        return soCount;
    }

    @Override
    public Integer remove(Predicate<K> keyFilter) {
        return remove(keyFilter, new TimeUnitWrapper(5*getTimeout,getTimeUnit));
    }

    @Override
    public Integer removeAll(TimeUnitWrapper timeout) {
        Integer soCount = 0;
        boolean lockObtained = false;
        try {
            if (repositoryLock.writeLock().tryLock() || repositoryLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                soCount = objMap.size();
                objMap.clear();
            }
        } catch (InterruptedException e) {
            log.error("remove interrupted", e);
        } finally {
            if (lockObtained && repositoryLock.isWriteLockedByCurrentThread()) {
                repositoryLock.writeLock().unlock();
            }
        }
        return soCount;
    }

    @Override
    public Integer removeAll() {
        return removeAll(new TimeUnitWrapper(5*getTimeout,getTimeUnit));
    }

    @Override
    public void setGetTimeout(Long timeout, TimeUnit tu) {
        getTimeout = timeout;
        getTimeUnit = tu;
    }

    @Override
    public void setPutTimeout(Long timeout, TimeUnit tu) {
        putTimeout = timeout;
        putTimeUnit = tu;
    }

    @Override
    public Set<K> getAllKeys(TimeUnitWrapper timeout) {
        Set<K> result = new HashSet<>();
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                result.addAll(objMap.keySet());
            } else {
                log.info("getAllKeys Timeout expired trying to get ALL Keyed Objects");
            }
        } catch (InterruptedException e) {
            log.error("getAll Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public Set<K> getAllKeys() {
        return getAllKeys(new TimeUnitWrapper(5*getTimeout,getTimeUnit));
    }

    @Override
    public Set<K> getKeys(final Predicate<R> valueFilter, TimeUnitWrapper timeout) {
        // return an empty set in the case of error
        Set<K> result = new HashSet<>();
        boolean lockObtained = false;
        try {
            if (repositoryLock.readLock().tryLock() || repositoryLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                // the commented out code did the same left it just in case
                result = objMap.entrySet().stream()
                    .filter(e -> valueFilter.test(e.getValue()))
                    .map(e -> e.getKey())
                    .collect(Collectors.toCollection(() -> new HashSet<>()));
            } else {
                log.info("getKeys Timeout expired trying to get ALL Keyed Objects");
            }
        } catch (InterruptedException e) {
            log.error("getKeys Error", e);
        } finally {
            if (lockObtained) {
                repositoryLock.readLock().unlock();
            }
        }
        return result;
    }

    @Override
    public Set<K> getKeys(Predicate<R> valueFilter) {
        return getKeys(valueFilter, new TimeUnitWrapper(5*getTimeout,getTimeUnit));
    }

    @Override
    public Boolean isEmpty() {
        return objMap.isEmpty();
    }

    @Override
    public Integer getCount() {
        return objMap.size();
    }

    public AsyncEventBus getRepositoryEventBus() {
        return repositoryEventBus;
    }

    public void setRepositoryEventBus(AsyncEventBus repositoryEventBus) {
        this.repositoryEventBus = repositoryEventBus;
    }

    protected void setMap(Map<K, W> newMapRef) {
        objMap = newMapRef;
    }


}
