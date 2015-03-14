package org.eclipselabs.real.core.regex;

import org.eclipselabs.real.core.util.ITypedObject;

public interface IRealRegexParam<T> extends ITypedObject<RealRegexParamType>{

    public T getValue();
    public void setValue(T newValue);
    public String getName();
    public void setName(String name);
}
