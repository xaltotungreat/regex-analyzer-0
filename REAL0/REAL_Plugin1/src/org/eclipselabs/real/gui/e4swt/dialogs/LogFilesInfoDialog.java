package org.eclipselabs.real.gui.e4swt.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.core.logfile.LogFileAggregateInfo;
import org.eclipselabs.real.core.logfile.LogFileInfo;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.gui.e4swt.Eclipse4GUIBridge;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;

import com.google.common.util.concurrent.ListenableFuture;

@Creatable
public class LogFilesInfoDialog extends SingleDialog {

    private static final Logger log = LogManager.getLogger(LogFilesInfoDialog.class);

    protected List<ListenableFuture<LogFileAggregateInfo>> aggrFuturesList = Collections.synchronizedList(new ArrayList<ListenableFuture<LogFileAggregateInfo>>());
    protected List<LogFileAggregateInfo> aggrInfoList;

    @Inject
    UISynchronize uiSynch;

    protected Double[] columnWeights = new Double[] {0.1, 0.15, 0.1, 0.1, 0.55};

    private Table tableResults;
    private ProgressBar progressBar;
    private Button btnOK;
    private Label lblStatus;
    private static String DATA_KEY_LOG_TYPE = "LogType";
    private static String DATA_KEY_FILE_NAME = "FileName";

