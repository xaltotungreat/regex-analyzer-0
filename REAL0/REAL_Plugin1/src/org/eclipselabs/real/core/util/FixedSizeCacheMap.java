package org.eclipselabs.real.core.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FixedSizeCacheMap<K, V> {

    private Comparator<K> keysComparator; 
    
    protected Integer size = 5;
    
    protected Map<K, V> cacheMap = Collections.synchronizedMap(new LinkedHashMap<K,V>() {
        /**
         * Default serial Version UID
         */
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
            return super.size() > size;
        }
    });

    public FixedSizeCacheMap(int cacheSize, Comparator<K> comp) {
        size = cacheSize;
        keysComparator = comp;
    }

    public V getMin() {
        V result = null;
        if (!cacheMap.isEmpty()) {
            result = cacheMap.get(Collections.min(cacheMap.keySet(), keysComparator));
        }
        return result;
    }
    
    public V getMax() {
        V result = null;
        if (!cacheMap.isEmpty()) {
            result = cacheMap.get(Collections.max(cacheMap.keySet(), keysComparator));
        }
        return result;
    }
    
    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }
    
    public void put(K key, V value) {
        cacheMap.put(key, value);
    }
    
    public int size() {
        return cacheMap.size();
    }

    public Comparator<K> getKeysComparator() {
        return keysComparator;
    }

    public void setKeysComparator(Comparator<K> keysComparator) {
        this.keysComparator = keysComparator;
    }

    

}
