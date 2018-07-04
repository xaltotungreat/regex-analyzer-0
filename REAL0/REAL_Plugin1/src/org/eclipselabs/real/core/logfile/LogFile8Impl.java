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
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogFile8Impl implements ILogFile {

    private static final Logger log = LogManager.getLogger(LogFile8Impl.class);

    protected String logText;
    protected char[] logFileContents;
    protected File fileRef;
    protected LogFileState state;
    protected ILogFileAggregate parentAggregate;
    protected Boolean fileZip = false;

    public LogFile8Impl(ILogFileAggregate aggr, String fp) {
        fileRef = new File(fp);
        if (fp.endsWith(".zip")) {
            fileZip = true;
        }
        parentAggregate = aggr;
        state = LogFileState.FILE_NOT_READ;
    }

    public LogFile8Impl(ILogFileAggregate aggr, File lf) {
        fileRef = lf;
        if (fileRef.getAbsolutePath().endsWith(".zip")) {
            fileZip = true;
        }
        parentAggregate = aggr;
        state = LogFileState.FILE_NOT_READ;
    }

    @Override
    public LogFileInfo readFile() {
        log.debug("Opening file " + fileRef.getAbsolutePath());
        LogFileInfo currReadResult = new LogFileInfo();
        currReadResult.setFileFullName(getFilePath());
        currReadResult.setLastReadSuccessful(false);
        state = LogFileState.FILE_READING;
        if (!fileZip) {
            try (FileInputStream fis = new FileInputStream(fileRef);
                    InputStreamReader isr = new InputStreamReader(new FileInputStream(fileRef))) {
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
                state = LogFileState.FILE_READ;
            } catch (FileNotFoundException e1) {
                log.error("readFile ", e1);
                currReadResult.setLastReadException(e1);
            } catch (IOException e) {
                log.error("readFile ", e);
                currReadResult.setLastReadException(e);
            }
        } else {
            try (ZipFile thisZF = new ZipFile(fileRef)){
                log.debug("Processing zip file name=" + thisZF.getName());
                Enumeration<? extends ZipEntry> allZipEntries = thisZF.entries();
                List<char[]> allFiles = new ArrayList<>();
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
                    state = LogFileState.FILE_READ;
                } else {
                    log.error("No log files within the zip archive");
                }
            } catch (IOException e) {
                log.error("readFile ", e);
                currReadResult.setLastReadException(e);
            }
        }
        return currReadResult;
    }

    @Override
    public String getFileText(boolean cleanCharArray) {
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
        return fileRef.getAbsolutePath();
    }

    @Override
    public Long getFileSize() {
        Long fz = (long)0;
        if (!fileZip) {
            fz = fileRef.length();
        } else {
            try (ZipFile thisZF = new ZipFile(fileRef)) {
                if (thisZF.entries().hasMoreElements()) {
                    fz = thisZF.stream().mapToLong(ZipEntry::getSize).sum();
                }
            } catch (IOException e) {
                log.error("Zip Error " + getFilePath(),e);
            }
        }
        return fz;
    }

    @Override
    public Boolean isRead() {
        return (LogFileState.FILE_READ == state);
    }

    @Override
    public synchronized void cleanFile() {
        if (LogFileState.FILE_READ == state) {
            logFileContents = null;
            logText = null;
        }
        state = LogFileState.FILE_NOT_READ;
    }

    @Override
    public ReentrantLock getReadLock() {
        synchronized(parentAggregate) {
            return parentAggregate.getReadFileLock();
        }
    }

    @Override
    public LogFileState getState() {
        return state;
    }

    @Override
    public ILogFileAggregate getAggregate() {
        return parentAggregate;
    }

    @Override
    public LogFileInfo getInfo() {
        LogFileInfo info = new LogFileInfo();
        info.setFileFullName(getFilePath());
        info.setFileSize(getFileSize().doubleValue()/(1024*1024));
        info.setInMemory(getState() == LogFileState.FILE_READ);
        return info;
    }

}
