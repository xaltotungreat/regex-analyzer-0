package org.eclipselabs.real.core.util;


public class KeyedObjectImpl<K> implements IKeyedObject<K> {

    protected K objName;
    public KeyedObjectImpl(K name) {
        objName = name;
    }

    public K getKey() {
        return objName;
    }

    public void setKey(K aNewName) {
        objName = aNewName;
    }

}
