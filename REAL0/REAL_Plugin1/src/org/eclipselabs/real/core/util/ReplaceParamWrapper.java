package org.eclipselabs.real.core.util;

import org.eclipselabs.real.core.searchobject.param.IReplaceParam;

public class ReplaceParamWrapper<T> {

    protected IReplaceParam<T> param;
    protected T oldValue;
    
    public ReplaceParamWrapper() {
    }
    
    public ReplaceParamWrapper(IReplaceParam<T> p) {
        param = p;
    }
    
    public ReplaceParamWrapper(IReplaceParam<T> p, T val) {
        param = p;
        oldValue = val;
    }
    
    public static <Q> ReplaceParamWrapper<Q> getWrapper(IReplaceParam<Q> p) {
        return new ReplaceParamWrapper<>(p);
    }

    public IReplaceParam<T> getParam() {
        return param;
    }

    public void setParam(IReplaceParam<T> param) {
        this.param = param;
    }

    public T getOldValue() {
        return oldValue;
    }

    public void setOldValue(T oldValue) {
        this.oldValue = oldValue;
    }

}
