package org.eclipselabs.real.core.logfile.task;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.ILogFile;
import org.eclipselabs.real.core.logfile.ILogFileAggregate.MultiThreadingState;
import org.eclipselabs.real.core.logfile.ILogFileAggregateRep;
import org.eclipselabs.real.core.logfile.LogFile6Impl;
import org.eclipselabs.real.core.logfile.LogFileAggregateInfo;
import org.eclipselabs.real.core.logfile.LogFileInfo;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public class LogFileAggregateTaskReloadFolders<M> extends LogFileTask<LogFileAggregateInfo, M> {

    private static final Logger log = LogManager.getLogger(LogFileAggregateTaskReloadFolders.class);
    List<String> folders;
    ILogFileAggregateRep aggregate;

    public LogFileAggregateTaskReloadFolders(List<String> aFolders, ILogFileAggregateRep aggr, IAddLogTaskResult<LogFileAggregateInfo, M> addCB,
            TimeUnitWrapper waitTimeout, TimeUnitWrapper execTimeout) {
        super(addCB, waitTimeout, execTimeout);
        folders = aFolders;
        aggregate = aggr;
    }

    @Override
    public LogFileAggregateInfo call() throws Exception {
        if (folders != null) {
            LogFileAggregateInfo aggrResult = new LogFileAggregateInfo(aggregate.getType());
            for (String currFolder : folders) {
                log.debug("Starting new AddFolder task type=" + aggregate.getType() + " folder=" + currFolder);
                Set<String> allPatterns = LogFileTypes.INSTANCE.getPatterns(aggregate.getType());
                List<ILogFile> newFiles = Collections.synchronizedList(new ArrayList<ILogFile>());
                if (allPatterns != null) {
                    for (String currPattern : allPatterns) {
                        Pattern logFilePattern = Pattern.compile(currPattern);
                        File newDir = new File(currFolder);
                        List<File> allFilesList = Collections.synchronizedList(new ArrayList<File>());
                        getFilesWithSubfolders(newDir, allFilesList, logFilePattern);
                        if (!allFilesList.isEmpty()) {
                            for (File file : allFilesList) {
                                if (aggregate.get(file.getAbsolutePath()) == null) {
                                    ILogFile newLog = new LogFile6Impl(aggregate, file);
                                    newFiles.add(newLog);
                                    aggregate.add(file.getAbsolutePath(), newLog);
                                }
                            }
                        }
                    }
                }
                log.debug("Found all files size=" + newFiles.size() + " type=" + aggregate.getType());
                if (!newFiles.isEmpty()) {
                    Long aggrFileSize = aggregate.getAggregateFilesSize(newFiles);
                    if (aggrFileSize / (1024 * 1024) > aggregate.getAggregateSizeLimit()) {
                        log.info(aggregate.getType() + "The aggregate file size is bigger than the limit, will read for search aggrSize=" + aggrFileSize);
                        aggregate.setReadFilesState(MultiThreadingState.DISALLOW_MULTITHREADING_READ);
                        List<ILogFile> allLogFiles = aggregate.getAllValues();
                        log.info("Cleaning all files");
                        for (ILogFile currLogFile : allLogFiles) {
                            currLogFile.cleanFile();
                            LogFileInfo currRes = new LogFileInfo();
                            currRes.setFileFullName(currLogFile.getFilePath());
                            currRes.setFileSize(currLogFile.getFileSize().doubleValue() / (1024 * 1024));
                            currRes.setInMemory(false);
                            currRes.setLastReadSuccessful(null);
                            aggrResult.addLogFileInfo(currRes);
                        }
                    } else {
                        for (ILogFile currLF : newFiles) {
                            aggrResult.addLogFileInfo(currLF.readFile());
                        }
                    }
                } else {
                    log.info("No log files found of type " + aggregate.getType() + " in folder=" + currFolder);
                }
            }
            return aggrResult;
        }
        log.error("No folders to reload return null");
        return null;
    }

    protected void getFilesWithSubfolders(File filesDir, final List<File> allFiles, final Pattern logFilePattern) {
        if (filesDir.isDirectory()) {
            File[] logFiles = filesDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean result = false;
                    if (!pathname.isDirectory()) {
                        String fileName = pathname.getName();
                        Matcher mt = logFilePattern.matcher(fileName);
                        if (mt.matches()) {
                            result = true;
                        }
                    } else {
                        getFilesWithSubfolders(pathname, allFiles, logFilePattern);
                    }
                    return result;
                }
            });
            if (logFiles != null) {
                allFiles.addAll(Arrays.asList(logFiles));
            } else {
                log.error("getFilesWithSubfolders null files array returned");
            }
        } else {
            String fileName = filesDir.getName();
            Matcher mt = logFilePattern.matcher(fileName);
            if (mt.matches()) {
                allFiles.add(filesDir);
            }
        }
    }

}
