package org.eclipselabs.real.core.logfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.util.CompletableFutureWatcher;
import org.eclipselabs.real.core.util.ICompletableFutureWatcherCallback;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public enum LogFileControllerImpl {
    INSTANCE;

    private static final Logger log = LogManager.getLogger(LogFileControllerImpl.class);

    protected volatile List<String> controllerLogFolders = Collections.synchronizedList(new ArrayList<String>());
    protected volatile Map<LogFileTypeKey,ILogFileAggregate> logAggregateMap = new ConcurrentHashMap<>();
    protected volatile ReentrantReadWriteLock logControllerLock = new ReentrantReadWriteLock();
    protected ExecutorService aggrSizeChangeExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("AggregateSizeChange"));

    protected Long controllerOperationTimeout = (long)60;
    protected TimeUnit controllerOperationTimeUnit = TimeUnit.SECONDS;

    private LogFileControllerImpl() {
    }



    public CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> addFolderListFutures(final String logFilesDir, TimeUnitWrapper timeout) {
        File newLogFilesDir = new File(logFilesDir);
        if (newLogFilesDir.exists() && newLogFilesDir.isDirectory()) {
            return reloadFoldersListFutures(Collections.singletonList(logFilesDir), timeout);
        }
        log.error("Not a folder or not exists " + logFilesDir + " returning null future");
        return null;
    }

    public CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> addFolderListFutures(final String logFilesDir) {
        return addFolderListFutures(logFilesDir, new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    public CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> reloadCurrentFoldersListFutures(TimeUnitWrapper timeout) {
        if (!controllerLogFolders.isEmpty()) {
            return reloadFoldersListFutures(new ArrayList<>(controllerLogFolders), timeout);
        }
        log.error("No folders to reload");
        return null;
    }

    public CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> reloadCurrentFoldersListFutures() {
        return reloadCurrentFoldersListFutures(new TimeUnitWrapper(controllerOperationTimeout, controllerOperationTimeUnit));
    }

    public CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> reloadFoldersListFutures(final List<String> folders, TimeUnitWrapper timeout) {
        if ((folders != null) && (!folders.isEmpty())) {
            boolean lockObtained = false;
            try {
                if (logControllerLock.writeLock().tryLock()
                        || logControllerLock.writeLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                    log.debug("reloadFoldersListFutures() write lock obtained");
                    lockObtained = true;
                    final Set<LogFileTypeKey> allTypes
                        = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(true));
                    CompletableFuture<List<CompletableFuture<LogFileAggregateInfo>>> returnFuture = new CompletableFuture<>();
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
                        ILogFileAggregate logAggr = getLogAggregateRep(currType);
                        Long currReadWaitTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.LOG_FILE_READ_WAIT, logAggr).getTimeout();
                        Long currReadTimeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.LOG_FILE_READ, logAggr).getTimeout();
                        cumulativeReadWaitTimeout += currReadWaitTimeout;
                        cumulativeReadTimeout += currReadTimeout;
                    }
                    ICompletableFutureWatcherCallback<LogFileAggregateInfo> newCallback = new ICompletableFutureWatcherCallback<LogFileAggregateInfo>() {

                        @Override
                        public List<CompletableFuture<LogFileAggregateInfo>> submitTasks(
                                CompletableFutureWatcher<LogFileAggregateInfo> watcher) {
                            log.debug("Completable Future Watcher Submitting tasks");
                            List<CompletableFuture<LogFileAggregateInfo>> fList = Collections.synchronizedList(new ArrayList<CompletableFuture<LogFileAggregateInfo>>());
                            for (final LogFileTypeKey currType : allTypes) {
                                ILogFileAggregate logAggr = getLogAggregateRep(currType);
                                CompletableFuture<LogFileAggregateInfo> loadFuture = logAggr.addFolders(folders);
                                fList.add(loadFuture);
                            }
                            return fList;
                        }

                        @Override
                        public void executionComplete(List<CompletableFuture<LogFileAggregateInfo>> currentFutures) {
                            log.debug("Completable Future Watcher execution complete. Setting unfinished to error results");
                            int i = 0;
                            for (CompletableFuture<LogFileAggregateInfo> lFuture : currentFutures) {

                                if (!lFuture.isDone()) {
                                    lFuture.cancel(true);
                                    i++;
                                }
                            }
                            log.warn("Cancelled futures " + i);
                        }
                    };
                    List<Lock> locks = new ArrayList<>();
                    locks.add(logControllerLock.writeLock());
                    CompletableFutureWatcher<LogFileAggregateInfo> futureWatcher
                        = new CompletableFutureWatcher<>("AddFolderWatcher", locks, newCallback, returnFuture, null);
                    log.info("reloadFoldersFutureList waitTO " + cumulativeReadWaitTimeout
                            + " execTO " + (cumulativeReadWaitTimeout + cumulativeReadTimeout));
                    futureWatcher.startWatch(new TimeUnitWrapper(cumulativeReadWaitTimeout, TimeUnit.SECONDS),
                            new TimeUnitWrapper(cumulativeReadWaitTimeout + cumulativeReadTimeout,
                                    LogTimeoutPolicy.INSTANCE.getDefaultTimeUnit()));
                    return returnFuture;
                }
                log.error("reloadFoldersListFutures Timeout trying to get write lock");
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
            }
            log.error("getLogAggregate Timeout trying to get read lock");
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

    private ILogFileAggregate getLogAggregateRep(LogFileTypeKey type) {
        ILogFileAggregate logAggr;
        if (logAggregateMap.containsKey(type)) {
            logAggr = logAggregateMap.get(type);
        } else {
            logAggr = new LogFileAggregateImpl(type, logControllerLock.readLock(), aggrSizeChangeExecutor);
            logAggregateMap.put(type, logAggr);
        }
        return logAggr;
    }

    public Boolean isLogFilesAvailable(Set<LogFileTypeKey> typesList) {
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
            Collection<ILogFileAggregate> allAggr = logAggregateMap.values();
            for (ILogFileAggregate lfag : allAggr) {
                lfag.removeFolder(logFilesDir);
            }
            controllerLogFolders.remove(logFilesDir);
        } else {
            log.error("removeOneFolder Folder not in the list " + logFilesDir);
        }
    }

    public List<String> getAllFolders() {
        return new ArrayList<>(controllerLogFolders);
    }

    public List<LogFileAggregateInfo> getInfos(TimeUnitWrapper timeout) {
        boolean lockObtained = false;
        try {
            if (logControllerLock.readLock().tryLock()
                    || logControllerLock.readLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                List<LogFileAggregateInfo> resultList = new ArrayList<>();
                Set<LogFileTypeKey> allTypes
                    = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(true));
                for (final LogFileTypeKey currType : allTypes) {
                    ILogFileAggregate logAggr = getLogAggregate(currType);
                    resultList.add(logAggr.getInfo());
                }
                return resultList;
            }
            log.error("getInfos Timeout trying to get read lock");
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

}
