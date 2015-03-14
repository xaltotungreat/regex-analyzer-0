package org.eclipselabs.real.core.config.regex.constructor;

import org.eclipselabs.real.core.config.IConfigObjectConstructor;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;

public interface ISOConstructor<K,V extends IKeyedSearchObject<?, ?>> extends IConfigObjectConstructor<K,V> {
}
