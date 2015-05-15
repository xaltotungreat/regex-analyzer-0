package org.eclipselabs.real.core.regex;

import org.eclipselabs.real.core.util.ITypedObject;

/**
 * This class is a parameter for a IRealRegex.
 * Parameters change the behavoir of the regex. For example a parameter
 * may say that this regex must find only the first occurrence in the text.
 * The second, third etc. occurrences then will not be found.
 *
 * @author Vadim Korkin
 *
 * @param <T> the type of the value of this param
 */
public interface IRealRegexParam<T> extends ITypedObject<RealRegexParamType>{

    public T getValue();
    public void setValue(T newValue);
    public String getName();
    public void setName(String name);
}
