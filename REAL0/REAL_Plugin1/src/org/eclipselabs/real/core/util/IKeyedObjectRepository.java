package org.eclipselabs.real.core.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This interface represent an interface for an object repository where objects
 * are stored in a map with keys. It follows many of the principles on which
 * the simple Java Map is built like "unique keys", "use equals for keys" etc.
 *
 * But it also provides some additional methods to a simple Map:
 * 1. The repository can be locked to avoid any writes until the lock is released
 * 2. The add/remove methods are atomic - they lock the repository to prevent any modifications to the
 * repository. get methods also cannot return the value until the write lock is released
 * 3. The get methods use a read lock that must be released before any write operation can proceed.
 * The read lock may be obtained by multiple readers if the write lock is not held by anyone.
 *
 */
public interface IKeyedObjectRepository<K,V> extends ILockableRepository {
    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     */
    public void add(K objKey, V obj, TimeUnitWrapper timeout);
    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     * In this method the timeout is not specified so the default values are used.
     */
    public void add(K objKey, V obj);
    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     */
    public void addAll(Map<K,V> valMap, TimeUnitWrapper timeout);
    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     * In this method the timeout is not specified so the default values are used.
     * @param valMap - the map of keys/values to add
     */
    public void addAll(Map<K,V> valMap);

    public V get(K objKey, TimeUnitWrapper timeout);
    public V get(K soKey);

    public Set<K> getAllKeys();
    public Set<K> getAllKeys(TimeUnitWrapper timeout);

    public Set<K> getKeys(Predicate<V> valueFilter);
    public Set<K> getKeys(Predicate<V> valueFilter, TimeUnitWrapper timeout);

    public List<V> getAllValues(TimeUnitWrapper timeout);
    public List<V> getAllValues();

    public List<V> getValues(Predicate<K> keyFilter, TimeUnitWrapper timeout);
    public List<V> getValues(Predicate<K> keyFilter);

    public List<V> getValues(Predicate<K> keyFilter, Predicate<V> valueFilter, TimeUnitWrapper timeout);
    public List<V> getValues(Predicate<K> keyFilter, Predicate<V> valueFilter);

    public V getFirstValue(Predicate<K> keyFilter, TimeUnitWrapper timeout);
    public V getFirstValue(Predicate<K> keyFilter);

    public Boolean remove(K soKey, TimeUnitWrapper timeout);
    public Boolean remove(K soKey);
    /**
     * Removes all values from the repository for which the key satisfies the predicate.
     * @param keyFilter - if the test method of this filter returns true for the key this element is removed
     * @param timeout - the timeout to obtain the write lock
     * @return the number of key/value pairs removed
     */
    public Integer remove(Predicate<K> keyFilter, TimeUnitWrapper timeout);
    /**
     * Removes all values from the repository for which they key satisfies the predicate.
     * In this method the timeout is not specified so the default values are used.
     * @param keyFilter - if the test method of this filter returns true for the key this element is removed
     * @return the number of key/value pairs removed
     */
    public Integer remove(Predicate<K> keyFilter);

    /**
     * Removes all key/value pairs from the repository
     * @param timeout - the timeout to obtain the write lock
     * @return the number of key/value pairs removed
     */
    public Integer removeAll(TimeUnitWrapper timeout);
    /**
     * Removes all key/value pairs from the repository
     * In this method the timeout is not specified so the default values are used.
     * @return the number of key/value pairs removed
     */
    public Integer removeAll();

    /**
     * Returns true if this repository contains 0 elements
     * @return true if this repository contains 0 elements
     */
    public Boolean isEmpty();
    /**
     * Returns the number of elements in the repository
     * @return the number of elements in the repository
     */
    public Integer getCount();
}
