package org.eclipselabs.real.core.logfile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.ITypedObject;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

/**
 * The basic interface for the log aggregate.
 * A log aggregate is basically a collection of log files with the same
 * log type (i.e. usually belonging to the same software component)
 * @author Vadim Korkin
 *
 */
public interface ILogFileAggregate extends ITypedObject<LogFileTypeKey> {

    public static enum MultiThreadingState {
        ALLOW_MULTITHREADING_READ,
        DISALLOW_MULTITHREADING_READ;
    }

    public static Integer FILE_SIZE_LIMIT = 51;

    public ReentrantLock getReadFileLock();
    public MultiThreadingState getReadFilesState();
    public void setReadFilesState(MultiThreadingState newState);
    public Integer getAggregateSizeLimit();

    public CompletableFuture<LogFileAggregateInfo> addFolders(List<String> filesDirs);
    public CompletableFuture<LogFileAggregateInfo> addFolders(List<String> filesDirs, TimeUnitWrapper submitTimeout);

    public void removeFolder(String filesDir);

    public <R extends ISearchResult<O>, O extends ISearchResultObject> CompletableFuture<? extends Map<String,R>> submitSearch(
            ISearchObject<R,O> so, PerformSearchRequest searchRequest, TimeUnitWrapper submitTimeout);
    public <R extends ISearchResult<O>, O extends ISearchResultObject> CompletableFuture<? extends Map<String,R>> submitSearch(
            ISearchObject<R,O> so, PerformSearchRequest searchRequest);

    public LogFileAggregateInfo getInfo();
    public Long getAggregateFilesSize(List<ILogFile> logFiles);

    public Boolean isEmpty();
}
