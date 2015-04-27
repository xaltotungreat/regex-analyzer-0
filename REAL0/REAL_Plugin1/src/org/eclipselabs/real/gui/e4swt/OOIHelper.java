package org.eclipselabs.real.gui.e4swt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipselabs.real.gui.e4swt.dialogs.ProgressStatusDialog;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult.SearchResultActiveState;
import org.eclipselabs.real.gui.e4swt.util.FutureProgressMonitor;

public class OOIHelper {
    private static final Logger log = LogManager.getLogger(OOIHelper.class);

    private OOIHelper() {}
    /**
     * This method must be called from the UI thread
     * @param addInfo the GlobalOOIInfo to install
     * @param globOOIContext the context that contains the map of global OOIs. usually it should be
     * the context of the window
     * @param uiSynch to execute some code in the UI thread
     * @param resultsStack the PartStack that contains the results
     * @return a ListenableFuture that completes when the operation is complete
     */
    public static CompletableFuture<Void> installGlobalOOI(final GlobalOOIInfo addInfo, final IEclipseContext globOOIContext,
            final UISynchronize uiSynch, MPartStack resultsStack) {
        if ((addInfo == null) || (globOOIContext == null) || (uiSynch == null) || (resultsStack == null)) {
            log.error("installGlobalOOI One of the arguments is null addInfo=" + addInfo + " globOOIContext=" + globOOIContext
                    + " uiSynch=" + uiSynch + " resultsStack=" + resultsStack);
            CompletableFuture<Void> rt = CompletableFuture.completedFuture(null);
            //rt.set(null);
            return rt;
        }
        // get the map
        if (globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) == null) {
            globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST, new ConcurrentHashMap<String, GlobalOOIInfo>());
            globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, false);
        }
        final Map<String, GlobalOOIInfo> globalObjMap = (Map<String, GlobalOOIInfo>)globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
        int partsCount = 0;
        // select the parts for which to install the new global OOI
        final List<GUISearchResult> partsToExecute = new ArrayList<>();
        for (MStackElement currSE : resultsStack.getChildren()) {
            if ((currSE instanceof MPart) && (((MPart)currSE).isToBeRendered())) {
                GUISearchResult guiObj = (GUISearchResult)((MPart)currSE).getObject();
                if ((guiObj != null) && (guiObj.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED)) {
                    partsToExecute.add(guiObj);
                    partsCount++;
                }
            }
        }
        final int finalPartsCount = partsCount;

        // init and show the progress dialog
        final ProgressStatusDialog globObjProgressDialog = new ProgressStatusDialog((Shell)globOOIContext.get(IServiceConstants.ACTIVE_SHELL),
                SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL, "Install Global OOI");
        globObjProgressDialog.init(finalPartsCount, false);
        globObjProgressDialog.open();
        globObjProgressDialog.setStatus("Initializing...");

        Runnable removeAllRun = new Runnable() {

            @Override
            public void run() {
                final CountDownLatch ooiLatch = new CountDownLatch(finalPartsCount);
                log.info("Adding " + addInfo.getDisplayString() +" to Global Objects");
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        globObjProgressDialog.setStatus("Adding " + addInfo.getDisplayString() +" to Global Objects");
                        globObjProgressDialog.getShell().redraw();
                    }
                });
                for (GUISearchResult currSR : partsToExecute) {
                    if (currSR.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED) {
                        FutureProgressMonitor<Integer> globObjFPM = currSR.setStyleForPattern(addInfo.getTextPattern(), addInfo.getStyle(), false);
                        globObjFPM.getFuture().handle((Integer arg0, Throwable t) -> {
                            if (t != null) {
                                log.error("Error removing a global object ", t);
                            }
                            uiSynch.asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    globObjProgressDialog.increaseProgress(1);
                                }
                            });
                            ooiLatch.countDown();
                            return null;
                        });
                        /*Futures.addCallback(globObjFPM.getFuture(), new FutureCallback<Integer>() {

                            @Override
                            public void onSuccess(Integer arg0) {
                                uiSynch.asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        globObjProgressDialog.increaseProgress(1);
                                    }
                                });
                                ooiLatch.countDown();
                            }
                            @Override
                            public void onFailure(Throwable arg0) {
                                log.error("Error removing a global object ", arg0);
                                uiSynch.asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        globObjProgressDialog.increaseProgress(1);
                                    }
                                });
                                ooiLatch.countDown();
                            }
                        });*/
                    } else {
                        uiSynch.asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                globObjProgressDialog.increaseProgress(1);
                            }
                        });
                        ooiLatch.countDown();
                        log.error("execute The current part state is " + currSR.getMainSearchState());
                    }
                }
                try {
                    ooiLatch.await();
                    uiSynch.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            globObjProgressDialog.setStatus("Complete");
                            globObjProgressDialog.getShell().dispose();
                        }
                    });

                    log.debug("Adding " + addInfo.getDisplayString() + " all threads completed continue after latch");
                } catch (InterruptedException e) {
                    log.error("Interrupted countdown latch", e);
                }
                globalObjMap.put(addInfo.getTextPattern().pattern(), addInfo);
                // check the pending map
                Map<String, GlobalOOIInfo> globalOOIPendList = null;
                if (globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST) != null) {
                    globalOOIPendList = (Map<String, GlobalOOIInfo>)globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST);
                    if (globalOOIPendList.containsKey(addInfo.getTextPattern().pattern())) {
                        globalOOIPendList.remove(addInfo.getTextPattern().pattern());
                    }
                }
                // now update the OOI part through the context tracker
                globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, true);
                //return null;
            }
        };
        return CompletableFuture.runAsync(removeAllRun, Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor());
        //return Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().submit(removeAllRun);
    }

    /**
     * This static method must be called from the UI thread
     * @param addInfoList the List of GlobalOOIInfo to add to the map
     * @param globOOIContext the context that contains the global OOI map
     * @param uiSynch to execute some code in the UI thread
     * @param resultsStack the MPartStack that contains search results
     * @return a ListenableFuture that completes when the operation is complete
     */
    public static CompletableFuture<Void> installGlobalOOI(final List<GlobalOOIInfo> addInfoList, final IEclipseContext globOOIContext,
            final UISynchronize uiSynch, MPartStack resultsStack) {
        if ((addInfoList == null) || (globOOIContext == null) || (uiSynch == null) || (resultsStack == null)) {
            log.error("installGlobalOOI installGlobalOOIOne of the arguments is null addInfoList=" +addInfoList + " globOOIContext=" + globOOIContext
                    + " uiSynch=" + uiSynch + " resultsStack=" + resultsStack);
            CompletableFuture<Void> rt = CompletableFuture.completedFuture(null);
            //rt.set(null);
            return rt;
        }
        // get the map
        if (globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) == null) {
            globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST, new ConcurrentHashMap<String, GlobalOOIInfo>());
            globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, false);
        }
        final Map<String, GlobalOOIInfo> globalObjMap = (Map<String, GlobalOOIInfo>)globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);

        final int addGlobOOICount = addInfoList.size();
        // init and show the progress dialog
        final ProgressStatusDialog globObjProgressDialog = new ProgressStatusDialog((Shell)globOOIContext.get(IServiceConstants.ACTIVE_SHELL),
                SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL, "Install Global OOI");
        globObjProgressDialog.init(addGlobOOICount, false);
        globObjProgressDialog.open();
        globObjProgressDialog.setStatus("Initializing...");

        int partsCount = 0;
        // select the parts for which the new global OOIs will be installed
        final List<GUISearchResult> partsToExecute = new ArrayList<>();
        for (MStackElement currSE : resultsStack.getChildren()) {
            if ((currSE instanceof MPart) && (((MPart)currSE).isToBeRendered())) {
                GUISearchResult guiObj = (GUISearchResult)((MPart)currSE).getObject();
                if ((guiObj != null) && (guiObj.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED)) {
                    partsToExecute.add(guiObj);
                    partsCount++;
                }
            }
        }
        final int finalPartsCount = partsCount;

        final AtomicInteger progressCount = new AtomicInteger(0);
        Runnable removeAllRun = new Runnable() {

            @Override
            public void run() {
                for (final GlobalOOIInfo currOOI : addInfoList) {
                    final CountDownLatch ooiLatch = new CountDownLatch(finalPartsCount);
                    log.info("Adding " + currOOI.getDisplayString() +" to Global Objects");
                    uiSynch.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            globObjProgressDialog.setStatus("Adding " + currOOI.getDisplayString() +" to Global Objects");
                            globObjProgressDialog.getShell().redraw();
                        }
                    });
                    for (GUISearchResult currSR : partsToExecute) {
                        if (currSR.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED) {
                            FutureProgressMonitor<Integer> globObjFPM = currSR.setStyleForPattern(currOOI.getTextPattern(), currOOI.getStyle(), false);
                            globObjFPM.getFuture().handle((Integer arg0, Throwable t) -> {
                                if (t != null) {
                                    log.error("Error installing a global object ", t);
                                }
                                ooiLatch.countDown();
                                return null;
                            });
                            /*Futures.addCallback(globObjFPM.getFuture(), new FutureCallback<Integer>() {

                                @Override
                                public void onSuccess(Integer arg0) {
                                    ooiLatch.countDown();
                                }
                                @Override
                                public void onFailure(Throwable arg0) {
                                    log.error("Error installing a global object ", arg0);
                                    ooiLatch.countDown();
                                }
                            });*/
                        } else {
                            ooiLatch.countDown();
                            log.error("execute The current part state is " + currSR.getMainSearchState());
                        }
                    }
                    try {
                        ooiLatch.await();
                        if (addGlobOOICount == progressCount.incrementAndGet()) {
                            uiSynch.syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    globObjProgressDialog.increaseProgress(1);
                                    if (addGlobOOICount == progressCount.get()) {
                                        globObjProgressDialog.setStatus("Complete");
                                        globObjProgressDialog.getShell().dispose();
                                    }
                                }
                            });
                        } else {
                            uiSynch.asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    globObjProgressDialog.increaseProgress(1);
                                    if (addGlobOOICount == progressCount.get()) {
                                        globObjProgressDialog.setStatus("Complete");
                                        globObjProgressDialog.getShell().dispose();
                                    }
                                }
                            });
                        }
                        log.debug("Adding " + currOOI.getDisplayString() + " all threads completed continue after latch");
                    } catch (InterruptedException e) {
                        log.error("Interrupted countdown latch", e);
                    }
                }
                for (GlobalOOIInfo addInfo : addInfoList) {
                    globalObjMap.put(addInfo.getTextPattern().pattern(), addInfo);
                }
                globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, true);
                //return null;
            }
        };
        //return Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().submit(removeAllRun);
        return CompletableFuture.runAsync(removeAllRun, Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor());
    }

    /**
     * This method must be called from the UI thread
     * @param removeInfo the Gloabl OOI to remove
     * @param globOOIContext the context that contains the map of GlobalOOI
     * @param uiSynch to execute some code in the UI thread
     * @param resultsStack the PartStack that contains search results
     * @return a ListenableFuture that completes when the operation is complete
     */
    public static CompletableFuture<Void> removeGlobalOOI(final GlobalOOIInfo removeInfo, final IEclipseContext globOOIContext,
            final UISynchronize uiSynch, MPartStack resultsStack) {
        if ((removeInfo == null) || (globOOIContext == null) || (uiSynch == null) || (resultsStack == null)) {
            log.error("One of the arguments is null removeInfo=" + removeInfo + " globOOIContext=" + globOOIContext
                    + " uiSynch=" + uiSynch + " resultsStack=" + resultsStack);
            CompletableFuture<Void> rt = CompletableFuture.completedFuture(null);
            //rt.set(null);
            return rt;
        }
        // get the map
        final Map<String, GlobalOOIInfo> globalObjMap = (Map<String, GlobalOOIInfo>)globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);

        int partsCount = 0;
        // select the parts for which the global OOI will be removed
        final List<GUISearchResult> partsToExecute = new ArrayList<>();
        for (MStackElement currSE : resultsStack.getChildren()) {
            if ((currSE instanceof MPart) && (((MPart)currSE).isToBeRendered())) {
                GUISearchResult guiObj = (GUISearchResult)((MPart)currSE).getObject();
                if ((guiObj != null) && (guiObj.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED)) {
                    partsToExecute.add(guiObj);
                    partsCount++;
                }
            }
        }
        final int finalPartsCount = partsCount;

        // init and show the progress dialog
        final ProgressStatusDialog globObjProgressDialog = new ProgressStatusDialog((Shell)globOOIContext.get(IServiceConstants.ACTIVE_SHELL),
                SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL, "Remove Global OOI");
        globObjProgressDialog.init(finalPartsCount, false);
        globObjProgressDialog.open();
        globObjProgressDialog.setStatus("Initializing...");

        Runnable removeAllRun = new Runnable() {

            @Override
            public void run() {
                final CountDownLatch ooiLatch = new CountDownLatch(finalPartsCount);
                log.info("Removing " + removeInfo.getDisplayString() + " from Global Objects");
                uiSynch.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        globObjProgressDialog.setStatus("Removing " + removeInfo.getDisplayString() + " from Global Objects");
                        globObjProgressDialog.getShell().redraw();
                    }
                });
                for (GUISearchResult currSR : partsToExecute) {
                    if (currSR.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED) {
                        CompletableFuture<Void> globObjFuture = currSR.removeStyleForPattern(removeInfo.getTextPattern());
                        globObjFuture.handle((Void arg0, Throwable t) -> {
                            if (t != null) {
                                log.error("Error removing a global object ", t);
                            }
                            uiSynch.asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    globObjProgressDialog.increaseProgress(1);
                                }
                            });
                            ooiLatch.countDown();
                            return null;
                        });
                        /*Futures.addCallback(globObjFuture, new FutureCallback<Void>() {

                            @Override
                            public void onSuccess(Void arg0) {
                                uiSynch.asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        globObjProgressDialog.increaseProgress(1);
                                    }
                                });
                                ooiLatch.countDown();
                            }
                            @Override
                            public void onFailure(Throwable arg0) {
                                log.error("Error removing a global object ", arg0);
                                uiSynch.asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        globObjProgressDialog.increaseProgress(1);
                                    }
                                });
                                ooiLatch.countDown();
                            }
                        });*/
                    } else {
                        uiSynch.asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                globObjProgressDialog.increaseProgress(1);
                            }
                        });
                        ooiLatch.countDown();
                        log.error("execute The current part state is " + currSR.getMainSearchState());
                    }
                }
                try {
                    ooiLatch.await();
                    uiSynch.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            globObjProgressDialog.setStatus("Complete");
                            globObjProgressDialog.getShell().dispose();
                        }
                    });
                    log.debug("Removing " + removeInfo.getDisplayString() + " all threads completed continue after latch");
                } catch (InterruptedException e) {
                    log.error("Interrupted countdown latch", e);
                }

                globalObjMap.remove(removeInfo.getTextPattern().pattern());
                globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, true);
                //return null;
            }
        };
        return CompletableFuture.runAsync(removeAllRun, Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor());
        //return Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().submit(removeAllRun);
    }

    /**
     * This static method should be run from the UI thread
     * @param removeInfoList the List of GlobalOOIInfo to remove from the map
     * @param globOOIContext the context that contains the global OOI map
     * @param uiSynch to execute some code in the UI thread
     * @param resultsStack the MPartStack that contains search results
     * @return a ListenableFuture that completes when the operation is complete
     */
    public static CompletableFuture<Void> removeGlobalOOI(final List<GlobalOOIInfo> removeInfoList, final IEclipseContext globOOIContext,
            final UISynchronize uiSynch, MPartStack resultsStack) {
        if ((removeInfoList == null) || (globOOIContext == null) || (uiSynch == null) || (resultsStack == null)) {
            log.error("One of the arguments is null removeInfoList=" +removeInfoList + " globOOIContext=" + globOOIContext
                    + " uiSynch=" + uiSynch + " resultsStack=" + resultsStack);
            CompletableFuture<Void> rt = CompletableFuture.completedFuture(null);
            //rt.set(null);
            return rt;
        }
        // get the map
        final Map<String, GlobalOOIInfo> globalObjMap = (Map<String, GlobalOOIInfo>)globOOIContext.get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);

        // verify all passed global OOI are present in the map
        // remove the ones that aren't
        for (GlobalOOIInfo globInfo : removeInfoList) {
            if (!globalObjMap.containsKey(globInfo.getTextPattern().pattern())) {
                removeInfoList.remove(globInfo);
            }
        }
        final int globalObjCount = removeInfoList.size();
        // init and show the progress dialog
        final ProgressStatusDialog globObjProgressDialog = new ProgressStatusDialog((Shell)globOOIContext.get(IServiceConstants.ACTIVE_SHELL),
                SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL, "Remove Global OOI");
        globObjProgressDialog.init(globalObjCount, false);
        globObjProgressDialog.open();
        globObjProgressDialog.setStatus("Initializing...");

        int partsCount = 0;
        // select the parts for which the global OOs will be removed
        final List<GUISearchResult> partsToExecute = new ArrayList<>();
        for (MStackElement currSE : resultsStack.getChildren()) {
            if ((currSE instanceof MPart) && (((MPart)currSE).isToBeRendered())) {
                GUISearchResult guiObj = (GUISearchResult)((MPart)currSE).getObject();
                if ((guiObj != null) && (guiObj.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED)) {
                    partsToExecute.add(guiObj);
                    partsCount++;
                }
            }
        }
        final int finalPartsCount = partsCount;

        final AtomicInteger progressCount = new AtomicInteger(0);
        Runnable removeAllRun = new Runnable() {

            @Override
            public void run() {
                for (final GlobalOOIInfo currOOI : removeInfoList) {
                    final CountDownLatch ooiLatch = new CountDownLatch(finalPartsCount);
                    log.info("Removing " + currOOI.getDisplayString() + " from Global Objects");
                    uiSynch.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            globObjProgressDialog.setStatus("Removing " + currOOI.getDisplayString() + " from Global Objects");
                            globObjProgressDialog.getShell().redraw();
                        }
                    });
                    for (GUISearchResult currSR : partsToExecute) {
                        if (currSR.getMainSearchState() == SearchResultActiveState.SEARCH_COMPLETED) {
                            CompletableFuture<Void> globObjFuture = currSR.removeStyleForPattern(currOOI.getTextPattern());
                            globObjFuture.handle((Void arg0, Throwable t) -> {
                                if (t != null) {
                                    log.error("Error removing a global object ", arg0);
                                }
                                ooiLatch.countDown();
                                return null;
                            });
                            /*Futures.addCallback(globObjFuture, new FutureCallback<Void>() {

                                @Override
                                public void onSuccess(Void arg0) {
                                    ooiLatch.countDown();
                                }
                                @Override
                                public void onFailure(Throwable arg0) {
                                    log.error("Error removing a global object ", arg0);
                                    ooiLatch.countDown();
                                }
                            });*/
                        } else {
                            ooiLatch.countDown();
                            log.error("execute The current part state is " + currSR.getMainSearchState());
                        }
                    }
                    try {
                        ooiLatch.await();
                        uiSynch.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                globObjProgressDialog.increaseProgress(1);
                                if (globalObjCount == progressCount.incrementAndGet()) {
                                    globObjProgressDialog.setStatus("Complete");
                                    globObjProgressDialog.getShell().dispose();
                                }
                            }
                        });

                        log.debug("Removing " + currOOI.getDisplayString() + " all threads completed continue after latch");
                    } catch (InterruptedException e) {
                        log.error("Interrupted countdown latch", e);
                    }
                }
                for (GlobalOOIInfo removeInfo : removeInfoList) {
                    globalObjMap.remove(removeInfo.getTextPattern().pattern());
                }
                globOOIContext.set(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST_CHANGED, true);
                //return null;
            }
        };
        return CompletableFuture.runAsync(removeAllRun, Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor());
        //return Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().submit(removeAllRun);
    }

}
