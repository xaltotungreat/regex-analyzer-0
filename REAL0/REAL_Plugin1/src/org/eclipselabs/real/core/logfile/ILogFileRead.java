package org.eclipselabs.real.core.logfile;

public interface ILogFileRead {

    public String getFileText();
    public String getFilePath();
    public Long getFileSize();
    public Boolean isRead();

    /**
     *
     * @return the state of the file (currently NOT_READ, READING, READ)
     */
    public LogFileState getState();
    /**
     * A ILogFile implementation may store a reference to the parent aggregate.
     * This method is supposed to return this reference.
     * @return the aggregate for this log file
     */
    public ILogFileAggregateRead getAggregate();
    /**
     * Returns the info for this logs file. Without actually reading the file
     * only some part of information is available
     * @return the info for this file
     */
    public LogFileInfo getInfo();
}
