package org.eclipselabs.real.core.logfile.task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.exception.IncorrectPatternExceptionRT;
import org.eclipselabs.real.core.logfile.ILogFile;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.PerformSearchRequest;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.util.TimeUnitWrapper;

public class LogFileTaskSearch<R extends ISearchResult<?>,M> extends LogFileTask<R, M> {

    private static final Logger log = LogManager.getLogger(LogFileTaskSearch.class);
    protected ISearchObject<R,?> theSearchObject;
    protected PerformSearchRequest searchRequest;
    protected ILogFile taskLogFile;

    public LogFileTaskSearch(IAddLogTaskResult<R, M> addCB, ILogFile logFile, ISearchObject<R,?> so,
            PerformSearchRequest request,
            TimeUnitWrapper waitTimeout, TimeUnitWrapper execTimeout) {
        super(addCB, waitTimeout, execTimeout);
        theSearchObject = so;
        searchRequest = request;
        taskLogFile = logFile;
    }

    @Override
    public R call() throws Exception {
        return get();
    }

    @Override
    public R get() {
        R result = null;
        if ((searchRequest.getProgressMonitor() != null) && (!searchRequest.getProgressMonitor().isSearchCancelled())) {
            log.debug("LogFileSearchTask LogFile " + taskLogFile);
            /*
             * In this case the exception won't go up in the hierarchy because this is a separate thread
             * Log it is all that can be done
             */
            try {
                searchRequest.setText(taskLogFile.getFileText());
                result = theSearchObject.performSearch(searchRequest);
            } catch (IncorrectPatternException e) {
                log.error("Incorrect pattern exception for file " + taskLogFile.getFilePath(), e);
                throw new IncorrectPatternExceptionRT(taskLogFile.getFilePath(), e);
            }
            searchRequest.getProgressMonitor().incrementCompletedSOFiles();
        } else {
            log.debug("LogFileSearchTask canceled LogFile " + taskLogFile);
        }

        return result;
    }
}
