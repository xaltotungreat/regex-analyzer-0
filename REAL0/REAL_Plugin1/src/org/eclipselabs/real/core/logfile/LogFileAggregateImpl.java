package org.eclipselabs.real.core.logfile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.core.event.logfile.LogFileTypeStateChangedEvent;
import org.eclipselabs.real.core.logtype.LogFileType;
import org.eclipselabs.real.core.logtype.LogFileTypeState;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.util.KeyedObjectRepositoryImpl;
import org.eclipselabs.real.core.util.LockUtil;
import org.eclipselabs.real.core.util.LockWrapper;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.core.util.RepositorySizeChangedEvent;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;

/**
 * This class represents an abstraction - a group of log files from one application component.
 * For example if Comp1 generates log4j logs they are usually divided into files and there is
 * a limit for the max size of one file. LogFileAggregateImpl allows the core components
 * to work with a group of files as with one file. For example submit search requests or
 * read all log files for Comp1 from the given folder.
 * @author Vadim Korkin
 *
 */
class LogFileAggregateImpl extends KeyedObjectRepositoryImpl<String, ILogFileRead, ILogFile> implements ILogFileAggregate {
    private static final Logger log = LogManager.getLogger(LogFileAggregateImpl.class);

    protected MultiThreadingState readFilesState = MultiThreadingState.ALLOW_MULTITHREADING_READ;

    // performance property keys
    private static final String PERF_CONST_AGGREGATE_SIZE_LIMIT = "org.eclipselabs.real.core.logfile.AggregateSizeLimit";

    protected LogFileTypeKey lfTypeKey;
    // this is the default file size limit
    protected Double aggregateSizeLimit = (double)51;

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
    public LogFileAggregateImpl(LogFileTypeKey aType, Lock contrRLock, Executor sizeChangeExec) {
        repositoryEventBus = new AsyncEventBus(sizeChangeExec);
        repositoryEventBus.register(this);
        lfTypeKey = aType;
        contrReadLock = contrRLock;
        aggregateSizeLimit = PerformanceUtils.getDoubleProperty(PERF_CONST_AGGREGATE_SIZE_LIMIT, aggregateSizeLimit);
    }

    @Override
    public void removeFolder(String filesDir) {
        log.info("Removing folder type=" + getType().getLogTypeName() + " dir=" + filesDir);
        List<ILogFileRead> allLF = getAllValues();
        for (ILogFileRead currLF : allLF) {
            if (currLF.getFilePath().startsWith(filesDir)) {
                log.debug("Removing logFile path=" + currLF.getFilePath());
                remove(currLF.getFilePath());
            }
        }
    }

    @Override
    public void removeFolders(Set<String> filesDirs) {
        List<ILogFileRead> allLF = getAllValues();
        for (String currFolder : filesDirs) {
            log.info("Removing folder type=" + getType().getLogTypeName() + " dir=" + currFolder);
            for (ILogFileRead currLF : allLF) {
                if (currLF.getFilePath().startsWith(currFolder)) {
                    log.debug("Removing logFile path=" + currLF.getFilePath());
                    remove(currLF.getFilePath());
                }
            }
        }
    }

    @Override
    public Double getAggregateFilesSize(List<? extends ILogFileRead> logFiles) {
        double result = 0;
        for (ILogFileRead lf : logFiles) {
            result += lf.getFileSize();
        }
        return result;
    }

    @Override
    public LogFileTypeKey getType() {
        return lfTypeKey;
    }

    @Override
    public String toString() {
        return "LogFileAggregateImpl [lfTypeKey=" + lfTypeKey + ", \n\t LogFiles=" + getAllValues() + "]";
    }

    @Override
    public MultiThreadingState getReadFilesState() {
        return readFilesState;
    }

    @Override
    public LogFileAggregateInfo getInfo() {
        LogFileAggregateInfo info = new LogFileAggregateInfo(getType());
        List<ILogFileRead> allFiles = getAllValues();
        for (ILogFileRead lf : allFiles) {
            info.addLogFileInfo(lf.getInfo());
        }
        return info;
    }

    @Override
    public void setReadFilesState(MultiThreadingState newState) {
        readFilesState = newState;
    }

    @Override
    public Double getAggregateSizeLimit() {
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

    @Override
    public List<LockWrapper> getLocksForOperation(LogOperationType lot) {
        List<LockWrapper> lks = new ArrayList<>();
        switch(lot) {
        // other cases will be handled later
        case LOG_FILE_READ:
            lks.add(LockUtil.getWrapper(getWriteLock(), "Aggregate write lock"));
            break;
        case SEARCH:
            lks.add(LockUtil.getWrapper(contrReadLock, "Controller read lock"));
            lks.add(LockUtil.getWrapper(getReadLock(), "Aggregate read lock"));
            break;
        default:
            // no locks for other operation types
            break;
        }
        return lks;
    }

    @Override
    public ILogFile createLogFile(File fl) {
        ILogFile newLog = new LogFile8Impl(this, fl);
        this.add(fl.getAbsolutePath(), newLog);
        return newLog;
    }

    @Override
    public void cleanAllFiles() {
        List<ILogFile> allLogFiles = this.getAllValuesFull();
        log.info("Cleaning all files type=" + getType().getLogTypeName());
        for (ILogFile currLogFile : allLogFiles) {
            currLogFile.cleanFile();
            LogFileInfo currRes = new LogFileInfo();
            currRes.setFileFullName(currLogFile.getFilePath());
            currRes.setFileSize(currLogFile.getFileSize().doubleValue() / (1024 * 1024));
            currRes.setInMemory(false);
            currRes.setLastReadSuccessful(null);
        }
    }
}
