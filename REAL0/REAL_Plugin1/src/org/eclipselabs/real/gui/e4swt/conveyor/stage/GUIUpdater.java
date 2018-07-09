package org.eclipselabs.real.gui.e4swt.conveyor.stage;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipselabs.real.core.searchobject.ISearchProgressMonitor;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvProductContext;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult.SearchResultActiveState;

public class GUIUpdater implements Runnable {

    private static final Logger log = LogManager.getLogger(GUIUpdater.class);

    private CompletableFuture<Void> future;

    private volatile ConvProductContext prodContext;

    private UISynchronize uiSynch;

    private long sleepInterval;

    private IStatusFormatter statusFormatter;

    public interface IStatusFormatter {
        void updateStatus(GUISearchResult partSRObj, ISearchProgressMonitor searchMonitor, String searchID);
        default void setErrorStatus(GUISearchResult partSRObj, String searchID, String errorMsg) {
            partSRObj.setErrorStatus(searchID, errorMsg);
        }
    }

    public static class StatusFormatter implements IStatusFormatter {

        @Override
        public void updateStatus(GUISearchResult partSRObj, ISearchProgressMonitor searchMonitor, String searchID) {
            partSRObj.setSearchStatus(searchID, "Searched files " + searchMonitor.getCompletedSOFiles()
                    + "/" + searchMonitor.getTotalSOFiles() + " Found objects " + searchMonitor.getObjectsFound(),
                    searchMonitor.getCompletedSOFiles(), searchMonitor.getTotalSOFiles());
        }

        @Override
        public void setErrorStatus(GUISearchResult partSRObj, String searchID, String errorMsg) {
            partSRObj.setErrorStatus(searchID, errorMsg);
        }
    }

    public GUIUpdater(CompletableFuture<Void> f, ConvProductContext ctxt, UISynchronize synch, long sleepInt) {
        this(f, ctxt, synch, sleepInt, new StatusFormatter());
    }

    public GUIUpdater(CompletableFuture<Void> f, ConvProductContext ctxt, UISynchronize synch, long sleepInt, IStatusFormatter formt) {
        future = f;
        prodContext = ctxt;
        uiSynch = synch;
        sleepInterval = sleepInt;
        statusFormatter = formt;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(sleepInterval);
        } catch (InterruptedException e) {
            log.error("UI update error", e);
            // Restore interrupted state in accordance with the Sonar rule squid:S2142
            Thread.currentThread().interrupt();
        }
        final GUISearchResult partSRObj = (GUISearchResult) prodContext.getSearchPart().getObject();
        while ((!prodContext.isComplete()) && (prodContext.isProceed())
                    && (!SearchResultActiveState.DISPOSED.equals(partSRObj.getMainSearchState()))
                    && (partSRObj.isSearchTabAvailable(prodContext.getSearchID()))) {
            try {
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        statusFormatter.updateStatus(partSRObj, prodContext.getSearchRequest().getProgressMonitor(),
                                prodContext.getSearchID());
                    }
                });
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                log.error("UI update error", e);
                // Restore interrupted state in accordance with the Sonar rule squid:S2142
                Thread.currentThread().interrupt();
            }
        }
        if ((!SearchResultActiveState.DISPOSED.equals(partSRObj.getMainSearchState()))
                && (partSRObj.isSearchTabAvailable(prodContext.getSearchID()))) {
            if (prodContext.getAbortMessage() == null) {
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        statusFormatter.updateStatus(partSRObj, prodContext.getSearchRequest().getProgressMonitor(),
                                prodContext.getSearchID());
                        partSRObj.stopProgress(prodContext.getSearchID());
                    }
                });
            } else {
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        statusFormatter.setErrorStatus(partSRObj, prodContext.getSearchID(),
                                prodContext.getAbortMessage());
                    }
                });
            }
        }
        future.complete(null);

    }

}
