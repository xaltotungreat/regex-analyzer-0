package org.eclipselabs.real.core.logfile;

public enum LogOperationType {
    LOG_FILE_READ,
    SEARCH,
    LOG_FILE_READ_WAIT,
    SEARCH_WAIT,
    CONTROLLER_REMOVE_FOLDERS_WAIT,
    CONTROLLER_READ_WAIT,
    CONTROLLER_RELOAD_FOLDERS,
    CONTROLLER_RELOAD_FOLDERS_WAIT,
    OPERATION_PENDING;
}