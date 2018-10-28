package org.eclipselabs.real.core.exception;

public class IncorrectPatternException extends Exception {

    public IncorrectPatternException() {
    }

    public IncorrectPatternException(String message) {
        super(message);
    }

    public IncorrectPatternException(Throwable cause) {
        super(cause);
    }

    public IncorrectPatternException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectPatternException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
