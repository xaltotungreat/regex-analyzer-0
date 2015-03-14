package org.eclipselabs.real.core.logfile;

/**
 * This class contains the information about one attempt to read
 * a log file. Every time a file is read it returns an instance of this class.
 * @author Vadim Korkin
 *
 */
public class LogFileInfo {

    protected String fileFullName;
    protected Boolean inMemory;
    protected Boolean lastReadSuccessful;
    protected Exception lastReadException;
    protected Double fileSize;
    
    public String getFileFullName() {
        return fileFullName;
    }
    public void setFileFullName(String fileFullName) {
        this.fileFullName = fileFullName;
    }
    public Boolean getInMemory() {
        return inMemory;
    }
    public void setInMemory(Boolean inMemory) {
        this.inMemory = inMemory;
    }
    public Boolean getLastReadSuccessful() {
        return lastReadSuccessful;
    }
    public void setLastReadSuccessful(Boolean lastReadSuccessful) {
        this.lastReadSuccessful = lastReadSuccessful;
    }
    public Exception getLastReadException() {
        return lastReadException;
    }
    public void setLastReadException(Exception lastReadException) {
        this.lastReadException = lastReadException;
    }
    
    public Double getFileSize() {
        return fileSize;
    }
    public void setFileSize(Double fileSize) {
        this.fileSize = fileSize;
    }
    
    @Override
    public String toString() {
        return "LogFileInfo [fileFullName=" + fileFullName + ", inMemory=" + inMemory + ", lastReadSuccessful="
                + lastReadSuccessful + ", lastReadException=" + lastReadException + "]";
    }
    
}
