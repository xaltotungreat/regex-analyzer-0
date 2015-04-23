package org.eclipselabs.real.core.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public interface IKeyedObjectRepository<K,V> extends ILockableRepository {
    public void add(K objKey, V obj, TimeUnitWrapper timeout);
    public void add(K objKey, V obj);
    public void addAll(Map<K,V> valMap, TimeUnitWrapper timeout);
    public void addAll(Map<K,V> valMap);

    public V get(K objKey, TimeUnitWrapper timeout);
    public V get(K soKey);

    public Set<K> getAllKeys();
    public Set<K> getAllKeys(TimeUnitWrapper timeout);

    public Set<K> getKeys(Predicate<V> valueFilter);
    public Set<K> getKeys(Predicate<V> valueFilter, TimeUnitWrapper timeout);

    public List<V> getAllValues();
    public List<V> getAllValues(TimeUnitWrapper timeout);
    public List<V> getValues(Predicate<K> keyFilter, TimeUnitWrapper timeout);
    public List<V> getValues(Predicate<K> keyFilter);

    public List<V> getValues(Predicate<K> keyFilter, Predicate<V> valueFilter, TimeUnitWrapper timeout);
    public List<V> getValues(Predicate<K> keyFilter, Predicate<V> valueFilter);

    public V getFirstValue(Predicate<K> keyFilter, TimeUnitWrapper timeout);
    public V getFirstValue(Predicate<K> keyFilter);

    public Boolean remove(K soKey, TimeUnitWrapper timeout);
    public Boolean remove(K soKey);
    public Integer remove(Predicate<K> keyFilter, TimeUnitWrapper timeout);
    public Integer remove(Predicate<K> keyFilter);

    public Integer removeAll(TimeUnitWrapper timeout);
    public Integer removeAll();

    public Boolean isEmpty();
    public Integer getCount();
}
