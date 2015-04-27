package org.eclipselabs.real.core.config;

import java.util.concurrent.CompletableFuture;

public interface IConfigReader<U> {

    /**
     * The main method for reading the configuration
     * @param configRI the resource from which the configuration is read
     * @return a completable future which returns some Integer parameter (could be the number of objects read)
     */
    public CompletableFuture<Integer> read(U configRI);

}
