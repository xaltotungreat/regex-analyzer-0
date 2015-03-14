package org.eclipselabs.real.core.util;

public interface IKeyedObject<K> {
    public K getKey();
    public void setKey(K aNewName); 
}
