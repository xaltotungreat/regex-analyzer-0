package org.eclipselabs.real.core.logfile.task;


public interface IAddLogTaskResult<V, R> {
    public R addResult(V taskResult, R mainResult);
}
