package org.eclipselabs.real.core.logfile.task;

import org.eclipselabs.real.core.logfile.ILogFile;
import org.eclipselabs.real.core.logfile.LogFileInfo;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public class LogFileTaskRead<M> extends LogFileTask<LogFileInfo,M> {

    protected ILogFile taskLogFile;
    public LogFileTaskRead(IAddLogTaskResult<LogFileInfo, M> addCB, ILogFile logFile, TimeUnitWrapper waitTimeout, TimeUnitWrapper execTimeout) {
        super(addCB, waitTimeout, execTimeout);
        taskLogFile = logFile;
    }

    @Override
    public LogFileInfo call() throws Exception {
        return taskLogFile.readFile();
    }

    @Override
    public LogFileInfo get() {
        return taskLogFile.readFile();
    }

}
