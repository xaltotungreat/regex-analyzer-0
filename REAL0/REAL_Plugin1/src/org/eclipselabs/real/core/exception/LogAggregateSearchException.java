package org.eclipselabs.real.core.exception;

import java.util.List;

public class LogAggregateSearchException extends Exception {

    private List<LogFileSearchException> fileExceptions;

    public LogAggregateSearchException(List<LogFileSearchException> lst) {
        fileExceptions = lst;
    }

    public LogAggregateSearchException(List<LogFileSearchException> lst, String message) {
        super(message);
        fileExceptions = lst;
    }

    public LogAggregateSearchException(List<LogFileSearchException> lst, Throwable cause) {
        super(cause);
        fileExceptions = lst;
    }

    public LogAggregateSearchException(List<LogFileSearchException> lst, String message, Throwable cause) {
        super(message, cause);
        fileExceptions = lst;
    }

    public LogAggregateSearchException(List<LogFileSearchException> lst, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        fileExceptions = lst;
    }

    public List<LogFileSearchException> getFileExceptions() {
        return fileExceptions;
    }

    public void setFileExceptions(List<LogFileSearchException> fileExceptions) {
        this.fileExceptions = fileExceptions;
    }

}
