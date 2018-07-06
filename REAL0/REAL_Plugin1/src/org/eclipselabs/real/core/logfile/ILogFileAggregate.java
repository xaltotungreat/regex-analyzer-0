package org.eclipselabs.real.core.logfile;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipselabs.real.core.util.IKeyedObjectRepositoryWrite;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

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

    public CompletableFuture<LogFileAggregateInfo> addFolders(List<String> filesDirs);
    public CompletableFuture<LogFileAggregateInfo> addFolders(List<String> filesDirs, TimeUnitWrapper submitTimeout);

    public void removeFolder(String filesDir);

    /**
     * This method creates a new log file object and adds it to this aggregate
     * @param fl the file object that points to the log file
     * @return the created log file in the full interface
     */
    public ILogFile createLogFile(File fl);

    public void cleanAllFiles();

    /*public <R extends ISearchResult<O>, O extends ISearchResultObject> CompletableFuture<? extends Map<String,R>> submitSearch(
            ISearchObject<R,O> so, PerformSearchRequest searchRequest, TimeUnitWrapper submitTimeout);
    public <R extends ISearchResult<O>, O extends ISearchResultObject> CompletableFuture<? extends Map<String,R>> submitSearch(
            ISearchObject<R,O> so, PerformSearchRequest searchRequest);*/

}
