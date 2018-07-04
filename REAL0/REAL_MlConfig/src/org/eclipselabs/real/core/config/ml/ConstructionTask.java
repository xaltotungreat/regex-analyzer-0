package org.eclipselabs.real.core.config.ml;

import java.util.function.Supplier;

/**
 * The ConstructionTask exists to avoid tight coupling between the construction source
 * (it may be a XML element, a DB table row) and the constructed object.
 * The process of construction should be as following:
 * 1. The main construction class creates new construction tasks while processing
 *     the configuration information.
 * 2. When the tasks are completed the constructed objects are accumulated in an array
 * 3. After all configuration information has been processed the created objects
 *     are added to the repository
 * @author Vadim Korkin
 *
 * @param <K> the class of the unit of configuration information, the source
 * @param <V> the class of the object to be constructed
 */
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
