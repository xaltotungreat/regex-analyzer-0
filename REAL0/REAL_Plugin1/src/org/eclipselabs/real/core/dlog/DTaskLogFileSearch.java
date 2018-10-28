package org.eclipselabs.real.core.dlog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.distrib.DistribFactoryMain;
import org.eclipselabs.real.core.distrib.IDistribTask;
import org.eclipselabs.real.core.distrib.IDistribTaskResultWrapper;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.exception.IncorrectPatternExceptionRT;
import org.eclipselabs.real.core.logfile.ILogFileRead;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.ISearchResult;

public class DTaskLogFileSearch<R extends ISearchResult<?>> implements IDistribTask<R> {

    private static final Logger log = LogManager.getLogger(DTaskLogFileSearch.class);
    protected ISearchObject<R,?> searchObject;
    protected PerformSearchRequest searchRequest;
    protected ILogFileRead taskLogFile;

    public DTaskLogFileSearch(ILogFileRead logFile, ISearchObject<R,?> so, PerformSearchRequest request) {
        taskLogFile = logFile;
        searchObject = so;
        searchRequest = request;
    }

    @Override
    public IDistribTaskResultWrapper<R> get() {
        IDistribTaskResultWrapper<R> result = DistribFactoryMain.INSTANCE.getDefaultFactory().getTaskResult();
        result.putNVP("logFilePath", taskLogFile.getFilePath());
        if ((searchRequest.getProgressMonitor() != null) && (!searchRequest.getProgressMonitor().isSearchCancelled())) {
            log.debug("LogFile " + taskLogFile);
            /*
             * In this case the exception won't go up in the hierarchy because this is a separate thread
             * Throwing a RuntimeException will lead to it showing up in the handle method.
             */
            try {
                searchRequest.setText(taskLogFile.getFileText());
                result.setActualResult(searchObject.performSearch(searchRequest));
            } catch (IncorrectPatternException e) {
                log.error("Incorrect pattern exception for file " + taskLogFile.getFilePath(), e);
                throw new IncorrectPatternExceptionRT(taskLogFile.getFilePath(), e);
            }
            searchRequest.getProgressMonitor().incrementCompletedSOFiles();
        } else {
            log.debug("LogFileSearchTask canceled LogFile " + taskLogFile);
            result.putNVP("canceled", true);
        }

        return result;
    }

}
