package org.eclipselabs.real.core.logfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LogFileAggregateInfo {

    LogFileTypeKey logAggregateType;
    List<LogFileInfo> logFileInfos = new ArrayList<LogFileInfo>();
    
    public LogFileAggregateInfo(LogFileTypeKey type) {
        logAggregateType = type;
    }
    
    public LogFileAggregateInfo(LogFileAggregateInfo copyObj) {
        logAggregateType = copyObj.getLogAggregateType();
        logFileInfos = copyObj.getLogFileInfos();
    }

    public LogFileTypeKey getLogAggregateType() {
        return logAggregateType;
    }

    public void setLogAggregateType(LogFileTypeKey logAggregateType) {
        this.logAggregateType = logAggregateType;
    }

    public List<LogFileInfo> getLogFileInfos() {
        return logFileInfos;
    }
    
    public void addLogFileInfo(LogFileInfo logFileResult) {
        logFileInfos.add(logFileResult);
    }

    public void setLogFileInfos(List<LogFileInfo> logFileResults) {
        this.logFileInfos = logFileResults;
    }
    
    public void sortFileResults() {
        Collections.sort(logFileInfos, new Comparator<LogFileInfo>() {

            @Override
            public int compare(LogFileInfo o1, LogFileInfo o2) {
                int returnVal = 0;
                if ((o1 != null) && (o2 != null)) {
                    returnVal = o1.getFileFullName().compareTo(o2.getFileFullName());
                } else if ((o1 != null) && (o2 == null)) {
                    returnVal = 1;
                } else if ((o1 == null) && (o2 != null)) {
                    returnVal = -1;
                } else {
                    returnVal = 0;
                }
                return returnVal;
            }
        });
    }

    @Override
    public String toString() {
        return "LogFileAggregateInfo [logAggregateType=" + logAggregateType + ", logFileInfos=" + logFileInfos
                + "]";
    }

    
}
