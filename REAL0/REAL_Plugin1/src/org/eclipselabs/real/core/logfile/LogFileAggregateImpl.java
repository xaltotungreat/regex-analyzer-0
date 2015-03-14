package org.eclipselabs.real.core.logfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.core.event.logfile.LogFileTypeStateChangedEvent;
import org.eclipselabs.real.core.logfile.task.AddLogFileAggregateTaskResult;
import org.eclipselabs.real.core.logfile.task.AddLogFileTaskResult;
import org.eclipselabs.real.core.logfile.task.LogFileAggregateTaskReloadFolders;
import org.eclipselabs.real.core.logfile.task.LogFileTaskExecutor;
import org.eclipselabs.real.core.logfile.task.LogFileTaskRead;
import org.eclipselabs.real.core.logfile.task.LogFileTaskSearch;
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypeState;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.KeyedObjectRepositoryImpl;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.core.util.RepositorySizeChangedEvent;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

/**
 * This class represents an abstraction - a group of log files from one application component.
 * For example if Comp1 generates log4j logs they are usually divided into files and there is
 * a limit for the max size of one file. LogFileAggregateImpl allows the core components
 * to work with a group of files as with one file. For example submit search requests or
 * read all log files for Comp1 from the given folder.
 * @author Vadim Korkin
 *
 */
public class LogFileAggregateImpl extends KeyedObjectRepositoryImpl<String, ILogFile> implements ILogFileAggregateRep {
    private static final Logger log = LogManager.getLogger(LogFileAggregateImpl.class);

    protected MultiThreadingState theReadFilesState = MultiThreadingState.ALLOW_MULTITHREADING_READ;

    // performance property keys
    protected static final String PERF_CONST_SEARCH_THREADS = "org.eclipselabs.real.core.logfile.SearchThreadsNumberPerAggregate";
    protected static final String PERF_CONST_AGGREGATE_SIZE_LIMIT = "org.eclipselabs.real.core.logfile.AggregateSizeLimit";

    protected LogFileTypeKey lfTypeKey;
    protected Integer aggregateSizeLimit = FILE_SIZE_LIMIT;
    protected ListeningExecutorService logFileAggregateExecutor;

    protected ReentrantLock operationPendingLock = new ReentrantLock();
    protected ReentrantLock readFileLock = new ReentrantLock();
    protected Lock contrReadLock;

