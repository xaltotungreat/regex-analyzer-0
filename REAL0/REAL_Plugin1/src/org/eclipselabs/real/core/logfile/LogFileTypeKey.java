package org.eclipselabs.real.core.logfile;

/**
 * The log file type key is used to find the log file aggregate.
 * It doesn't contain all type information (for example file name patterns)
 * only the unique information enough to find the correct log file aggregate.
 * @author Vadim Korkin
 *
 */
public class LogFileTypeKey implements Comparable<LogFileTypeKey>, Cloneable {

    String logTypeName;

    public LogFileTypeKey(String typeName) {
        logTypeName = typeName;

    }

    public String getLogTypeName() {
        return logTypeName;
    }

    @Override
    public LogFileTypeKey clone() throws CloneNotSupportedException {
        return (LogFileTypeKey)super.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logTypeName == null) ? 0 : logTypeName.hashCode());
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
        LogFileTypeKey other = (LogFileTypeKey) obj;
        if (logTypeName == null) {
            if (other.logTypeName != null)
                return false;
        } else if (!logTypeName.equals(other.logTypeName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogFileTypeKey [logTypeName=" + logTypeName + "]";
    }

    @Override
    public int compareTo(LogFileTypeKey o) {
        if (o != null) {
            return logTypeName.compareTo(o.getLogTypeName());
        }
        return 1;
    }

}
