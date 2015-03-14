package org.eclipselabs.real.core.logfile.task;

import org.eclipselabs.real.core.util.TimeUnitWrapper;

public abstract class LogFileTask<R,M> implements ILogFileTask<R> {

    protected IAddLogTaskResult<R,M> addTaskResult;
    protected TimeUnitWrapper theExecutionTO;
    protected TimeUnitWrapper theWaitTO;
    
    public LogFileTask(IAddLogTaskResult<R,M> resultCB, TimeUnitWrapper waitTimeout, TimeUnitWrapper execTimeout) {
        addTaskResult = resultCB;
        theWaitTO = waitTimeout;
        theExecutionTO = execTimeout;
    }

    public IAddLogTaskResult<R,M> getAddTaskResult() {
        return addTaskResult;
    }
    
    public TimeUnitWrapper getExecutionTO() {
        return theExecutionTO;
    }

    public TimeUnitWrapper getWaitTO() {
        return theWaitTO;
    }

}
