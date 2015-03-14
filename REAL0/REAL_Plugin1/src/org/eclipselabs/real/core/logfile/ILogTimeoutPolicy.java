package org.eclipselabs.real.core.logfile;

import java.util.concurrent.TimeUnit;

public interface ILogTimeoutPolicy {
    public static Long DEFAULT_READ_TIMEOUT = (long)90;
    
    public static Long DEFAULT_SEARCH_TIMEOUT = (long)90;
    
    public static TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;

}
