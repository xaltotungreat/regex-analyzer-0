package org.eclipselabs.real.core.logfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogFile6Impl implements ILogFile {

    private static final Logger log = LogManager.getLogger(LogFile6Impl.class);
    protected String logText;
    protected char[] logFileContents;
    protected File theLogFile;
    protected LogFileState theState;
    protected ILogFileAggregate theAggregate;
    protected Boolean isZip = false;

    public LogFile6Impl(ILogFileAggregate aggr, String fp) {
        theLogFile = new File(fp);
        if (fp.endsWith(".zip")) {
            isZip = true;
        }
        theAggregate = aggr;
        theState = LogFileState.FILE_NOT_READ;
    }

    public LogFile6Impl(ILogFileAggregate aggr, File lf) {
        theLogFile = lf;
        if (theLogFile.getAbsolutePath().endsWith(".zip")) {
            isZip = true;
        }
        theState = LogFileState.FILE_NOT_READ;
        theAggregate = aggr;
    }
    @Override
    public LogFileInfo readFile() {
        log.debug("Opening file " + theLogFile.getAbsolutePath());
        LogFileInfo currReadResult = new LogFileInfo();
        currReadResult.setFileFullName(getFilePath());
        currReadResult.setLastReadSuccessful(false);
        theState = LogFileState.FILE_READING;
        if (!isZip) {
            try (FileInputStream fis = new FileInputStream(theLogFile);
                    InputStreamReader isr = new InputStreamReader(new FileInputStream(theLogFile))) {
                logFileContents = new char[fis.available()];
                int currCharsRead = 0;
                int totalCharsRead = 0;
                do {
                    totalCharsRead += currCharsRead;
                    currCharsRead = isr.read(logFileContents, totalCharsRead, logFileContents.length - totalCharsRead);
                } while (currCharsRead > 0);
                currReadResult.setLastReadSuccessful(true);
                currReadResult.setInMemory(true);
                currReadResult.setFileSize(((double)logFileContents.length)/(1024*1024));
                theState = LogFileState.FILE_READ;
            } catch (FileNotFoundException e1) {
                log.error("readFile ", e1);
                currReadResult.setLastReadException(e1);
            } catch (IOException e) {
                log.error("readFile ", e);
                currReadResult.setLastReadException(e);
            }
        } else {
            try (ZipFile thisZF = new ZipFile(theLogFile)){
                log.debug("Processing zip file name=" + thisZF.getName());
                Enumeration<? extends ZipEntry> allZipEntries = thisZF.entries();
                List<char[]> allFiles = new ArrayList<char[]>();
                int allFilesSize = 0;
                while (allZipEntries.hasMoreElements()) {
                    ZipEntry currZipEntry = allZipEntries.nextElement();
                    log.debug("Processing zip entry name=" + currZipEntry.getName());
                    if (!currZipEntry.isDirectory()) {
                        try(InputStream currIS = thisZF.getInputStream(currZipEntry);
                                InputStreamReader isr = new InputStreamReader(currIS)) {
                            char[] currBytes = new char[currIS.available()];
                            allFilesSize += currIS.available();
                            int currCharsRead = 0;
                            int totalCharsRead = 0;
                            do {
                                totalCharsRead += currCharsRead;
                                currCharsRead = isr.read(currBytes, totalCharsRead, currBytes.length - totalCharsRead);
                            } while (currCharsRead > 0);
                            log.debug("ZIP entry size read " + currBytes.length);
                            allFiles.add(currBytes);
                        }
                    }
                }
                if (allFilesSize > 0) {
                    logFileContents = new char[allFilesSize];
                    int currPos = 0;
                    for (char[] bs : allFiles) {
                        System.arraycopy(bs, 0, logFileContents, currPos, bs.length);
                        currPos += bs.length;
                    }
                    currReadResult.setLastReadSuccessful(true);
                    currReadResult.setInMemory(true);
                    currReadResult.setFileSize(((double)logFileContents.length)/(1024*1024));
                    theState = LogFileState.FILE_READ;
                } else {
                    log.error("No log files within the zip archive");
                }
            } catch (ZipException e) {
                log.error("readFile ", e);
                currReadResult.setLastReadException(e);
            } catch (IOException e) {
                log.error("readFile ", e);
                currReadResult.setLastReadException(e);
            }
        }
        return currReadResult;
    }

    @Override
    public synchronized String getFileText(boolean cleanCharArray) {
        if ((logText == null) && (logFileContents != null)) {
            logText = new String(logFileContents);
            if (cleanCharArray) {
                logFileContents = null;
                System.gc();
            }
        }
        return logText;
    }

    @Override
    public String getFilePath() {
        return theLogFile.getAbsolutePath();
    }

    @Override
    public Long getFileSize() {
        Long fz = (long)0;
        if (!isZip) {
            fz = theLogFile.length();
        } else {
            try (ZipFile thisZF = new ZipFile(theLogFile)) {
                Enumeration<? extends ZipEntry> allZipEntries = thisZF.entries();
                while (allZipEntries.hasMoreElements()) {
                    ZipEntry currZipEntry = allZipEntries.nextElement();
                    fz += currZipEntry.getSize();
                }
            } catch (ZipException e) {
                log.error("Zip Error " + getFilePath(),e);
            } catch (IOException e) {
                log.error("Zip Error " + getFilePath(),e);
            }
        }
        return fz;
    }

    @Override
    public String toString() {
        return "LogFile6Impl [theLogFilePath=" + theLogFile.getAbsolutePath() + "]";
    }

    @Override
    public Boolean isRead() {
        return (LogFileState.FILE_READ == theState);
    }

    @Override
    public synchronized void cleanFile() {
        if (LogFileState.FILE_READ == theState) {
            logFileContents = null;
            logText = null;
        }
        theState = LogFileState.FILE_NOT_READ;
    }

    @Override
    public LogFileState getState() {
        return theState;
    }

    @Override
    public ILogFileAggregate getAggregate() {
        return theAggregate;
    }

    @Override
    public ReentrantLock getReadLock() {
        synchronized(theAggregate) {
            return theAggregate.getReadFileLock();
        }
    }

    @Override
    public LogFileInfo getInfo() {
        LogFileInfo info = new LogFileInfo();
        info.setFileFullName(getFilePath());
        info.setFileSize(getFileSize().doubleValue()/(1024*1024));
        info.setInMemory((getState() == LogFileState.FILE_READ)?true:false);
        return info;
    }

}
