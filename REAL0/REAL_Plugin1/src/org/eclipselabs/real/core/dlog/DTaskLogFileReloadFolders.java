package org.eclipselabs.real.core.dlog;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.distrib.DistribFactoryMain;
import org.eclipselabs.real.core.distrib.IDistribTask;
import org.eclipselabs.real.core.distrib.IDistribTaskResultWrapper;
import org.eclipselabs.real.core.logfile.ILogFile;
import org.eclipselabs.real.core.logfile.ILogFileAggregate;
import org.eclipselabs.real.core.logfile.ILogFileAggregate.MultiThreadingState;
import org.eclipselabs.real.core.logfile.ILogFileRead;
import org.eclipselabs.real.core.logfile.LogFileAggregateInfo;
import org.eclipselabs.real.core.logfile.LogFileInfo;
import org.eclipselabs.real.core.logtype.LogFileTypes;

public class DTaskLogFileReloadFolders implements IDistribTask<LogFileAggregateInfo> {


    private static final Logger log = LogManager.getLogger(DTaskLogFileReloadFolders.class);
    private Set<String> folders;
    private ILogFileAggregate aggregate;

    public DTaskLogFileReloadFolders(Set<String> aFolders, ILogFileAggregate aggr) {
        folders = aFolders;
        aggregate = aggr;
    }

    @Override
    public IDistribTaskResultWrapper<LogFileAggregateInfo> get() {
        IDistribTaskResultWrapper<LogFileAggregateInfo> result = DistribFactoryMain.INSTANCE.getDefaultFactory().getTaskResult();
        if ((folders != null) && (!folders.isEmpty())) {
            LogFileAggregateInfo aggrResult = new LogFileAggregateInfo(aggregate.getType());
            for (String currFolder : folders) {
                processFolder(currFolder, aggrResult);
            }
            result.setActualResult(aggrResult);
        } else {
            log.error("No folders to reload return null");
        }
        return result;
    }

    protected void processFolder(String folder, LogFileAggregateInfo aggrResult) {
        log.debug("Processing  folder=" + folder + " type=" + aggregate.getType());
        Set<String> allPatterns = LogFileTypes.INSTANCE.getPatterns(aggregate.getType());
        List<ILogFile> newFiles = addLogFilesForFolder(folder, allPatterns);
        log.debug("Found all files size=" + newFiles.size() + " type=" + aggregate.getType());
        if (!newFiles.isEmpty()) {
            Double newAggrFileSize = aggregate.getAggregateFilesSize(aggregate.getAllValues());
            // the aggregate size in the configuration is in megabytes
            if (newAggrFileSize / (1024 * 1024) > aggregate.getAggregateSizeLimit()) {
                log.info(aggregate.getType() + "The aggregate file size is bigger than the limit, will read for search aggrSize=" + newAggrFileSize);
                aggregate.setReadFilesState(MultiThreadingState.DISALLOW_MULTITHREADING_READ);
                aggregate.cleanAllFiles();
                aggrResult.addAllLogFileInfo(aggregate.getAllValues().stream().map(ILogFileRead::getInfo).collect(Collectors.toList()));
            } else {
                newFiles.stream().forEach(a -> aggrResult.addLogFileInfo(a.readFile()));
            }
        } else {
            log.info("No log files found of type " + aggregate.getType() + " in folder=" + folder);
        }
    }

    protected LogFileInfo fillInInfoNotRead(ILogFile lf) {
        LogFileInfo currRes = new LogFileInfo();
        currRes.setFileFullName(lf.getFilePath());
        currRes.setFileSize(lf.getFileSize().doubleValue() / (1024 * 1024));
        currRes.setInMemory(false);
        currRes.setLastReadSuccessful(null);
        return currRes;
    }

    protected List<ILogFile> addLogFilesForFolder(String folder, Set<String> filePatterns) {
        List<ILogFile> newFiles = new ArrayList<>();
        if (filePatterns != null) {
            for (String currPattern : filePatterns) {
                Pattern logFilePattern = Pattern.compile(currPattern);
                File newDir = new File(folder);
                List<File> allFilesList = new ArrayList<>();
                getFilesWithSubfolders(newDir, allFilesList, logFilePattern);
                if (!allFilesList.isEmpty()) {
                    for (File file : allFilesList) {
                        if (aggregate.get(file.getAbsolutePath()) == null) {
                            ILogFile newLog = aggregate.createLogFile(file);
                            newFiles.add(newLog);
                        }
                    }
                }
            }
        }
        return newFiles;
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
