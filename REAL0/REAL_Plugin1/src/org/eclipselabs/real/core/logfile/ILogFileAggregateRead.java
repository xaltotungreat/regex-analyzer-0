package org.eclipselabs.real.core.logfile;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipselabs.real.core.logfile.ILogFileAggregate.MultiThreadingState;
import org.eclipselabs.real.core.util.IKeyedObjectRepositoryRead;
import org.eclipselabs.real.core.util.ITypedObject;
import org.eclipselabs.real.core.util.LockWrapper;

public interface ILogFileAggregateRead extends IKeyedObjectRepositoryRead<String, ILogFileRead>, ITypedObject<LogFileTypeKey> {

    // performance property keys
    String PERF_CONST_SEARCH_THREADS = "org.eclipselabs.real.core.logfile.SearchThreadsNumberPerAggregate";

    public ReentrantLock getReadFileLock();
    public MultiThreadingState getReadFilesState();

    public LogFileAggregateInfo getInfo();
    public Double getAggregateFilesSize(List<? extends ILogFileRead> logFiles);

    public List<LockWrapper> getLocksForOperation(LogOperationType lot);

}
