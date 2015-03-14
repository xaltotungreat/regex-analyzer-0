package org.eclipselabs.real.core.config;

public interface IConfigObjectConstructor<K,V> {
	public V constructCO(IConstructionSource<K> cSource);
}
