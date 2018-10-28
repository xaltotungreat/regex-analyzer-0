package org.eclipselabs.real.core.logfile;

import java.io.File;
import java.util.Set;

import org.eclipselabs.real.core.util.IKeyedObjectRepositoryWrite;

/**
 * The basic interface for the log aggregate.
 * A log aggregate is basically a collection of log files with the same
 * log type (i.e. usually belonging to the same software component)
 * @author Vadim Korkin
 *
 */
public interface ILogFileAggregate extends ILogFileAggregateRead, IKeyedObjectRepositoryWrite<String, ILogFile> {

    public enum MultiThreadingState {
        ALLOW_MULTITHREADING_READ,
        DISALLOW_MULTITHREADING_READ;
    }

    public void setReadFilesState(MultiThreadingState newState);
    public Double getAggregateSizeLimit();

    public void removeFolder(String filesDir);
    public void removeFolders(Set<String> filesDirs);

    /**
     * This method creates a new log file object and adds it to this aggregate
     * @param fl the file object that points to the log file
     * @return the created log file in the full interface
     */
    public ILogFile createLogFile(File fl);

    public void cleanAllFiles();

}
