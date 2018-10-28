package org.eclipselabs.real.core.exception;

public class LogFileSearchException extends Exception {

    private String fileName;

    public LogFileSearchException(String fn) {
        fileName = fn;
    }

    public LogFileSearchException(String fn, String message) {
        super(message);
        fileName = fn;
    }

    public LogFileSearchException(String fn, Throwable cause) {
        super(cause);
        fileName = fn;
    }

    public LogFileSearchException(String fn, String message, Throwable cause) {
        super(message, cause);
        fileName = fn;
    }

    public LogFileSearchException(String fn, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        fileName = fn;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
