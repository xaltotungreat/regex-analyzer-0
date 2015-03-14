package org.eclipselabs.real.core.event.logfile;

import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.logtype.LogFileTypeState;

public class LogFileTypeStateChangedEvent extends LogFileEventImpl {

    protected LogFileTypeKey logFileTypeKey;
    protected LogFileTypeState oldState;
    protected LogFileTypeState newState;

    public LogFileTypeStateChangedEvent(LogFileTypeKey key, LogFileTypeState aOldState, LogFileTypeState aNewState) {
        logFileTypeKey = key;
        oldState = aOldState;
        newState = aNewState;
    }

    public LogFileTypeKey getLogFileTypeKey() {
        return logFileTypeKey;
    }

    public void setLogFileTypeKey(LogFileTypeKey typeKey) {
        this.logFileTypeKey = typeKey;
    }

    public LogFileTypeState getOldState() {
        return oldState;
    }

    public void setOldState(LogFileTypeState oldState) {
        this.oldState = oldState;
    }

    public LogFileTypeState getNewState() {
        return newState;
    }

    public void setNewState(LogFileTypeState newState) {
        this.newState = newState;
    }

    @Override
    public String toString() {
        return "LogFileTypeStateChangedEvent [logFileTypeKey=" + logFileTypeKey.getLogTypeName() + ", oldState=" + oldState + ", newState=" + newState + "]";
    }

}
