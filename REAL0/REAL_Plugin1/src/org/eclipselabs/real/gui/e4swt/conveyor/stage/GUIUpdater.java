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
        public void updateStatus(GUISearchResult partSRObj, ISearchProgressMonitor searchMonitor, String searchID);
    }

    public static class StatusFormatter implements IStatusFormatter {

        @Override
        public void updateStatus(GUISearchResult partSRObj, ISearchProgressMonitor searchMonitor, String searchID) {
            partSRObj.setSearchStatus(searchID, "Searched files " + searchMonitor.getCompletedSOFiles()
                    + "/" + searchMonitor.getTotalSOFiles() + " Found objects " + searchMonitor.getObjectsFound(),
                    searchMonitor.getCompletedSOFiles(), searchMonitor.getTotalSOFiles());
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
        } catch (InterruptedException e1) {
            log.error("UI update error", e1);
        }
        final GUISearchResult partSRObj = (GUISearchResult) prodContext.getSearchPart().getObject();
        //statusFormatter.setSearchMonitor(prodContext.getSearchRequest().getProgressMonitor());
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
            }
        }
        if ((!SearchResultActiveState.DISPOSED.equals(partSRObj.getMainSearchState()))
                && (partSRObj.isSearchTabAvailable(prodContext.getSearchID()))) {
            uiSynch.syncExec(new Runnable() {

                @Override
                public void run() {
                    statusFormatter.updateStatus(partSRObj, prodContext.getSearchRequest().getProgressMonitor(),
                            prodContext.getSearchID());
                    partSRObj.stopProgress(prodContext.getSearchID());
                }
            });
        }
        future.complete(null);

    }

}
