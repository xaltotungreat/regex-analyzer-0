package org.eclipselabs.real.gui.e4swt.conveyor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.gui.core.util.SearchInfo;

/**
 * This class is a wrapper for a cache of previously performed
 * search operations. The cache stores parameters of previous search
 * operations.
 *
 * @author Vadim Korkin
 *
 */
public class OperationsCache {
    private static final Logger log = LogManager.getLogger(OperationsCache.class);

    private static final int MAX_OPERATIONS = 5;

    private Map<SearchObjectKey, LinkedBlockingDeque<SearchInfo>> internalCache = new ConcurrentHashMap<SearchObjectKey, LinkedBlockingDeque<SearchInfo>>();

    /**
     * Returns a {@link SearchInfo} with the parameters of the last search
     * for the search object identified by the passed key.
     * @param key the key for the search object for which the last parameters are required
     * @return the parameters of the last search
     *      for the search object identified by the passed key
     */
    public Optional<SearchInfo> getLastExecutionParams(SearchObjectKey key) {
        Optional<SearchInfo> result = Optional.empty();
        LinkedBlockingDeque<SearchInfo> currQueue = internalCache.get(key);
        if ((currQueue != null) && (!currQueue.isEmpty())) {
            result = Optional.of(currQueue.peekLast());
        }
        return result;
    }

    /**
     * Adds a set of execution parameters to the cache for the specified search object key.
     *
     * @param key the key for which the parameters are added
     * @param info the parameters to add
     */
    public void addExecutionParams(SearchObjectKey key, SearchInfo info) {
        LinkedBlockingDeque<SearchInfo> currQueue = internalCache.get(key);
        if (currQueue == null) {
            currQueue = new LinkedBlockingDeque<SearchInfo>(MAX_OPERATIONS);
            internalCache.put(key, currQueue);
        }
        if ((!currQueue.contains(info)) && (!currQueue.offer(info))) {
            currQueue.poll();
            if (!currQueue.offer(info)) {
                log.error("addExecutionParams Unknown error adding new element");
            }
        }
    }

}
