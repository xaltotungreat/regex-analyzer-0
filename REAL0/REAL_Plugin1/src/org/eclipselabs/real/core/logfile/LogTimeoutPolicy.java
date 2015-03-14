package org.eclipselabs.real.core.logfile;

import java.util.concurrent.TimeUnit;

import org.eclipselabs.real.core.logfile.ILogFileAggregate.MultiThreadingState;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public enum LogTimeoutPolicy implements ILogTimeoutPolicy {
    INSTANCE;
    
    public static enum OperationType {
        LOG_FILE_READ,
        SEARCH,
        LOG_FILE_READ_WAIT,
        SEARCH_WAIT,
        OPERATION_PENDING;
    }
    
    protected TimeUnit defaultTimeUnit;
    protected Long readTimeout;
    protected TimeUnit readTimeUnit;
    protected Long searchTimeout;
    protected TimeUnit searchTimeUnit;
    
    
    private LogTimeoutPolicy() {
        readTimeout = DEFAULT_READ_TIMEOUT;
        readTimeUnit = DEFAULT_TIMEUNIT;
        searchTimeout = DEFAULT_SEARCH_TIMEOUT;
        searchTimeUnit = DEFAULT_TIMEUNIT;
    }
    
    public TimeUnit getDefaultTimeUnit() {
        return DEFAULT_TIMEUNIT;
    }
    
    public TimeUnitWrapper getOperationTimeout(OperationType opType, ILogFileAggregate aggregateObj, ILogFile fileObj) {
        Long timeout = (long)0;
        if (opType == OperationType.LOG_FILE_READ) {
            if (fileObj.isRead()) {
                timeout = (long)0;
            } else {
                timeout = readTimeout;
            }
        } else if (opType == OperationType.SEARCH) {
            if (fileObj.isRead()) {
                timeout = searchTimeout;
            } else {
                timeout = readTimeout + searchTimeout;
            }
        } else if (opType == OperationType.LOG_FILE_READ_WAIT) {
            if (ILogFileAggregate.MultiThreadingState.ALLOW_MULTITHREADING_READ == aggregateObj.getReadFilesState()) {
                timeout = (long)0;
            } else {
                timeout = 5*readTimeout;
            }
        } else if (opType == OperationType.SEARCH_WAIT) {
            if (ILogFileAggregate.MultiThreadingState.ALLOW_MULTITHREADING_READ == aggregateObj.getReadFilesState()) {
                timeout = 5*searchTimeout;
            } else {
                timeout = 5*(readTimeout + searchTimeout);
            }
        } else if (opType == OperationType.OPERATION_PENDING) {
            timeout = (long)1;
        }
        return new TimeUnitWrapper(timeout, DEFAULT_TIMEUNIT);
    }
    
    public TimeUnitWrapper getOperationTimeout(OperationType opType, ILogFileAggregate aggregateObj) {
        Long timeout = (long)0;
        if (opType == OperationType.LOG_FILE_READ) {
            if (MultiThreadingState.ALLOW_MULTITHREADING_READ.equals(aggregateObj.getReadFilesState())) {
                timeout = readTimeout;
            } else {
                timeout = readTimeout;
            }
        } else if (opType == OperationType.SEARCH) {
            if (MultiThreadingState.ALLOW_MULTITHREADING_READ.equals(aggregateObj.getReadFilesState())) {
                timeout = searchTimeout;
            } else {
                timeout = readTimeout + searchTimeout;
            }
        } else if (opType == OperationType.LOG_FILE_READ_WAIT) {
            if (ILogFileAggregate.MultiThreadingState.ALLOW_MULTITHREADING_READ == aggregateObj.getReadFilesState()) {
                timeout = readTimeout;
            } else {
                timeout = 5*readTimeout;
            }
        } else if (opType == OperationType.SEARCH_WAIT) {
            if (ILogFileAggregate.MultiThreadingState.ALLOW_MULTITHREADING_READ == aggregateObj.getReadFilesState()) {
                timeout = 5*searchTimeout;
            } else {
                timeout = 5*(readTimeout + searchTimeout);
            }
        } else if (opType == OperationType.OPERATION_PENDING) {
            timeout = (long)1;
        }
        return new TimeUnitWrapper(timeout, DEFAULT_TIMEUNIT);
    }
}
