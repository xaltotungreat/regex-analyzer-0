package org.eclipselabs.real.core.config.ml.regex.constructor;

import org.eclipselabs.real.core.config.ml.IConfigObjectConstructor;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;

public interface ISOConstructor<K,V extends IKeyedSearchObject<?, ?>> extends IConfigObjectConstructor<K,V> {
}
