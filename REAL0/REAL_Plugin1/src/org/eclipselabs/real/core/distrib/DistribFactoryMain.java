package org.eclipselabs.real.core.distrib;

public enum DistribFactoryMain {

    INSTANCE;

    public IDistribFactory getDefaultFactory() {
        return new DistribFactoryImpl();
    }
}
