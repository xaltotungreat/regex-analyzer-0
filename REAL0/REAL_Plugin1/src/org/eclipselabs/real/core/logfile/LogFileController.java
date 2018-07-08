package org.eclipselabs.real.core.logfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.distrib.GenericError;
import org.eclipselabs.real.core.distrib.IDistribRoot;
import org.eclipselabs.real.core.dlog.DAccumulatorReloadFolders;
import org.eclipselabs.real.core.dlog.DBuilderReloadFolders;
import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.core.event.logfile.ControllerFolderListUpdated;
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.util.LockWrapper;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public enum LogFileController {

    INSTANCE;

    private static final Logger log = LogManager.getLogger(LogFileController.class);

    private volatile Set<String> controllerLogFolders = Collections.synchronizedSet(new HashSet<String>());
    private ExecutorService aggrSizeChangeExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("AggregateSizeChange"));
    private LogAggregateRepositoryImpl aggregateRepository = new LogAggregateRepositoryImpl();

    public static class ReloadFoldersResult {
        private DAccumulatorReloadFolders accumulator;
        private CompletableFuture<DAccumulatorReloadFolders> future;

        public ReloadFoldersResult() {}

        public ReloadFoldersResult(DAccumulatorReloadFolders accum, CompletableFuture<DAccumulatorReloadFolders> ft) {
            accumulator = accum;
            future = ft;
        }

        public DAccumulatorReloadFolders getAccumulator() {
            return accumulator;
        }

        public void setAccumulator(DAccumulatorReloadFolders accumulator) {
            this.accumulator = accumulator;
        }

        public CompletableFuture<DAccumulatorReloadFolders> getFuture() {
            return future;
        }

        public void setFuture(CompletableFuture<DAccumulatorReloadFolders> future) {
            this.future = future;
        }
    }

    /**
     * This method returns a read-only view of the underlying repository
     * Only get methods are available
     * @return a read-only view of the underlying repository
     */
    public ILogAggregateRepositoryRead getRepository() {
        return aggregateRepository;
    }

    /**
     * This method returns a read-only view of the underlying log aggregate
     * Only get methods are available
     * @param lftKey the key of the log file type
     * @return the read-only view of the corresponding log aggregate
     */
    public ILogFileAggregateRead getLogAggregate(LogFileTypeKey lftKey) {
        return aggregateRepository.get(lftKey);
    }

    /**
     * This methods adds the new folder to the current list of folders and refreshes ALL the folders
     * @param fld - the new folder to add to the controller
     * @return a holder object containing the accumulator to watch the progress and the future
     */
    public ReloadFoldersResult addFolder(String fld) {
        return addFolders(Collections.singleton(fld));
    }

    /**
     * This methods adds the new folders to the current list of folders and refreshes ALL the folders
     * @param fld
     * @param fld - the new folder to add to the controller
     * @return a holder object containing the accumulator to watch the progress and the future
     */
    public ReloadFoldersResult addFolders(Set<String> fld) {
        controllerLogFolders.addAll(fld);
        return reloadCurrentFolders();
    }

    /**
     * This methods adds the new folders to the current list of folders and refreshes ALL the folders
     * @return a holder object containing the accumulator to watch the progress and the future
     */
    public synchronized ReloadFoldersResult reloadCurrentFolders() {
        checkLogAggregates(true);
        clearLogAggregates();
        DBuilderReloadFolders builder = new DBuilderReloadFolders(controllerLogFolders, aggregateRepository);
        IDistribRoot<LogFileAggregateInfo, DAccumulatorReloadFolders, List<LogFileAggregateInfo>, GenericError> root = builder.build(2);
        CompletableFuture<DAccumulatorReloadFolders> ft = root.execute();
        return new ReloadFoldersResult(root.getAccumulator(), ft);
    }

    /**
     * This method verifies that all log aggregates exist prior to any folder operation
     */
    private void checkLogAggregates(boolean removeDisabled) {
        boolean lockObtained = false;
        TimeUnitWrapper timeout = LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.CONTROLLER_RELOAD_FOLDERS_WAIT, null);
        try {
            if (aggregateRepository.getWriteLock().tryLock()
                    || aggregateRepository.getWriteLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                Set<LogFileTypeKey> allEnabledTypes = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(true));
                for (LogFileTypeKey currType : allEnabledTypes) {
                    getOrCreateLogAggregateFull(currType, true);
                }
                if (removeDisabled) {
                    Set<LogFileTypeKey> allDisabledTypes = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(false));
                    for (LogFileTypeKey currType : allDisabledTypes) {
                        aggregateRepository.remove(currType);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("Exception trying to remove folders", e);
        } finally {
            if (lockObtained) {
                aggregateRepository.getWriteLock().unlock();
            } else {
                log.error("checkLogAggregates write lock was not obtained");
            }
        }
    }

    /**
     * This method removes all log files inside the log aggregates
     */
    private void clearLogAggregates() {
        aggregateRepository.getAllValuesFull().stream().forEach(ILogFileAggregate::removeAll);
    }

    public List<LockWrapper> getLocksForOperation(LogOperationType tp) {
        List<LockWrapper> locks = new ArrayList<>();
        switch (tp) {
        case CONTROLLER_RELOAD_FOLDERS:
            locks.add(new LockWrapper(aggregateRepository.getWriteLock(), "Aggregate rep write lock"));
            break;

        default:
            break;
        }
        return locks;
    }

    private ILogFileAggregate getOrCreateLogAggregateFull(LogFileTypeKey lftKey, boolean createIfNotFound) {
        ILogFileAggregate aggr = aggregateRepository.getFull(lftKey);
        if ((aggr == null) && createIfNotFound) {
            aggr = new LogFileAggregateImpl(lftKey, aggregateRepository.getReadLock(), aggrSizeChangeExecutor);
            aggregateRepository.add(lftKey, aggr);
        }
        return aggr;
    }

    public Boolean isLogFilesAvailable(Set<LogFileTypeKey> typesList) {
        Boolean available = true;
        if (typesList != null) {
            for (LogFileTypeKey lftKey : typesList) {
                if (lftKey != null) {
                    ILogFileAggregateRead currAggr = getLogAggregate(lftKey);
                    if ((currAggr == null) || (currAggr.isEmpty())) {
                        available = false;
                        break;
                    }
                }
            }
        }
        return available;
    }

    /**
     * This method returns the set of current log folders in the controller
     * Remember the returned Set is unmodifiable
     * @return an unmodifiable Set of the log folders in the controller
     */
    public Set<String> getAllFolders() {
        return Collections.unmodifiableSet(controllerLogFolders);
    }

    public List<LogFileAggregateInfo> getAggregateInfos(TimeUnitWrapper timeout) {
        boolean lockObtained = false;
        try {
            if (aggregateRepository.getReadLock().tryLock()
                    || aggregateRepository.getReadLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                List<LogFileAggregateInfo> resultList = new ArrayList<>();
                Set<LogFileTypeKey> allTypes
                    = LogFileTypes.INSTANCE.getAllTypeKeys(new LogFileType.LFTEnabledStatePredicate(true));
                for (final LogFileTypeKey currType : allTypes) {
                    ILogFileAggregateRead logAggr = getLogAggregate(currType);
                    resultList.add(logAggr.getInfo());
                }
                return resultList;
            }
            log.error("getInfos Timeout trying to get read lock");
        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
        } finally {
            if (lockObtained) {
                aggregateRepository.getReadLock().unlock();
            } else {
                log.error("removeFolders read lock was not obtained");
            }
        }
        return null;
    }

    public List<LogFileAggregateInfo> getAggregateInfos() {
        return getAggregateInfos(LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.CONTROLLER_READ_WAIT, null));
    }

    public void removeFolders(List<String> logFilesDir, TimeUnitWrapper timeout) {
        boolean lockObtained = false;
        try {
            if (aggregateRepository.getWriteLock().tryLock()
                    || aggregateRepository.getWriteLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                lockObtained = true;
                for (String currFolder : logFilesDir) {
                    removeFolder(currFolder, timeout);
                }
            }
        } catch (InterruptedException e) {
            log.error("Exception trying to remove folders", e);
        } finally {
            if (lockObtained) {
                aggregateRepository.getWriteLock().unlock();
            } else {
                log.error("removeFolders write lock was not obtained");
            }
        }
    }

    public void removeFolder(String logFilesDir) {
        removeFolder(logFilesDir,
                LogTimeoutPolicy.INSTANCE.getOperationTimeout(LogOperationType.CONTROLLER_REMOVE_FOLDERS_WAIT, null));
    }

    public void removeFolder(String logFilesDir, TimeUnitWrapper timeout) {
        if (controllerLogFolders.contains(logFilesDir)) {
            boolean lockObtained = false;
            try {
                if (aggregateRepository.getWriteLock().tryLock()
                        || aggregateRepository.getWriteLock().tryLock(timeout.getTimeout(), timeout.getTimeUnit())) {
                    lockObtained = true;
                    List<String> oldFolders = new ArrayList<>(controllerLogFolders);
                    removeOneFolder(logFilesDir);
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
                if (lockObtained) {
                    aggregateRepository.getWriteLock().unlock();
                }
            }
        }
    }

    protected void removeManyFolders(List<String> logFilesDir) {
        for (String currFolder : logFilesDir) {
            removeOneFolder(currFolder);
        }
    }

    protected void removeOneFolder(String logFilesDir) {
        if (controllerLogFolders.contains(logFilesDir)) {
            List<ILogFileAggregate> allAggr = aggregateRepository.getAllValuesFull();
            for (ILogFileAggregate lfag : allAggr) {
                lfag.removeFolder(logFilesDir);
            }
            controllerLogFolders.remove(logFilesDir);
        } else {
            log.error("removeOneFolder Folder not in the list " + logFilesDir);
        }
    }
}
