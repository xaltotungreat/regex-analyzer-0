package org.eclipselabs.real.core.logfile.task;

import org.eclipselabs.real.core.logfile.ILogFile;

public abstract class AddLogFileTaskResult<V, R> implements IAddLogTaskResult<V, R> {

    protected ILogFile logFile;

    public AddLogFileTaskResult(ILogFile aLF) {
        logFile = aLF;
         
    }
    
    public ILogFile getLogFile() {
        return logFile;
    }

    public void setLogFile(ILogFile logFile) {
        this.logFile = logFile;
    }


}
