package org.eclipselabs.real.core.util;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface IKeyedObjectRepositoryWrite<K, W> {

    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     */
    public void add(K objKey, W obj, TimeUnitWrapper timeout);
    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     * In this method the timeout is not specified so the default values are used.
     */
    public void add(K objKey, W obj);
    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     */
    public void addAll(Map<K,W> valMap, TimeUnitWrapper timeout);
    /**
     * This method is atomic - it obtains the write lock at the start
     * and unlocks it after the element has been added.
     * If the lock was not obtained after the timeout has expired nothing happens.
     * In this method the timeout is not specified so the default values are used.
     * @param valMap - the map of keys/values to add
     */
    public void addAll(Map<K,W> valMap);

    /**
     * Removes the specified key-value from the repository
     * @param key the key by which to remove the key-value pair
     * @param timeout the timeout to obtain the write lock
     * @return true if removed successfully false otherwise
     */
    public Boolean remove(K key, TimeUnitWrapper timeout);
    /**
     * Removes the specified key-value from the repository
     * @param key the key by which to remove the key-value pair
     * @return
     */
    public Boolean remove(K key);
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

    public W getFull(K objKey, TimeUnitWrapper timeout);
    public W getFull(K soKey);

    public List<W> getAllValuesFull(TimeUnitWrapper timeout);
    public List<W> getAllValuesFull();
}
