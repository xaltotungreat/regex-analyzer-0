package org.eclipselabs.real.core.logfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.core.event.logfile.ControllerFolderListUpdated;
import org.eclipselabs.real.core.logfile.task.AddLogFileAggregateTaskResult;
import org.eclipselabs.real.core.logfile.task.LogFileAggregateTaskReloadFolders;
import org.eclipselabs.real.core.logfile.task.LogFileTaskExecutor;
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.util.IListenableFutureWatcherCallback;
import org.eclipselabs.real.core.util.ListenableFutureWatcher;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public enum LogFileControllerImpl {
    INSTANCE;

    private static final Logger log = LogManager.getLogger(LogFileControllerImpl.class);

    private final Long DEFAULT_MAX_ALL_LOG_SIZE = (long)50;
    protected Long maxLogAggregateSize;

    protected volatile List<String> controllerLogFolders = Collections.synchronizedList(new ArrayList<String>());
    protected volatile Map<LogFileTypeKey,ILogFileAggregateRep> logAggregateMap = new ConcurrentHashMap<LogFileTypeKey,ILogFileAggregateRep>();
    protected volatile ReentrantReadWriteLock logControllerLock = new ReentrantReadWriteLock();
    protected ExecutorService aggrSizeChangeExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("AggregateSizeChange"));

    protected ListeningExecutorService logFileExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10,
            new NamedThreadFactory("LogFileTask")));

    protected Long controllerOperationTimeout = (long)60;
    protected TimeUnit controllerOperationTimeUnit = TimeUnit.SECONDS;

    private LogFileControllerImpl() {
        int logFileTypes = LogFileTypes.values().length;
        maxLogAggregateSize = DEFAULT_MAX_ALL_LOG_SIZE/logFileTypes;
    }



    public SettableFuture<List<LogFileAggregateInfo>> addFolderFutureList(final String logFilesDir, TimeUnitWrapper timeout) {
        File newLogFilesDir = new File(logFilesDir);
        if (newLogFilesDir.exists() && newLogFilesDir.isDirectory()) {
            return reloadFoldersFutureList(Collections.singletonList(logFilesDir), timeout);
        } else {
            log.error("Not a folder or not exists " + logFilesDir + " returning null future");
        }
        return null;
    }

    public SettableFuture<List<LogFileAggregateInfo>> addFolderFutureList(final String logFilesDir) {
        return addFolderFutureList(logFilesDir, new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    public SettableFuture<List<ListenableFuture<LogFileAggregateInfo>>> addFolderListFutures(final String logFilesDir, TimeUnitWrapper timeout) {
        File newLogFilesDir = new File(logFilesDir);
        if (newLogFilesDir.exists() && newLogFilesDir.isDirectory()) {
            return reloadFoldersListFutures(Collections.singletonList(logFilesDir), timeout);
        } else {
            log.error("Not a folder or not exists " + logFilesDir + " returning null future");
        }
        return null;
    }

    public SettableFuture<List<ListenableFuture<LogFileAggregateInfo>>> addFolderListFutures(final String logFilesDir) {
        return addFolderListFutures(logFilesDir, new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    public SettableFuture<List<LogFileAggregateInfo>> reloadCurrentFoldersFutureList(TimeUnitWrapper timeout) {
        if (!controllerLogFolders.isEmpty()) {
            return reloadFoldersFutureList(controllerLogFolders, timeout);
        } else {
            log.error("No folder in the controller list");
        }
        return null;
    }

    public SettableFuture<List<LogFileAggregateInfo>> reloadCurrentFoldersFutureList() {
        return reloadCurrentFoldersFutureList(new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    public SettableFuture<List<ListenableFuture<LogFileAggregateInfo>>> reloadCurrentFoldersListFutures(TimeUnitWrapper timeout) {
        if (!controllerLogFolders.isEmpty()) {
            return reloadFoldersListFutures(new ArrayList<String>(controllerLogFolders), timeout);
        } else {
            log.error("No folders to reload");
        }
        return null;
    }

    public SettableFuture<List<ListenableFuture<LogFileAggregateInfo>>> reloadCurrentFoldersListFutures() {
        return reloadCurrentFoldersListFutures(new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    public SettableFuture<List<LogFileAggregateInfo>> reloadFoldersFutureList(List<String> folders, TimeUnitWrapper timeout) {
        if ((folders != null) && (!folders.isEmpty())) {
            boolean lockObtained = false;
            try {
                if (logControllerLock.writeLock().tryLock()
                        || logControllerLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                    lockObtained = true;
                    log.debug("reloadFoldersListFutures() write lock obtained");
                    List<LogFileAggregateTaskReloadFolders<List<LogFileAggregateInfo>>> taskList = new ArrayList<LogFileAggregateTaskReloadFolders<List<LogFileAggregateInfo>>>();
                    Long cumulativeReadWaitTimeout = (long) 0;
                    Long cumulativeReadTimeout = (long) 0;
                    final Set<LogFileTypeKey> allTypes
                        = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(true));
                    for (LogFileTypeKey currType : allTypes) {
                        ILogFileAggregateRep logAggr = getLogAggregateRep(currType);
                        Long currReadWaitTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ_WAIT, logAggr).getTimeout();
                        Long currReadTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ, logAggr).getTimeout();
                        cumulativeReadWaitTimeout += currReadWaitTimeout;
                        cumulativeReadTimeout += currReadTimeout;
                        AddLogFileAggregateTaskResult<LogFileAggregateInfo, List<LogFileAggregateInfo>> currAddResult = new AddLogFileAggregateTaskResult<LogFileAggregateInfo, List<LogFileAggregateInfo>>(logAggr) {

                            @Override
                            public List<LogFileAggregateInfo> addResult(LogFileAggregateInfo taskResult, List<LogFileAggregateInfo> mainResult) {
                                if (taskResult != null) {
                                    mainResult.add(taskResult);
                                } else {
                                    log.warn("Null result for aggregate " + getLogFileAggregate().getType());
                                }
                                return mainResult;
                            }
                        };
                        LogFileAggregateTaskReloadFolders<List<LogFileAggregateInfo>> newTask
                                = new LogFileAggregateTaskReloadFolders<List<LogFileAggregateInfo>>(folders, logAggr, currAddResult,
                                        new TimeUnitWrapper(currReadWaitTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()),
                                        new TimeUnitWrapper(currReadTimeout, LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
                        taskList.add(newTask);
                        log.debug("LogFileAggregateTaskReloadFolders added task for " + logAggr.getType());
                    }
                    List<String> oldFolders = new ArrayList<>(controllerLogFolders);
                    removeManyFolders(folders, timeout);
                    controllerLogFolders.addAll(folders);
                    List<String> newFolders = new ArrayList<>(controllerLogFolders);
                    if ((!oldFolders.containsAll(newFolders)) || (!newFolders.containsAll(oldFolders))) {
                        log.debug("Firing Controller Folder List Changed event");
                        ControllerFolderListUpdated newFldListEvent = new ControllerFolderListUpdated(oldFolders, newFolders);
                        CoreEventBus.INSTANCE.postSingleThreadAsync(newFldListEvent);
                    }
                    List<Lock> locks = new ArrayList<Lock>();
                    locks.add(logControllerLock.writeLock());
                    SettableFuture<List<LogFileAggregateInfo>> contrFuture = SettableFuture.<List<LogFileAggregateInfo>> create();
                    LogFileTaskExecutor<LogFileAggregateInfo, List<LogFileAggregateInfo>> theTaskExecutor
                                = new LogFileTaskExecutor<LogFileAggregateInfo, List<LogFileAggregateInfo>>(
                                        "LogAggregateAddFolder", logFileExecutor, taskList, contrFuture,
                                        new ArrayList<LogFileAggregateInfo>(), locks,
                                        new TimeUnitWrapper((long)5, TimeUnit.SECONDS),
                                        new TimeUnitWrapper(cumulativeReadWaitTimeout + cumulativeReadTimeout,
                                                LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
                    theTaskExecutor.execute();
                    return contrFuture;
                } else {
                    log.error("reloadFoldersFutureList Timeout trying to get write lock");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted exception", e);
            } finally {
                if (lockObtained && logControllerLock.isWriteLockedByCurrentThread()) {
                    logControllerLock.writeLock().unlock();
                    log.debug("reloadFoldersFutureList() write lock unlocked");
                }
            }
        } else {
            log.error("reloadFoldersFutureList null folders list");
        }
        return null;
    }

    public SettableFuture<List<ListenableFuture<LogFileAggregateInfo>>> reloadFoldersListFutures(final List<String> folders, TimeUnitWrapper timeout) {
        if ((folders != null) && (!folders.isEmpty())) {
            boolean lockObtained = false;
            try {
                if (logControllerLock.writeLock().tryLock()
                        || logControllerLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                    log.debug("reloadFoldersListFutures() write lock obtained");
                    lockObtained = true;
                    final Set<LogFileTypeKey> allTypes
                        = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(true));
                    SettableFuture<List<ListenableFuture<LogFileAggregateInfo>>> returnFuture = SettableFuture.<List<ListenableFuture<LogFileAggregateInfo>>>create();
                    List<String> oldFolders = new ArrayList<>(controllerLogFolders);
                    removeManyFolders(folders, timeout);
                    controllerLogFolders.addAll(folders);
                    List<String> newFolders = new ArrayList<>(controllerLogFolders);
                    if ((!oldFolders.containsAll(newFolders)) || (!newFolders.containsAll(oldFolders))) {
                        log.debug("Firing Controller Folder List Changed event");
                        ControllerFolderListUpdated newFldListEvent = new ControllerFolderListUpdated(oldFolders, newFolders);
                        CoreEventBus.INSTANCE.postSingleThreadAsync(newFldListEvent);
                    }
                    Long cumulativeReadWaitTimeout = (long) 0;
                    Long cumulativeReadTimeout = (long) 0;
                    for (final LogFileTypeKey currType : allTypes) {
                        ILogFileAggregateRep logAggr = getLogAggregateRep(currType);
                        Long currReadWaitTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ_WAIT, logAggr).getTimeout();
                        Long currReadTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogTimeoutPolicy.OperationType.LOG_FILE_READ, logAggr).getTimeout();
                        cumulativeReadWaitTimeout += currReadWaitTimeout;
                        cumulativeReadTimeout += currReadTimeout;
                    }
                    IListenableFutureWatcherCallback<LogFileAggregateInfo> newCallback = new IListenableFutureWatcherCallback<LogFileAggregateInfo>() {

                        @Override
                        public List<ListenableFuture<LogFileAggregateInfo>> submitTasks(
                                ListenableFutureWatcher<LogFileAggregateInfo> watcher) {
                            log.debug("Listenable Future Watcher Submitting tasks");
                            List<ListenableFuture<LogFileAggregateInfo>> fList = Collections.synchronizedList(new ArrayList<ListenableFuture<LogFileAggregateInfo>>());
                            for (final LogFileTypeKey currType : allTypes) {
                                ILogFileAggregateRep logAggr = getLogAggregateRep(currType);
                                ListenableFuture<LogFileAggregateInfo> loadFuture = logAggr.addFolders(folders);
                                fList.add(loadFuture);
                            }
                            return fList;
                        }

                        @Override
                        public void executionComplete(List<ListenableFuture<LogFileAggregateInfo>> currentFutures) {
                            log.debug("Listenable Future Watcher execution complete. Setting unfinished to error results");
                            int i = 0;
                            for (ListenableFuture<LogFileAggregateInfo> lFuture : currentFutures) {

                                if (!lFuture.isDone()) {
                                    lFuture.cancel(true);
                                    i++;
                                }
                            }
                            log.warn("Cancelled futures " + i);
                        }
                    };
                    List<Lock> locks = new ArrayList<Lock>();
                    locks.add(logControllerLock.writeLock());
                    ListenableFutureWatcher<LogFileAggregateInfo> futureWatcher
                        = new ListenableFutureWatcher<LogFileAggregateInfo>("AddFolderWatcher", locks, newCallback, returnFuture, null);
                    log.info("reloadFoldersFutureList waitTO " + cumulativeReadWaitTimeout
                            + " execTO " + (cumulativeReadWaitTimeout + cumulativeReadTimeout));
                    futureWatcher.startWatch(new TimeUnitWrapper(cumulativeReadWaitTimeout, TimeUnit.SECONDS),
                            new TimeUnitWrapper(cumulativeReadWaitTimeout + cumulativeReadTimeout,
                                    LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
                    return returnFuture;
                } else {
                    log.error("reloadFoldersListFutures Timeout trying to get write lock");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted exception", e);
            } finally {
                if (lockObtained && logControllerLock.isWriteLockedByCurrentThread()) {
                    logControllerLock.writeLock().unlock();
                    log.debug("reloadFoldersListFutures() write lock unlocked");
                }
            }
        } else {
            log.error("reloadFoldersFutureList null folders list");
        }
        return null;
    }

    public ILogFileAggregate getLogAggregate(LogFileTypeKey type, TimeUnitWrapper timeout) {
        boolean lockObtained = false;
        try {
            if (logControllerLock.readLock().tryLock()
                    || logControllerLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                ILogFileAggregate res = logAggregateMap.get(type);
                log.debug("Getting Aggregate for type = " + type + " res=" + res);
                return res;
            } else {
                log.error("getLogAggregate Timeout trying to get read lock");
            }
        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
        } finally {
            if (lockObtained) {
                logControllerLock.readLock().unlock();
            }
        }
        return null;
    }

    public ILogFileAggregate getLogAggregate(LogFileTypeKey type) {
        return getLogAggregate(type, new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    public Boolean isLogFilesAvailable(List<LogFileTypeKey> typesList) {
        Boolean available = true;
        if (typesList != null) {
            for (LogFileTypeKey lftKey : typesList) {
                if (lftKey != null) {
                    ILogFileAggregate currAggr = getLogAggregate(lftKey);
                    if ((currAggr == null) || (currAggr.isEmpty())) {
                        available = false;
                        break;
                    }
                }
            }
        }
        return available;
    }

    public LogFileTypeKey getFirstNotAvailable(List<LogFileTypeKey> typesList) {
        LogFileTypeKey firstNA = null;
        for (LogFileTypeKey lftKey : typesList) {
            if (lftKey != null) {
                ILogFileAggregate currAggr = getLogAggregate(lftKey);
                if ((currAggr == null) || (currAggr.isEmpty())) {
                    firstNA = lftKey;
                    break;
                }
            }
        }
        return firstNA;
    }

    public Long getMaxLogAggregateSize() {
        return maxLogAggregateSize;
    }

    public void removeFolders(List<String> logFilesDir, TimeUnitWrapper timeout) {
        for (String currFolder : logFilesDir) {
            removeFolder(currFolder, timeout);
        }
    }

    public void removeFolder(String logFilesDir) {
        removeFolder(logFilesDir, new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }


    public void removeFolder(String logFilesDir, TimeUnitWrapper timeout) {
        if (controllerLogFolders.contains(logFilesDir)) {
            boolean lockObtained = false;
            try {
                if (logControllerLock.writeLock().tryLock()
                        || logControllerLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                    lockObtained = true;
                    List<String> oldFolders = new ArrayList<>(controllerLogFolders);
                    removeOneFolder(logFilesDir, timeout);
                    List<String> newFolders = new ArrayList<>(controllerLogFolders);
                    if ((!oldFolders.containsAll(newFolders)) || (!newFolders.containsAll(oldFolders))) {
                        log.debug("Firing Controller Folder List Changed event");
                        ControllerFolderListUpdated newFldListEvent = new ControllerFolderListUpdated(oldFolders, newFolders);
                        CoreEventBus.INSTANCE.postSingleThreadAsync(newFldListEvent);
                    }
                } else {
                    log.error("removeFolder Timeout trying to get write lock");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted exception", e);
            } finally {
                if (lockObtained && logControllerLock.isWriteLockedByCurrentThread()) {
                    logControllerLock.writeLock().unlock();
                }
            }
        }
    }

    protected void removeManyFolders(List<String> logFilesDir, TimeUnitWrapper timeout) {
        for (String currFolder : logFilesDir) {
            removeOneFolder(currFolder, timeout);
        }
    }

    protected void removeOneFolder(String logFilesDir) {
        removeOneFolder(logFilesDir, new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    protected void removeOneFolder(String logFilesDir, TimeUnitWrapper timeout) {
        if (controllerLogFolders.contains(logFilesDir)) {
            Collection<ILogFileAggregateRep> allAggr = logAggregateMap.values();
            for (ILogFileAggregateRep lfag : allAggr) {
                lfag.removeFolder(logFilesDir);
            }
            controllerLogFolders.remove(logFilesDir);
        } else {
            log.error("removeOneFolder Folder not in the list " + logFilesDir);
        }
    }

    public List<String> getAllFolders() {
        return new ArrayList<String>(controllerLogFolders);
    }

    public List<LogFileAggregateInfo> getInfos(TimeUnitWrapper timeout) {
        boolean lockObtained = false;
        try {
            if (logControllerLock.readLock().tryLock()
                    || logControllerLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                List<LogFileAggregateInfo> resultList = new ArrayList<LogFileAggregateInfo>();
                Set<LogFileTypeKey> allTypes
                    = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(true));
                for (final LogFileTypeKey currType : allTypes) {
                    ILogFileAggregateRep logAggr = getLogAggregateRep(currType);
                    resultList.add(logAggr.getInfo());
                }
                return resultList;
            } else {
                log.error("getInfos Timeout trying to get read lock");
            }
        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
        } finally {
            if (lockObtained) {
                logControllerLock.readLock().unlock();
            }
        }
        return null;
    }

    public List<LogFileAggregateInfo> getInfos() {
        return getInfos(new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    protected ILogFileAggregateRep getLogAggregateRep(LogFileTypeKey type) {
        ILogFileAggregateRep logAggr;
        if (logAggregateMap.containsKey(type)) {
            logAggr = logAggregateMap.get(type);
        } else {
            logAggr = new LogFileAggregateImpl(type, logFileExecutor, logControllerLock.readLock());
            logAggregateMap.put(type, logAggr);
        }
        return logAggr;
    }

    public ExecutorService getAggrSizeChangeExecutor() {
        return aggrSizeChangeExecutor;
    }

}
