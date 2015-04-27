package org.eclipselabs.real.core.config;

import java.util.function.Supplier;

public class ConstructionTask<K, V> implements Supplier<V> {

    private IConfigObjectConstructor<K, V> theConstructor;
    private IConstructionSource<K> theConstructionSource;

    public ConstructionTask(IConfigObjectConstructor<K, V> aConstructor, IConstructionSource<K> aSource) {
        theConstructor = aConstructor;
        theConstructionSource = aSource;
    }

    @Override
    public V get() {
        return theConstructor.constructCO(theConstructionSource);
    }

}
