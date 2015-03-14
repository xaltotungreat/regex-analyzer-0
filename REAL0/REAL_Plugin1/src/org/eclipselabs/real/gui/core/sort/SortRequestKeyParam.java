package org.eclipselabs.real.gui.core.sort;

import org.eclipselabs.real.core.util.ITypedObject;

public class SortRequestKeyParam<T> implements ITypedObject<SortRequestKeyParamType>, Cloneable {

    
    protected SortRequestKeyParamType type;
    protected SortRequestKeyParamUseType useType;
    protected T value;
    
    public SortRequestKeyParam(SortRequestKeyParamType aType, SortRequestKeyParamUseType aUseType, T val) {
        type = aType;
        useType = aUseType;
        value = val;
    }
    
    public SortRequestKeyParam<T> clone() throws CloneNotSupportedException {
        return (SortRequestKeyParam<T>)super.clone();
    }
    
    @Override
    public SortRequestKeyParamType getType() {
        return type;
    }

    public SortRequestKeyParamUseType getUseType() {
        return useType;
    }

    public void setUseType(SortRequestKeyParamUseType useType) {
        this.useType = useType;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SortRequestKeyParam [type=" + type + ", useType=" + useType + ", value=" + value + "]";
    }

}
