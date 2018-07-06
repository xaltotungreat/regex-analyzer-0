package org.eclipselabs.real.core.logfile;

import java.util.concurrent.TimeUnit;

import org.eclipselabs.real.core.logfile.ILogFileAggregate.MultiThreadingState;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public enum LogTimeoutPolicy implements ILogTimeoutPolicy {
    INSTANCE;

    protected TimeUnit defaultTimeUnit;
    protected Long readTimeout;
    protected TimeUnit readTimeUnit;
    protected Long searchTimeout;
    protected TimeUnit searchTimeUnit;


    private LogTimeoutPolicy() {
        defaultTimeUnit = DEFAULT_TIMEUNIT;
        readTimeout = DEFAULT_READ_TIMEOUT;
        readTimeUnit = DEFAULT_TIMEUNIT;
        searchTimeout = DEFAULT_SEARCH_TIMEOUT;
        searchTimeUnit = DEFAULT_TIMEUNIT;
    }

    public TimeUnit getDefaultTimeUnit() {
        return DEFAULT_TIMEUNIT;
    }

    public TimeUnitWrapper getOperationTimeout(LogOperationType opType, ILogFileAggregateRead aggregateObj) {
        Long timeout = (long)0;
        switch(opType) {
        case LOG_FILE_READ:
            timeout = readTimeout;
            break;
        case SEARCH:
            if (MultiThreadingState.ALLOW_MULTITHREADING_READ.equals(aggregateObj.getReadFilesState())) {
                timeout = searchTimeout;
            } else {
                timeout = readTimeout + searchTimeout;
            }
            break;
        case LOG_FILE_READ_WAIT:
            if (ILogFileAggregate.MultiThreadingState.ALLOW_MULTITHREADING_READ == aggregateObj.getReadFilesState()) {
                timeout = readTimeout;
            } else {
                timeout = 5*readTimeout;
            }
            break;
        case SEARCH_WAIT:
            if (ILogFileAggregate.MultiThreadingState.ALLOW_MULTITHREADING_READ == aggregateObj.getReadFilesState()) {
                timeout = 5*searchTimeout;
            } else {
                timeout = 5*(readTimeout + searchTimeout);
            }
            break;
        case OPERATION_PENDING:
            timeout = (long)1;
            break;
        case CONTROLLER_REMOVE_FOLDERS_WAIT:
            timeout = readTimeout;
            break;
        case CONTROLLER_READ_WAIT:
            timeout = readTimeout;
            break;
        case CONTROLLER_RELOAD_FOLDERS:
            timeout = readTimeout;
            break;
        case CONTROLLER_RELOAD_FOLDERS_WAIT:
            timeout = readTimeout;
            break;
        }
        return new TimeUnitWrapper(timeout, DEFAULT_TIMEUNIT);
    }


}
