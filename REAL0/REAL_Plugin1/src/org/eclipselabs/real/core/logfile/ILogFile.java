package org.eclipselabs.real.core.logfile;

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
public interface ILogFile extends ILogFileRead {
    /**
     * Reads the file and returns the resulting {@link}LogFileInfo object.
     * The object contains all the information about the read operation including
     * the exception that happened etc.
     * @return the resulting {@link}LogFileInfo object
     */
    public LogFileInfo readFile();
    /**
     * Only makes sense for files already read into memory. This method must clean up
     * all memory occupied by the file by manual setting the pointers to null
     * and manually calling garbage collection.
     */
    public void cleanFile();


}
