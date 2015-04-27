package org.eclipselabs.real.core.logfile.task;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface ILogFileTask<R> extends Callable<R>, Supplier<R> {

}
