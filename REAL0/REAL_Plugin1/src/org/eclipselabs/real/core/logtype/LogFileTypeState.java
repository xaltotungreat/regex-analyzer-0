package org.eclipselabs.real.core.logtype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"enabled","available"})
public class LogFileTypeState {

    private boolean enabled = true;
    private boolean available = false;

    public LogFileTypeState() {

    }

    public LogFileTypeState(boolean enb, boolean avail) {
        enabled = enb;
        available = avail;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (available ? 1231 : 1237);
        result = prime * result + (enabled ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogFileTypeState other = (LogFileTypeState) obj;
        if (available != other.available)
            return false;
        if (enabled != other.enabled)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogFileTypeState [enabled=" + enabled + ", available=" + available + "]";
    }
}
