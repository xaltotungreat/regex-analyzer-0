package org.eclipselabs.real.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    protected AtomicInteger threadCounter = new AtomicInteger(0);
    protected String threadName;
    
    public NamedThreadFactory(String aName) {
        threadName = aName;
    }
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, threadName + "-" + threadCounter.incrementAndGet());
    }
    public String getThreadName() {
        return threadName;
    }
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

}
