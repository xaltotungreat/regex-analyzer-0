package org.eclipselabs.real.core.config.ml;

public interface IConfigObjectConstructor<K,V> {
	public V constructCO(IConstructionSource<K> cSource);
}
