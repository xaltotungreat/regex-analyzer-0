package org.eclipselabs.real.core.config;

import com.google.common.util.concurrent.ListenableFuture;

public interface IConfigReader<U> {
    
    /**
     * The main method for reading the configuration 
     * @param configRI the resource from which the configuration is read
     * @return a listenable future which returns some Integer parameter (could be the number of objects read)
     */
    public ListenableFuture<Integer> read(U configRI);
    
}
