package org.eclipselabs.real.core.util;

public interface IMutableTypedObject<T> extends ITypedObject<T> {

    public void setType(T newType);
}
