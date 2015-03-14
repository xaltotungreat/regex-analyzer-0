package org.eclipselabs.real.core.config;

public interface IConstructionSource<V> {
	public V getSource();
	public void setSource(V newSource);
}