    @Inject
    public LogFilesInfoDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
        setText("Read files");
    }

    public void initFuturesList(List<ListenableFuture<LogFileAggregateInfo>> dialogResults) {
        aggrFuturesList.clear();
        aggrFuturesList.addAll(dialogResults);
    }

    public void initInfoList(List<LogFileAggregateInfo> dialogResults) {
        aggrInfoList = dialogResults;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public void open() {
        createContents();
        shell.layout();
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(900, 450);
        shell.setText(getText());
        shell.setLayout(new FormLayout());

        registerContextRemoveOnClose();

        btnOK = new Button(shell, SWT.NONE);
        FormData fd_btnOK = new FormData();
        fd_btnOK.right = new FormAttachment(100, -10);
        fd_btnOK.top = new FormAttachment(100, -30);
        fd_btnOK.bottom = new FormAttachment(100, -5);
        fd_btnOK.left = new FormAttachment(85, -5);
        btnOK.setLayoutData(fd_btnOK);
        btnOK.setText("OK");
        btnOK.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        shell.setDefaultButton(btnOK);

        tableResults = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        FormData fd_tableResults = new FormData();
        fd_tableResults.bottom = new FormAttachment(100, -35);
        fd_tableResults.right = new FormAttachment(100);
        fd_tableResults.top = new FormAttachment(0);
        fd_tableResults.left = new FormAttachment(0);
        tableResults.setLayoutData(fd_tableResults);
        tableResults.setHeaderVisible(true);
        tableResults.setLinesVisible(true);
        tableResults.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                Table table = (Table)e.getSource();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    TableColumn tc = table.getColumn(i);
                    tc.setWidth((int)(table.getClientArea().width*columnWeights[i]));
                }
            }
        });
        TableColumn logTypeCol = new TableColumn(tableResults, SWT.NONE);
        logTypeCol.setText("Log type");
        TableColumn fnShortCol = new TableColumn(tableResults, SWT.NONE);
        fnShortCol.setText("File name");
        TableColumn fileReadCol = new TableColumn(tableResults, SWT.NONE);
        fileReadCol.setText("File read");
        TableColumn fileSizeCol = new TableColumn(tableResults, SWT.NONE);
        fileSizeCol.setText("File size(MB)");
        TableColumn fnFullCol = new TableColumn(tableResults, SWT.NONE);
        fnFullCol.setText("File path");

        lblStatus = new Label(shell, SWT.NONE);
        lblStatus.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NORMAL));
        FormData fd_lblStatus = new FormData();
        fd_lblStatus.bottom = new FormAttachment(100, -5);
        fd_lblStatus.right = new FormAttachment(84, -5);
        fd_lblStatus.top = new FormAttachment(100, -30);
        fd_lblStatus.left = new FormAttachment(51);
        lblStatus.setLayoutData(fd_lblStatus);

        if (aggrInfoList != null) {
            Collections.sort(aggrInfoList, new Comparator<LogFileAggregateInfo>() {

                @Override
                public int compare(LogFileAggregateInfo o1,
                        LogFileAggregateInfo o2) {
                    if ((o1 != null) && (o2 != null)) {
                        return o1.getLogAggregateType().compareTo(o2.getLogAggregateType());
                    } else if ((o1 != null) && (o2 == null)) {
                        return 1;
                    } else if ((o1 == null) && (o2 != null)) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            for (LogFileAggregateInfo currAggrResult : aggrInfoList) {
                currAggrResult.sortFileResults();
                if (currAggrResult.getLogFileInfos() != null) {
                    for (LogFileInfo currFileResult : currAggrResult.getLogFileInfos()) {
                        String fileName = currFileResult.getFileFullName().substring(
                                currFileResult.getFileFullName().lastIndexOf(System.getProperty("file.separator")) + 1);
                        TableItem ti = new TableItem(tableResults, SWT.NONE);

                        ti.setText(new String[]{currAggrResult.getLogAggregateType().getLogTypeName(), fileName,
                                String.valueOf(currFileResult.getInMemory()),
                                String.format("%.2f", currFileResult.getFileSize()),
                                currFileResult.getFileFullName()});
                    }
                }
            }
            btnOK.setEnabled(true);
            for (TableColumn tc : tableResults.getColumns()) {
                tc.pack();
            }
            tableResults.pack();
        } else if ((aggrFuturesList != null) && (!aggrFuturesList.isEmpty())) {
            btnOK.setEnabled(false);
            progressBar = new ProgressBar(shell, SWT.NONE);
            FormData fd_progressBar = new FormData();
            fd_progressBar.right = new FormAttachment(49, -5);
            fd_progressBar.left = new FormAttachment(0, 5);
            fd_progressBar.bottom = new FormAttachment(100, -5);
            fd_progressBar.top = new FormAttachment(100, -30);
            progressBar.setLayoutData(fd_progressBar);
            progressBar.setMaximum(aggrFuturesList.size());
            int guiUpdate = PerformanceUtils.getIntProperty(IEclipse4Constants.PERF_CONST_GUI_UPDATE_INTERVAL,
                    IEclipse4Constants.PERF_CONST_GUI_UPDATE_INTERVAL_DEFAULT);
            Runnable updateUIThread = new Runnable() {

                @Override
                public void run() {
                    log.debug("Starting update LogFiles thread futures size=" + aggrFuturesList.size());
                    List<LogFileAggregateInfo> infoList = new ArrayList<LogFileAggregateInfo>();
                    boolean runFlag = true;
                    int futuresNotComplete = 0;
                    int futuresComplete = 0;
                    int futuresCanceled = 0;
                    int prevFuturesNotComplete = 0;
                    int prevFuturesComplete = 0;
                    int prevFuturesCanceled = 0;
                    while (runFlag) {
                        try {
                            Thread.sleep(guiUpdate);
                        } catch (InterruptedException e1) {
                            log.error("Sleep interrupted", e1);
                        }
                        // cleanup before processing
                        infoList.clear();
                        prevFuturesNotComplete = futuresNotComplete;
                        prevFuturesComplete = futuresComplete;
                        prevFuturesCanceled = futuresCanceled;
                        futuresNotComplete = 0;
                        futuresComplete = 0;
                        futuresCanceled = 0;
                        try {
                            for (ListenableFuture<LogFileAggregateInfo> infoFuture : aggrFuturesList) {
                                if (infoFuture.isCancelled()) {
                                    futuresComplete++;
                                    futuresCanceled++;
                                } else if (infoFuture.isDone()) {
                                    infoList.add(infoFuture.get());
                                    futuresComplete++;
                                } else {
                                    futuresNotComplete++;
                                }
                            }
                            log.debug("Update LogFiles thread completed=" + futuresComplete + " not completed " + futuresNotComplete);
                            Collections.sort(infoList, new Comparator<LogFileAggregateInfo>() {

                                @Override
                                public int compare(LogFileAggregateInfo o1,
                                        LogFileAggregateInfo o2) {
                                    if ((o1 != null) && (o2 != null)) {
                                        return o1.getLogAggregateType().compareTo(o2.getLogAggregateType());
                                    } else if ((o1 != null) && (o2 == null)) {
                                        return 1;
                                    } else if ((o1 == null) && (o2 != null)) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            log.error("Generating LogFilesInfo exception", e);
                        } catch (ExecutionException e) {
                            log.error("Generating LogFilesInfo exception", e);
                        }
                        if ((futuresComplete == prevFuturesComplete)
                                && (futuresNotComplete == prevFuturesNotComplete)
                                && (futuresCanceled == prevFuturesCanceled)) {
                            continue;
                        }
                        final int futuresCompleteFinal = futuresComplete;
                        final int futuresCalceledFinal = futuresCanceled;
                        uiSynch.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                for (LogFileAggregateInfo currAggrResult : infoList) {
                                    int treeItemIndex = 0;
                                    currAggrResult.sortFileResults();
                                    if (tableResults.getItemCount() > treeItemIndex) {
                                        TableItem itemAtIndex = tableResults.getItem(treeItemIndex);
                                        boolean equalFound = false;
                                        String itemAtIndexLogType = (String)itemAtIndex.getData(DATA_KEY_LOG_TYPE);
                                        while(itemAtIndexLogType.compareTo(currAggrResult.getLogAggregateType().getLogTypeName()) <= 0) {
                                            if (itemAtIndexLogType.equals(currAggrResult.getLogAggregateType().getLogTypeName())) {
                                                equalFound = true;
                                            }
                                            treeItemIndex++;
                                            if (tableResults.getItemCount() <= treeItemIndex) {
                                                break;
                                            }
                                            itemAtIndex = tableResults.getItem(treeItemIndex);
                                            itemAtIndexLogType = (itemAtIndex != null)?(String)itemAtIndex.getData(DATA_KEY_LOG_TYPE):"null";

                                        }
                                        if (equalFound) {
                                            continue;
                                        }
                                    }

                                    if (currAggrResult.getLogFileInfos() != null) {
                                        for (LogFileInfo currFileResult : currAggrResult.getLogFileInfos()) {
                                            String fileName = currFileResult.getFileFullName().substring(
                                                    currFileResult.getFileFullName().lastIndexOf(System.getProperty("file.separator")) + 1);
                                            TableItem ti = new TableItem(tableResults, SWT.NONE, treeItemIndex);
                                            if ((currFileResult.getLastReadSuccessful() == null) || (currFileResult.getLastReadSuccessful())) {
                                                ti.setText(new String[]{currAggrResult.getLogAggregateType().getLogTypeName(), fileName,
                                                        String.valueOf(currFileResult.getInMemory()),
                                                        String.format("%.2f", currFileResult.getFileSize()),
                                                        currFileResult.getFileFullName()});
                                            } else {
                                                ti.setText(new String[]{currAggrResult.getLogAggregateType().getLogTypeName(), fileName,
                                                        String.valueOf(currFileResult.getInMemory()),
                                                        currFileResult.getLastReadException().getMessage(),
                                                        currFileResult.getFileFullName()});
                                            }
                                            ti.setData(DATA_KEY_LOG_TYPE, currAggrResult.getLogAggregateType().getLogTypeName());
                                            ti.setData(DATA_KEY_FILE_NAME, fileName);
                                            treeItemIndex++;
                                        }
                                    }
                                }
                                progressBar.setSelection(futuresCompleteFinal);
                                setState("Tasks: " + infoList.size() + " Completed: " + futuresCompleteFinal + " Cancelled: " + futuresCalceledFinal);
                                tableResults.getParent().layout();
                            }
                        });
                        if (futuresNotComplete == 0) {
                            runFlag = false;
                        }
                    }
                    uiSynch.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            btnOK.setEnabled(true);
                            progressBar.dispose();
                        }
                    });
                }
            };
            Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().execute(updateUIThread);
        }
    }

    public List<ListenableFuture<LogFileAggregateInfo>> getAggrFuturesList() {
        return aggrFuturesList;
    }

    public void setAggrFuturesList(List<ListenableFuture<LogFileAggregateInfo>> aggrFutures) {
        aggrFuturesList = aggrFutures;
    }

    public List<LogFileAggregateInfo> getAggrInfoList() {
        return aggrInfoList;
    }

    public void setAggrInfoList(List<LogFileAggregateInfo> aggrInfoList) {
        this.aggrInfoList = aggrInfoList;
    }

    public void setState(String newStatus) {
        lblStatus.setText(newStatus);
    }
}
