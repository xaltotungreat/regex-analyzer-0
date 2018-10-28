package org.eclipselabs.real.core.distrib;

import java.util.Map;
import java.util.Optional;

/**
 * This interface represents a wrapper for the actual task result. The idea is to
 * be able to add name-value pairs to the task result for further processing.
 *
 * @author Vadim Korkin
 *
 * @param <R> the type of the actual task result
 */
public interface IDistribTaskResultWrapper<R> {

    /**
     * This method returns the actual task result
     * @return the result of the task execution
     */
    R getActualResult();

    /**
     * This method sets the actual task result
     * @param res
     */
    void setActualResult(R res);

    /**
     * This method returns the whole map of name-value pairs.
     * @return all the name-value pairs as a map
     */
    Map<String,Object> getNVPs();

    /**
     * This method returns only one value from the map
     * @param key
     * @return
     */
    Optional<Object> getNVPValue(String key);

    /**
     * This method adds new name-value pairs to this task result
     * @param newNVPs new name-value pairs to add
     */
    void putNVPs(Map<String, Object> newNVPs);

    /**
     * Adds only one name-value pair to the task result
     * @param key the key for this name-value pair
     * @param value the value for this name-value pair
     */
    void putNVP(String key, Object value);

}
