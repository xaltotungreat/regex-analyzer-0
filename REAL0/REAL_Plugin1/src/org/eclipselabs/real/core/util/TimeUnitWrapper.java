package org.eclipselabs.real.core.util;

import java.util.concurrent.TimeUnit;

public class TimeUnitWrapper {

    protected Long timeout;
    protected TimeUnit timeUnit;
    public TimeUnitWrapper(Long to, TimeUnit tu) {
        timeout = to;
        timeUnit = tu;
    }
    public Long getTimeout() {
        return timeout;
    }
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
    @Override
    public String toString() {
        return "timeout=" + timeout + ", timeUnit=" + timeUnit;
    }

}
