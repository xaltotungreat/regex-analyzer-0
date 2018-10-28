package org.eclipselabs.real.core.exception;

public class IncorrectPatternExceptionRT extends RuntimeException {

    private String filePath;

    public IncorrectPatternExceptionRT() {
        // TODO Auto-generated constructor stub
    }

    public IncorrectPatternExceptionRT(String fp, String message) {
        super(message);
        filePath = fp;
    }

    public IncorrectPatternExceptionRT(String fp, Throwable cause) {
        super(cause);
        filePath = fp;
    }

    public IncorrectPatternExceptionRT(String fp, String message, Throwable cause) {
        super(message, cause);
        filePath = fp;
    }

    public IncorrectPatternExceptionRT(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
