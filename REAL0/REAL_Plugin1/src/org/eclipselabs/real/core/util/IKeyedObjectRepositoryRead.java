package org.eclipselabs.real.core.util;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface IKeyedObjectRepositoryRead<K,V> {

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
