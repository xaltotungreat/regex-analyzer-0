package org.eclipselabs.real.core.config;

import java.util.concurrent.Callable;

public class ConstructionTask<K, V> implements Callable<V> {

    private IConfigObjectConstructor<K, V> theConstructor;
    private IConstructionSource<K> theConstructionSource;

    public ConstructionTask(IConfigObjectConstructor<K, V> aConstructor, IConstructionSource<K> aSource) {
        theConstructor = aConstructor;
        theConstructionSource = aSource;
    }

    @Override
    public V call() throws Exception {
        return theConstructor.constructCO(theConstructionSource);
    }

}
