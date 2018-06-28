package org.eclipselabs.real.core.distrib;

public interface IDistribAbstract<R> {

    public default boolean isRoot() {
        return false;
    }

    public IDistribRoot<R> getRoot();
}
