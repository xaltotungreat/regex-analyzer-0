package org.eclipselabs.real.core.logtype;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.core.event.logfile.LogFileTypeStateChangedEvent;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.util.RealPredicate;

/**
 * This class implements an abstraction - a log file type.
 * A log file type has states for example active/inactive and the file name patterns.
 * When a new folder is added it is scanned for the files that match these
 * patterns.
 * @author Vadim Korkin
 *
 */
public class LogFileType {

    protected String logTypeName;
    protected LogFileTypeState stateInfo = new LogFileTypeState(true, false);
    protected Set<String> filePatterns = Collections.synchronizedSet(new HashSet<String>());

    public static class LFTNamePredicate implements RealPredicate<LogFileType> {

        protected String testName;

        public LFTNamePredicate(String name) {
            testName = name;}

        @Override
        public boolean test(LogFileType t) {
            boolean result = false;
            if ((testName != null) && (t != null)) {
                result = testName.equals(t.getLogTypeName());
            } else if ((testName == null) && (t != null) && (t.getLogTypeName() == null)) {
                result = true;
            }
            return result;
        }
    }

    public static class LFTEnabledStatePredicate implements RealPredicate<LogFileType> {

        protected boolean testState;
        public LFTEnabledStatePredicate(boolean state) {
            testState = state;
        }

        @Override
        public boolean test(LogFileType t) {
            boolean result = false;
            if (t != null) {
                result = testState == t.isEnabled();
            }
            return result;
        }
    }

    public static class LFTAvailableStatePredicate implements RealPredicate<LogFileType> {

        protected boolean testState;
        public LFTAvailableStatePredicate(boolean state) {
            testState = state;
        }

        @Override
        public boolean test(LogFileType t) {
            boolean result = false;
            if ((t != null)) {
                result = testState == t.isAvailable();
            }
            return result;
        }
    }

    public LogFileType(String typeName) {
        this(typeName, new LogFileTypeState(true, false));
    }

    public LogFileType(String typeName, LogFileTypeState stateInf) {
        logTypeName = typeName;
        stateInfo = stateInf;
    }

    public String getLogTypeName() {
        return logTypeName;
    }

    public Set<String> getFilePatterns() {
        return filePatterns;
    }

    public void setFilePatterns(Set<String> filePatterns) {
        this.filePatterns = filePatterns;
    }

    public void addPattern(String newPattern) {
        filePatterns.add(newPattern);
    }

    /*public void setState(LogFileTypeState state) {
        LogFileTypeState oldState = this.state;
        this.state = state;
        LogFileTypeState newState = this.state;
        if (((newState != null) && (!newState.equals(oldState)))
                || ((oldState != null) && (!oldState.equals(newState)))) {
            LogFileTypeStateChangedEvent newEvent = new LogFileTypeStateChangedEvent(
                    new LogFileTypeKey(getLogTypeName()), oldState, newState);
            CoreEventBus.INSTANCE.postAsync(newEvent);
        }
    }*/

    public LogFileTypeState getStateInfoCopy() {
        return new LogFileTypeState(stateInfo.isEnabled(), stateInfo.isAvailable());
    }

    public void setStateInfo(LogFileTypeState stateInfo) {
        this.stateInfo = stateInfo;
    }

    public boolean isEnabled() {
        return stateInfo.isEnabled();
    }

    public void setEnabled(boolean enb) {
        LogFileTypeState oldState = getStateInfoCopy();
        stateInfo.setEnabled(enb);
        LogFileTypeState newState = getStateInfoCopy();
        if (oldState != newState) {
            LogFileTypeStateChangedEvent newEvent = new LogFileTypeStateChangedEvent(
                    new LogFileTypeKey(getLogTypeName()), oldState, newState);
            CoreEventBus.INSTANCE.postSingleThreadAsync(newEvent);
        }
    }

    public boolean isAvailable() {
        return stateInfo.isAvailable();
    }

    public void setAvailable(boolean avail) {
        stateInfo.setAvailable(avail);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logTypeName == null) ? 0 : logTypeName.hashCode());
        return result;
    }

    /**
     * One type is equal to another type if their names are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogFileType other = (LogFileType) obj;
        if (logTypeName == null) {
            if (other.logTypeName != null)
                return false;
        } else if (!logTypeName.equals(other.logTypeName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogFileType [logTypeName=" + logTypeName + "]";
    }

}
