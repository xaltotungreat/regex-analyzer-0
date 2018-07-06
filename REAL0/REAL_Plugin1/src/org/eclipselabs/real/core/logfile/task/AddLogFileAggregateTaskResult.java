package org.eclipselabs.real.core.logfile.task;

import org.eclipselabs.real.core.logfile.ILogFileAggregate;

public abstract class AddLogFileAggregateTaskResult<V, R> implements IAddLogTaskResult<V, R> {

    ILogFileAggregate logFileAggregate;

    public AddLogFileAggregateTaskResult(ILogFileAggregate aggr) {
        logFileAggregate = aggr;
    }

    public ILogFileAggregate getLogFileAggregate() {
        return logFileAggregate;
    }

    public void setLogFileAggregate(ILogFileAggregate logFileAggregate) {
        this.logFileAggregate = logFileAggregate;
    }

}
