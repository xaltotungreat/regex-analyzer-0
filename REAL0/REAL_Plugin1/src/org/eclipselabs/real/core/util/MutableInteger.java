package org.eclipselabs.real.core.util;

public class MutableInteger {
    @Override
    public String toString() {
        return "MutableInteger [intValue=" + intValue + "]";
    }

    protected Integer intValue;
    
    public MutableInteger(Integer intVal) {
        intValue = intVal;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }
}
