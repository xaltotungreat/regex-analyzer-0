package org.eclipselabs.real.core.logfile.task;

import org.eclipselabs.real.core.logfile.ILogFileAggregateRep;

public abstract class AddLogFileAggregateTaskResult<V, R> implements IAddLogTaskResult<V, R> {

    ILogFileAggregateRep logFileAggregate;
    
    public AddLogFileAggregateTaskResult(ILogFileAggregateRep aggr) {
        logFileAggregate = aggr;
    }

    public ILogFileAggregateRep getLogFileAggregate() {
        return logFileAggregate;
    }

    public void setLogFileAggregate(ILogFileAggregateRep logFileAggregate) {
        this.logFileAggregate = logFileAggregate;
    }

}
