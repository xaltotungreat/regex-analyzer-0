package org.eclipselabs.real.core.logfile;

import org.eclipselabs.real.core.util.IKeyedObjectRepositoryWrite;

public interface ILogAggregateRepository extends ILogAggregateRepositoryRead, IKeyedObjectRepositoryWrite<LogFileTypeKey, ILogFileAggregate> {

}
