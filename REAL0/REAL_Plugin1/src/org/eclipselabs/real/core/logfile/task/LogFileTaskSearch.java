package org.eclipselabs.real.core.logfile.task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.ILogFile;
import org.eclipselabs.real.core.logfile.LogFileInfo;
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
        R result = null;
        if ((searchRequest.getProgressMonitor() != null) && (!searchRequest.getProgressMonitor().isSearchCancelled())) {
            log.debug("LogFileSearchTask LogFile " + taskLogFile);
            if (taskLogFile.isRead()) {
                searchRequest.setText(taskLogFile.getFileText(false));
                result = theSearchObject.performSearch(searchRequest);
            } else {
                LogFileInfo currRes = taskLogFile.readFile();
                if (currRes.getLastReadSuccessful()) {
                    searchRequest.setText(taskLogFile.getFileText(true));
                    result = theSearchObject.performSearch(searchRequest);
                    searchRequest.setText(null);
                    taskLogFile.cleanFile();
                    System.gc();
                } else {
                    log.error("Unable to read file returning null search result", currRes.getLastReadException());
                }
            }
            searchRequest.getProgressMonitor().incrementCompletedSOFiles();
        } else {
            log.debug("LogFileSearchTask canceled LogFile " + taskLogFile);
        }

        return result;
    }
}
