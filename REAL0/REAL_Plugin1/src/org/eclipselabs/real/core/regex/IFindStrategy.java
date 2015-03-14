package org.eclipselabs.real.core.regex;

import org.eclipselabs.real.core.util.FindTextResult;
import org.eclipselabs.real.core.util.ITypedObject;

public interface IFindStrategy extends ITypedObject<FindStrategyType> {

    public FindTextResult getResult();
    public void region(int rgStart, int rgEnd);
    public boolean find();
    public boolean matches();
}