    /**
     * The constructor initializes a AsyncEventBus (for KeyedObjectRepository) with a single-threaded executor.
     * This makes sure the events from the KeyedObjectRepository are received in the right order.
     * @param aType the type of the logfile in the form of a LogFileTypeKey
     * @param executor the tasks of this aggregate are executed in this executor
     * Currently this param is not used as all aggregates now have their own thread pool consisting of 2 threads.
     * later this will be one of performance parameters
     * @param contrRLock the read lock of the log file controller. When a search is submitted
     * the aggregate must lock the read lock to block any modification to the controller
     * until the search is completed
     */
    public LogFileAggregateImpl(LogFileTypeKey aType, ListeningExecutorService executor, Lock contrRLock) {
        repositoryEventBus = new AsyncEventBus(LogFileControllerImpl.INSTANCE.getAggrSizeChangeExecutor());
        repositoryEventBus.register(this);
        lfTypeKey = aType;
        // creating the executor for search/read requests
        NamedThreadFactory newFactory = null;
        if ((lfTypeKey != null) && (lfTypeKey.getLogTypeName() != null)) {
            newFactory = new NamedThreadFactory("Aggr-" + lfTypeKey.getLogTypeName());
        } else {
            newFactory = new NamedThreadFactory("No Log Type");
        }
        // loaded from the performance config, 2 threads by default
        int threadsNumber = PerformanceUtils.getIntProperty(PERF_CONST_SEARCH_THREADS, 2);
        logFileAggregateExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(threadsNumber, newFactory));
        contrReadLock = contrRLock;
        aggregateSizeLimit = PerformanceUtils.getIntProperty(PERF_CONST_AGGREGATE_SIZE_LIMIT, aggregateSizeLimit);
    }

    @Override
    public ListenableFuture<LogFileAggregateInfo> addFolders(List<String> filesDirs, TimeUnitWrapper submitTimeout) {
        Long currReadWaitTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ_WAIT, this).getTimeout();
        Long currReadTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ, this).getTimeout();
        AddLogFileAggregateTaskResult<LogFileAggregateInfo, LogFileAggregateInfo> currAddResult = new AddLogFileAggregateTaskResult<LogFileAggregateInfo, LogFileAggregateInfo>(this) {

            @Override
            public LogFileAggregateInfo addResult(LogFileAggregateInfo taskResult, LogFileAggregateInfo mainResult) {
                if (taskResult != null) {
                    mainResult = new LogFileAggregateInfo(taskResult);
                } else {
                    log.warn("Null result for aggregate " + getLogFileAggregate().getType());
                }
                return mainResult;
            }
        };
        LogFileAggregateTaskReloadFolders<LogFileAggregateInfo> newTask = new LogFileAggregateTaskReloadFolders<LogFileAggregateInfo>(
                filesDirs, this, currAddResult,
                new TimeUnitWrapper(currReadWaitTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()),
                new TimeUnitWrapper(currReadTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
        log.debug("LogFileAggregateTaskReloadFolders added task for " + this.getType());
        return logFileAggregateExecutor.submit(newTask);
    }

    @Override
    public ListenableFuture<LogFileAggregateInfo> addFolders(List<String> filesDirs) {
        return addFolders(filesDirs, new TimeUnitWrapper((long)5, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
    }

    @Override
    public void removeFolder(String filesDir) {
        log.info("Removing folder type=" + getType().getLogTypeName() + " dir=" + filesDir);
        List<ILogFile> allLF = getAllValues();
        for (ILogFile currLF : allLF) {
            log.debug("Curr logFile path=" + currLF.getFilePath());
            if (currLF.getFilePath().startsWith(filesDir)) {
                remove(currLF.getFilePath());
            }
        }
    }

    @Override
    public <R extends ISearchResult<O>, O extends ISearchResultObject> ListenableFuture<? extends Map<String, R>> submitSearch(ISearchObject<R, O> so,
            PerformSearchRequest searchRequest, TimeUnitWrapper submitTimeout) {
        SettableFuture<ConcurrentHashMap<String, R>> returnFuture = null;
        List<LogFileTaskSearch<R, ConcurrentHashMap<String, R>>> taskList = new ArrayList<LogFileTaskSearch<R, ConcurrentHashMap<String, R>>>();
        List<ILogFile> allLogFiles = getAllValues();
        Long cumulativeSearchWaitTimeout = (long) 0;
        Long cumulativeSearchTimeout = (long) 0;
        if ((allLogFiles != null) && (!allLogFiles.isEmpty())) {
            searchRequest.getProgressMonitor().setTotalSOFiles(allLogFiles.size());
            for (ILogFile currLogFile : allLogFiles) {
                Long currSearchWaitTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.SEARCH_WAIT, this, currLogFile).getTimeout();
                Long currSearchTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.SEARCH, this, currLogFile).getTimeout();
                AddLogFileTaskResult<R, ConcurrentHashMap<String, R>> currAddResult = new AddLogFileTaskResult<R, ConcurrentHashMap<String, R>>(currLogFile) {

                    @Override
                    public ConcurrentHashMap<String, R> addResult(R taskResult, ConcurrentHashMap<String, R> mainResult) {
                        if (taskResult != null) {
                            log.debug("Add result logFile=" + getLogFile().getFilePath() + " result size=" + taskResult.getSRObjects().size());
                            mainResult.put(getLogFile().getFilePath(), taskResult);
                        } else {
                            log.debug("NULL result for logFile=" + getLogFile().getFilePath());
                        }
                        return mainResult;
                    }
                };
                LogFileTaskSearch<R, ConcurrentHashMap<String, R>> newTask = new LogFileTaskSearch<R, ConcurrentHashMap<String, R>>(
                        currAddResult, currLogFile, so, searchRequest.getSharedMonitorCopy(),
                        new TimeUnitWrapper(currSearchWaitTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()),
                        new TimeUnitWrapper(currSearchTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
                taskList.add(newTask);
                cumulativeSearchWaitTimeout += currSearchWaitTimeout;
                cumulativeSearchTimeout += currSearchTimeout;
            }
            List<Lock> locks = new ArrayList<Lock>();
            locks.add(getReadLock());
            locks.add(contrReadLock);
            returnFuture = SettableFuture.<ConcurrentHashMap<String, R>> create();
            LogFileTaskExecutor<R, ConcurrentHashMap<String, R>> theTaskExecutor = new LogFileTaskExecutor<R, ConcurrentHashMap<String, R>>("LogSearch-" + lfTypeKey.getLogTypeName(),
                    logFileAggregateExecutor, taskList, returnFuture, new ConcurrentHashMap<String, R>(), locks, new TimeUnitWrapper(2 * putTimeout, putTimeUnit),
                    new TimeUnitWrapper(cumulativeSearchWaitTimeout + cumulativeSearchTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
            theTaskExecutor.execute();
        } else {
            log.error("No files found");
        }
        return returnFuture;
    }

    @Override
    public <R extends ISearchResult<O>, O extends ISearchResultObject> ListenableFuture<? extends Map<String, R>> submitSearch(ISearchObject<R, O> so,
            PerformSearchRequest searchRequest) {
        return submitSearch(so, searchRequest, new TimeUnitWrapper(DEFAULT_READ_TIMEOUT, DEFAULT_READ_TIME_UNIT));
    }

    public ListenableFuture<LogFileAggregateInfo> readFiles(List<ILogFile> files, LogFileAggregateInfo res, TimeUnitWrapper submitTimeout) {
        SettableFuture<LogFileAggregateInfo> returnFuture = null;
        try {
            if (operationPendingLock.tryLock() || operationPendingLock.tryLock(submitTimeout.getTimeout(), submitTimeout.getTimeUnit())) {
                log.debug("ReadFiles operationpending LOCK");
                List<LogFileTaskRead<LogFileAggregateInfo>> taskList = new ArrayList<LogFileTaskRead<LogFileAggregateInfo>>();
                Long cumulativeReadWaitTimeout = (long)0;
                Long cumulativeReadTimeout = (long)0;
                for (ILogFile currLogFile : files) {
                    Long currReadWaitTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ_WAIT, this, currLogFile).getTimeout();
                    Long currReadTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ, this, currLogFile).getTimeout();
                    AddLogFileTaskResult<LogFileInfo, LogFileAggregateInfo> currAddResult = new AddLogFileTaskResult<LogFileInfo, LogFileAggregateInfo>(currLogFile) {

                        @Override
                        public LogFileAggregateInfo addResult(LogFileInfo taskResult, LogFileAggregateInfo mainResult) {
                            mainResult.addLogFileInfo(taskResult);
                            return mainResult;
                        }
                    };
                    LogFileTaskRead<LogFileAggregateInfo> newTask = new LogFileTaskRead<LogFileAggregateInfo>(currAddResult, currLogFile,
                            new TimeUnitWrapper(currReadWaitTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()),
                            new TimeUnitWrapper(currReadTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
                    taskList.add(newTask);
                    log.debug("LogReadTask added file=" + currLogFile.getFilePath());
                }
                List<Lock> locks = new ArrayList<Lock>();
                locks.add(getWriteLock());
                returnFuture = SettableFuture.<LogFileAggregateInfo> create();
                LogFileTaskExecutor<LogFileInfo, LogFileAggregateInfo> theTaskExecutor
                        = new LogFileTaskExecutor<LogFileInfo, LogFileAggregateInfo>(
                        "LogRead-" + lfTypeKey.getLogTypeName(), logFileAggregateExecutor,taskList, returnFuture,
                        res, locks, new TimeUnitWrapper(2*getTimeout, getTimeUnit),
                        new TimeUnitWrapper(cumulativeReadWaitTimeout + cumulativeReadTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
                theTaskExecutor.execute();
            } else {
                log.error("Error reading files ");
            }
        } catch (InterruptedException e) {
            log.error(lfTypeKey + " loadFiles exception", e);
        } finally {
            if (operationPendingLock.isLocked() && operationPendingLock.isHeldByCurrentThread()) {
                operationPendingLock.unlock();
                log.debug("ReadFiles operationpending UNLOCK");
            }
        }
        return returnFuture;
    }

    @Override
    public Long getAggregateFilesSize(List<ILogFile> logFiles) {
        Long result = (long)0;
        for (ILogFile lf : logFiles) {
            result += lf.getFileSize();
        }
        return result;
    }

    @Override
    public LogFileTypeKey getType() {
        return lfTypeKey;
    }


    protected Long getSearchTimeout(int fileLength) {
        return (long)0;
    }

    @Override
    public String toString() {
        return "LogFileAggregateImpl [lfTypeKey=" + lfTypeKey + ", \n\t LogFiles=" + getAllValues() + "]";
    }

    @Override
    public synchronized ReentrantLock getReadFileLock() {
        return readFileLock;
    }

    @Override
    public MultiThreadingState getReadFilesState() {
        return theReadFilesState;
    }

    @Override
    public LogFileAggregateInfo getInfo() {
        LogFileAggregateInfo info = new LogFileAggregateInfo(getType());
        List<ILogFile> allFiles = getAllValues();
        for (ILogFile lf : allFiles) {
            info.addLogFileInfo(lf.getInfo());
        }
        return info;
    }

    @Override
    public void setReadFilesState(MultiThreadingState newState) {
        theReadFilesState = newState;
    }

    @Override
    public Integer getAggregateSizeLimit() {
        return aggregateSizeLimit;
    }

    /**
     * This method processes events from the KeyedObjectRepository when
     * the size of the map changes.
     * @param rscEvent the size change event
     */
    @Subscribe
    public void handleSizeChange(RepositorySizeChangedEvent rscEvent) {
        log.debug("handleSizeChange " + getType() + " old=" + rscEvent.getOldSize() + " new=" + rscEvent.getNewSize());

        LogFileType thisType = LogFileTypes.INSTANCE.getLogFileType(lfTypeKey.getLogTypeName());
        if (thisType != null) {
            LogFileTypeState oldSt = thisType.getStateInfoCopy();
            if ((rscEvent.getOldSize() == 0) && (rscEvent.getNewSize() > 0)) {
                thisType.setAvailable(true);
            } else if ((rscEvent.getOldSize() > 0) && (rscEvent.getNewSize() == 0)) {
                thisType.setAvailable(false);
            }
            LogFileTypeState newSt = thisType.getStateInfoCopy();
            LogFileTypeStateChangedEvent newEvent = new LogFileTypeStateChangedEvent(getType(), oldSt, newSt);
            CoreEventBus.INSTANCE.postSync(newEvent);
        }

        log.debug("handleSizeChange complete " + getType() + " old=" + rscEvent.getOldSize() + " new=" + rscEvent.getNewSize());
    }




}
