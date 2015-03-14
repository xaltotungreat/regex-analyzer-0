package org.eclipselabs.real.core.logfile;

import java.util.concurrent.locks.ReentrantLock;

/**
 * The basic logfile interface. A basic log file
 * is actually a file in the file system.
 * A ILogFile can be read that means its contents are in the memory
 * or not read i.e. it contains only the path.
 * Usually only small files are read into memory. In other cases the files
 * are read on demand for example when a search has been submitted to
 * the corresponding aggregate.
 *
 * @author Vadim Korkin
 *
 */
public interface ILogFile {
    /**
     * Reads the file and returns the resulting {@link}LogFileInfo object.
     * The object contains all the information about the read operation including
     * the exception that happened etc.
     * @return the resulting {@link}LogFileInfo object
     */
    public LogFileInfo readFile();
    public String getFileText(boolean cleanCharArray);
    public String getFilePath();
    public Long getFileSize();
    public Boolean isRead();
    /**
     * Only makes sense for files already read into memory. This method must clean up
     * all memory occupied by the file by manual setting the pointers to null
     * and manually calling garbage collection.
     */
    public void cleanFile();
    public ReentrantLock getReadLock();
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
    public ILogFileAggregate getAggregate();
    /**
     * Returns the info for this logs file. Without actually reading the file
     * only some part of information is available
     * @return the info for this file
     */
    public LogFileInfo getInfo();
}
