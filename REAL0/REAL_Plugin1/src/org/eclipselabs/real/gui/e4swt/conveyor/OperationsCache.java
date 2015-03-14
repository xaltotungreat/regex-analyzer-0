package org.eclipselabs.real.gui.e4swt.conveyor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.gui.core.util.SearchInfo;

public class OperationsCache {
    private static final Logger log = LogManager.getLogger(OperationsCache.class);

    private static final int MAX_OPERATIONS = 5;

    private Map<SearchObjectKey, LinkedBlockingDeque<SearchInfo>> internalCache = new ConcurrentHashMap<SearchObjectKey, LinkedBlockingDeque<SearchInfo>>();

    public Optional<SearchInfo> getLastExecutionParams(SearchObjectKey key) {
        Optional<SearchInfo> result = Optional.empty();
        LinkedBlockingDeque<SearchInfo> currQueue = internalCache.get(key);
        if ((currQueue != null) && (!currQueue.isEmpty())) {
            result = Optional.of(currQueue.peekLast());
        }
        return result;
    }

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
