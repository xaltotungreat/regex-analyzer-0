package org.eclipselabs.real.core.util;

public class NameValuePair<N, V> {

    protected N name;
    protected V value;
    
    public NameValuePair() {
        
    }
    
    public NameValuePair(N aName, V aValue) {
        name = aName;
        value = aValue;
    }
    public N getName() {
        return name;
    }
    public void setName(N name) {
        this.name = name;
    }
    public V getValue() {
        return value;
    }
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "NameValuePair [name=" + name + ", value=" + value + "]";
    }

}
