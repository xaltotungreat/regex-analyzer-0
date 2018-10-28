package org.eclipselabs.real.gui.e4swt.parts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.core.event.CoreEventBus;
import org.eclipselabs.real.core.searchobject.ISearchObjectConstants;
import org.eclipselabs.real.core.util.PerformanceUtils;
import org.eclipselabs.real.gui.core.GUIConfigController;
import org.eclipselabs.real.gui.core.GUIConfigKey;
import org.eclipselabs.real.gui.core.GUIConfigObjectType;
import org.eclipselabs.real.gui.core.GUIProperty;
import org.eclipselabs.real.gui.core.result.IComplexDisplayResult;
import org.eclipselabs.real.gui.core.result.IDRViewItem;
import org.eclipselabs.real.gui.core.result.IDisplayResult;
import org.eclipselabs.real.gui.core.util.SearchInfo;
import org.eclipselabs.real.gui.e4swt.Eclipse4GUIBridge;
import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.NamedBookmark;
import org.eclipselabs.real.gui.e4swt.OOIInfo;
import org.eclipselabs.real.gui.e4swt.dialogs.ProgressStatusDialog;
import org.eclipselabs.real.gui.e4swt.event.SRPartDisposedEvent;
import org.eclipselabs.real.gui.e4swt.event.SRTabDisposedEvent;
import org.eclipselabs.real.gui.e4swt.util.FindRegexAnalysisResult;
import org.eclipselabs.real.gui.e4swt.util.FutureProgressMonitor;
import org.eclipselabs.real.gui.e4swt.widgets.StyledTable;
import org.eclipselabs.real.gui.e4swt.widgets.StyledTableItem;
import org.eclipselabs.real.gui.e4swt.widgets.StyledText2;

/**
 * The main class for the GUI object for the search result part.
 * It contains all the GUI elements, states, provides the API to update the GUI elements
 * @author Vadim Korkin
 *
 */
public class GUISearchResult {
    private static final Logger log = LogManager.getLogger(GUISearchResult.class);

    @Inject
    IEclipseContext partContext;

    @Inject
    EModelService modelService;

    @Inject
    EPartService partService;

    @Inject
    MApplication application;

    @Inject
    UISynchronize uiSynch;

    @Inject
    EMenuService menuService;

    @Inject
    IStylingEngine stylingEngine;

    @Inject
    ECommandService commandService;

    @Inject
    EHandlerService handlerService;

    @Inject
    MWindow window;

    // class level GUI objects
    private SashForm srSashForm;
    private StyledText2 styledText;
    private Composite placeHolderComposite;
    private CTabFolder tabFolder;

    // contexts
    Map<String,IEclipseContext> contextMap = new ConcurrentHashMap<>();
    protected volatile IEclipseContext activeContext;

    // Object properties
    protected Long textLength;
    protected String mainSearchID;
    protected SearchInfo mainSearchInfo;
    protected SearchResultActiveState activeState = SearchResultActiveState.INIT;
    // the default value is 100
    protected int gcMaxCount = 100;

    // OOI cache
    protected Map<String,OOIInfo> styleOOICache = new ConcurrentHashMap<>();

    // Named Bookmarks
    protected List<NamedBookmark> localBookmarks = Collections.synchronizedList(new ArrayList<NamedBookmark>());

    private static final String PERF_CONST_MAX_WRAP_LENGTH = "org.eclipselabs.real.gui.e4swt.parts.WrapSizeLimit";

    // internal constants
    public static final String DATA_SEARCH_RESULT_ITEM_KEY = "ViewObject";
    public static final String DATA_COLUMN_VIEW_NAME = "ColumnViewName";
    public static final String CONTEXT_KEY_TABLE_ROWS = "TableRows";
    public static final String CONTEXT_KEY_SELECTED_ROW_INDEX = "SelectedRowIndex";
    public static final String CONTEXT_KEY_SORT_COLUMN = "SortColumn";
    public static final String CONTEXT_KEY_SORT_COLUMN_INDEX = "SortColumnIndex";
    public static final String CONTEXT_KEY_SORT_COLUMN_DIRECTION = "SortColumnDirection";
    public static final String CONTEXT_KEY_SEARCH_INFO = "SearchInfo";
    public static final String CONTEXT_KEY_SEARCH_ID = "SearchID";
    public static final String CONTEXT_KEY_VIEW_NAMES = "ViewNames";


    public static enum SearchResultActiveState {
        INIT,
        SEARCH_IN_PROGRESS,
        SEARCH_COMPLETED,
        DISPOSED;
    }

    @Inject
    public GUISearchResult() {
        gcMaxCount = PerformanceUtils.getIntProperty(ISearchObjectConstants.PERF_CONST_MAX_GC_COUNT, gcMaxCount);
    }

    /**
     * Create contents of the view part.
     */
    @PostConstruct
    protected void createControls(Composite parent) {
        parent.setLayout(new FormLayout());

        srSashForm = new SashForm(parent, SWT.VERTICAL);
        FormData fd_srSashForm = new FormData();
        fd_srSashForm.right = new FormAttachment(100);
        fd_srSashForm.bottom = new FormAttachment(100);
        fd_srSashForm.top = new FormAttachment(0);
        fd_srSashForm.left = new FormAttachment(0);
        srSashForm.setLayoutData(fd_srSashForm);

        placeHolderComposite = new Composite(srSashForm, SWT.NONE);
        placeHolderComposite.setLayout(new FillLayout());

        tabFolder = new CTabFolder(srSashForm, SWT.NONE);
        tabFolder.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((CTabItem)e.item).getData(IEclipse4Constants.LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY) != null) {
                    activeContext = contextMap.get(((CTabItem)e.item).getData(IEclipse4Constants.LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY).toString());
                    window.getContext().set(IEclipse4Constants.CONTEXT_SEARCH_RESULT_ACTIVE_VIEW_TAB, e.item);
                    if ((activeContext != null) && (activeContext.get(StyledTable.class) != null) &&
                            (activeContext.get(CONTEXT_KEY_SELECTED_ROW_INDEX) != null)) {
                        activeContext.get(StyledTable.class).setSelection((Integer)activeContext.get(CONTEXT_KEY_SELECTED_ROW_INDEX));
                    }
                }
            }
        });
        menuService.registerContextMenu(tabFolder, "gui_search_result.view_popupmenu");

        srSashForm.setWeights(new int[] {2, 1});
    }

    public void beginNewViewResult(String searchID, String tabText, String tabTooltip) {
        IEclipseContext newCtxt = partContext.createChild();
        newCtxt.set(EclipseContext.DEBUG_STRING, searchID);
        log.debug("beginNewViewResult Adding context to map " + searchID);
        if (getMainSearchState() == SearchResultActiveState.INIT) {
            mainSearchID = searchID;
            setMainSearchState(SearchResultActiveState.SEARCH_IN_PROGRESS);
        }
        contextMap.put(searchID, newCtxt);
        activeContext = newCtxt;
        activeContext.set(CONTEXT_KEY_SEARCH_ID, searchID);
        activeContext.set(SearchResultActiveState.class, SearchResultActiveState.SEARCH_IN_PROGRESS);
        activeContext.set(IEclipse4Constants.LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY, searchID);

        CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
        tabItem1.setText(tabText);
        tabItem1.setData(IEclipse4Constants.LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY, searchID);
        tabItem1.setShowClose(true);
        tabItem1.setToolTipText(tabTooltip);
        tabFolder.setSelection(tabItem1);
        newCtxt.set(CTabItem.class, tabItem1);

        tabItem1.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                String searchIDDisposed = (String)((CTabItem)e.getSource()).getData(IEclipse4Constants.LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY);
                IEclipseContext tabCtxt = contextMap.get(searchIDDisposed);
                if (tabCtxt != null) {
                    if ((tabCtxt.get(SearchInfo.class) != null) && (mainSearchInfo.getChildren() != null)) {
                        mainSearchInfo.getChildren().remove(tabCtxt.get(SearchInfo.class));
                    }
                    if (tabCtxt.get(StyledTable.class) != null) {
                        tabCtxt.get(StyledTable.class).dispose();
                    }
                    tabCtxt.dispose();
                }
                contextMap.remove(searchIDDisposed);
                CoreEventBus.INSTANCE.postSingleThreadAsync(new SRTabDisposedEvent(searchIDDisposed, searchIDDisposed.equals(mainSearchID)));
            }
        });

        //tabItem1.addListener(SWT.SELECTED, listener);

        Composite viewsArea = new Composite(tabFolder, SWT.NONE);
        tabItem1.setControl(viewsArea);
        viewsArea.setLayout(new FormLayout());
        newCtxt.set(Composite.class, viewsArea);

        StyledTable table = new StyledTable(viewsArea, SWT.BORDER | SWT.FULL_SELECTION);
        stylingEngine.setClassname(table, IEclipse4Constants.STYLING_CLASS_SR_STYLED_TABLE);
        FormData fd_table = new FormData();
        fd_table.top = new FormAttachment(0);
        fd_table.bottom = new FormAttachment(100, -25);
        fd_table.left = new FormAttachment(0);
        fd_table.right = new FormAttachment(100);
        table.setLayoutData(fd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        //Font tableFnt = new Font(table.getDisplay(), "Courier new", 11, SWT.NONE);
        //table.setFont(tableFnt);
        newCtxt.set(StyledTable.class, table);

        Label lblStatus = new Label(viewsArea, SWT.NONE);
        FormData fd_lblStatus = new FormData();
        fd_lblStatus.bottom = new FormAttachment(100);
        fd_lblStatus.top = new FormAttachment(100, -25);
        fd_lblStatus.left = new FormAttachment(0);
        fd_lblStatus.right = new FormAttachment(50);
        lblStatus.setLayoutData(fd_lblStatus);
        lblStatus.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NORMAL));
        lblStatus.setText("Initializing...");
        newCtxt.set(Label.class, lblStatus);

        createProgressBar(newCtxt, 0, false, false);
        srSashForm.layout();
        viewsArea.layout();
        //activeState = SearchResultActiveState.SEARCH_IN_PROGRESS;
    }

    protected void createProgressBar(IEclipseContext workCtxt, Integer maxProgress, Boolean isDeterminate, Boolean makeLayout) {
        if (workCtxt != null) {
            Composite viewsArea = workCtxt.get(Composite.class);
            ProgressBar progressBar;
            if (isDeterminate) {
                progressBar = new ProgressBar(viewsArea, SWT.BORDER);
                FormData fd_progressBar = new FormData();
                fd_progressBar.top = new FormAttachment(100, -25);
                fd_progressBar.left = new FormAttachment(50);
                fd_progressBar.bottom = new FormAttachment(100, -1);
                fd_progressBar.right = new FormAttachment(100, -2);
                progressBar.setLayoutData(fd_progressBar);
                progressBar.setMaximum(maxProgress);
            } else {
                progressBar = new ProgressBar(viewsArea, SWT.BORDER | SWT.INDETERMINATE);
                FormData fd_progressBar = new FormData();
                fd_progressBar.top = new FormAttachment(100, -25);
                fd_progressBar.left = new FormAttachment(50);
                fd_progressBar.bottom = new FormAttachment(100, -1);
                fd_progressBar.right = new FormAttachment(100, -2);
                progressBar.setLayoutData(fd_progressBar);
            }
            workCtxt.set(ProgressBar.class, progressBar);
            if (makeLayout) {
                viewsArea.layout();
                viewsArea.redraw();
            }
        }
    }


    public void addViewResult(final IComplexDisplayResult newResult, SearchInfo viewInfo) {
        if (viewInfo == null) {
            log.error("addViewResult viewInfo is null returning");
            return;
        }
        IEclipseContext workContext = contextMap.get(viewInfo.getSearchID());
        if (workContext != null) {
            StyledTable currTable = workContext.get(StyledTable.class);
            workContext.set(SearchInfo.class, viewInfo);
            if (!viewInfo.equals(mainSearchInfo)) {
                if (mainSearchInfo.getChildren() == null) {
                    mainSearchInfo.setChildren(new ArrayList<SearchInfo>());
                }
                mainSearchInfo.getChildren().add(viewInfo);
            }
            if ((currTable != null) && !newResult.getViewNames().isEmpty()
                    && (newResult.getViewItems() != null) && !newResult.getViewItems().isEmpty()) {

                TableColumn tableColumn = new TableColumn(currTable, SWT.LEAD);
                tableColumn.setText("#");
                workContext.set(CONTEXT_KEY_VIEW_NAMES, newResult.getViewNames());
                for (String colName : newResult.getViewNames()) {
                    TableColumn newCol = new TableColumn(currTable, SWT.NONE);
                    newCol.setText(colName);
                    newCol.setMoveable(true);
                    newCol.setResizable(true);
                    newCol.setData(DATA_COLUMN_VIEW_NAME, colName);
                    newCol.addSelectionListener(new SelectionAdapter() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            fillInTableItems(activeContext, (TableColumn)e.getSource(), true);
                        }
                    });
                }
                workContext.set(CONTEXT_KEY_TABLE_ROWS, newResult.getViewItems());
                currTable.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        StyledTableItem selectedTI = (StyledTableItem)e.item;
                        if ((selectedTI != null) && (activeContext != null)) {
                            activeContext.set(CONTEXT_KEY_SELECTED_ROW_INDEX, selectedTI.getTablePosition());
                        }
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        StyledTableItem selectedTI = (StyledTableItem)e.item;
                        if ((selectedTI != null) && (selectedTI.getData(DATA_SEARCH_RESULT_ITEM_KEY) != null)) {
                            IDRViewItem selectedItem = (IDRViewItem)selectedTI.getData(DATA_SEARCH_RESULT_ITEM_KEY);
                            setSelectionWithStyles(selectedItem.getStartPos(), selectedItem.getEndPos());
                        }
                    }
                });
                fillInTableItems(workContext, currTable.getColumn(1), false);
                stopCurrentProgress(workContext);
                setOOIForTable(currTable);
                workContext.set(SearchResultActiveState.class, SearchResultActiveState.SEARCH_COMPLETED);
            } else {
                log.info("No result items for result name=" + newResult.getSearchObjectName());
                disposeViewResult(viewInfo.getSearchID());
            }
        } else {
            log.error("addViewResult workContext NULL");
        }
    }

    public void disposeViewResult(String searchID) {
        IEclipseContext workContext = contextMap.get(searchID);
        if (workContext != null) {
            stopCurrentProgress(workContext);
            log.debug("disposeViewResult SearchID " + workContext.get(IEclipse4Constants.LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY));
            Composite currComp = workContext.get(Composite.class);
            currComp.dispose();
            CTabItem currTab = workContext.get(CTabItem.class);
            contextMap.remove(workContext.get(IEclipse4Constants.LOCAL_CONTEXT_SEARCH_RESULT_SEARCH_ID_KEY));
            workContext.dispose();
            currTab.dispose();
        } else {
            log.error("No view to dispose for searchID=" + searchID);
        }
    }

    protected void stopCurrentProgress(IEclipseContext workContext) {
        if (workContext != null) {
            ProgressBar progressBar = workContext.get(ProgressBar.class);
            if (progressBar != null) {
                Composite progressParent = progressBar.getParent();
                progressBar.dispose();
                workContext.remove(ProgressBar.class);
                progressParent.redraw();
            }
        }
    }

    public void stopProgress(String searchID) {
        IEclipseContext workContext = contextMap.get(searchID);
        if (workContext != null) {
            ProgressBar progressBar = workContext.get(ProgressBar.class);
            if (progressBar != null) {
                Composite progressParent = progressBar.getParent();
                progressBar.dispose();
                workContext.remove(ProgressBar.class);
                progressParent.redraw();
            }
        }
    }

    protected void fillInTableItems(IEclipseContext workContext, TableColumn sortColumn, Boolean performSort) {
        StyledTable table = workContext.get(StyledTable.class);
        List<IDRViewItem> rows = new ArrayList<>((List<IDRViewItem>)workContext.get(CONTEXT_KEY_TABLE_ROWS));
        if (performSort) {
            log.debug("Sorting by column " + sortColumn.getText());
            TableColumn currSortColumn = (TableColumn)workContext.get(CONTEXT_KEY_SORT_COLUMN);
            Integer currSortDirection = (Integer)workContext.get(CONTEXT_KEY_SORT_COLUMN_DIRECTION);

            Integer newSortDirection = 1;
            if ((currSortColumn != null) && (currSortColumn.equals(sortColumn))) {
                newSortDirection = -currSortDirection;
            }
            TableColumn[] allCols = table.getColumns();
            int newSortColIndex;
            for(newSortColIndex = 0; newSortColIndex < allCols.length; newSortColIndex++) {
                if (allCols[newSortColIndex].equals(sortColumn)) {
                    break;
                }
            }
            if (newSortColIndex == 0) {
                for(newSortColIndex = 0; newSortColIndex < allCols.length; newSortColIndex++) {
                    if (allCols[newSortColIndex].equals(currSortColumn)) {
                        break;
                    }
                }
                newSortDirection = -currSortDirection;
            }

            final Integer sortDirection = newSortDirection;
            final Integer sortColIndex = newSortColIndex;
            if (sortColIndex > 0) {
                log.debug("sortColIndex " + sortColIndex + " sortDirection " + sortDirection);
                /* using parallelStream for sorting doesn't make sense
                 * because the running time of sorting is small compared to disposing/creating table items.
                 */
                Collections.sort(rows, new Comparator<IDRViewItem>() {

                    @Override
                    public int compare(IDRViewItem o1, IDRViewItem o2) {
                        String col1 = o1.getAllViewText().get(sortColIndex - 1);
                        String col2 = o2.getAllViewText().get(sortColIndex - 1);
                        Collator collator = Collator.getInstance(Locale.getDefault());
                        int res = (collator.compare(col1, col2))*sortDirection;
                        //log.debug(" TableSort col1=" + col1 + " col2=" + col2 + " result=" + res);
                        return res;
                    }
                });
                table.setSortColumn(table.getColumn(sortColIndex));
                table.setSortDirection((sortDirection == 1)?SWT.UP:SWT.DOWN);
                workContext.set(CONTEXT_KEY_SORT_COLUMN, sortColumn);
                workContext.set(CONTEXT_KEY_SORT_COLUMN_INDEX, sortColIndex);
                workContext.set(CONTEXT_KEY_SORT_COLUMN_DIRECTION, sortDirection);
            } else {
                log.error("Error in sorting sort column index " + sortColIndex);
            }
        }
        for (StyledTableItem ti : table.getStyledItems()) {
            ti.dispose();
        }
        table.clearAll();
        int i = 1;
        for (IDRViewItem currItem : rows) {
            StyledTableItem newItem = new StyledTableItem(table, SWT.NONE);
            String[] rowStrings = new String[currItem.getAllViewText().size() + 1];
            rowStrings[0] = String.valueOf(i);
            System.arraycopy(currItem.getAllViewText().toArray(new String[] { "" }), 0,
                    rowStrings, 1, rowStrings.length - 1);
            newItem.setText(rowStrings);
            newItem.setData(DATA_SEARCH_RESULT_ITEM_KEY, currItem);
            i++;
        }
        for (TableColumn col : table.getColumns()) {
            col.pack();
        }
    }

    public void setResult(IComplexDisplayResult newResult, SearchInfo mainInfo, Boolean silent) {
        setMainSearchState(SearchResultActiveState.SEARCH_COMPLETED);
        if (newResult != null) {
            // 50 megabytes wrap size limit by default
            Integer maxLength = PerformanceUtils.getIntProperty(PERF_CONST_MAX_WRAP_LENGTH, 50);
            boolean makeWrap = false;
            textLength = newResult.getTextConcatLength();
            MPart thisPart = partContext.get(MPart.class);
            thisPart.setTooltip(thisPart.getTooltip() + " Size=" + String.format("%.2f", (textLength.doubleValue()*2)/(1024*1024)));
            if (maxLength.doubleValue() > (textLength.doubleValue()*2)/(1024*1024)) {
                makeWrap = true;
            }
            log.debug("WrapLimit " + maxLength + " usingWrap=" + makeWrap + " calcTextSize=" + (textLength.doubleValue()*2)/(1024*1024));
            addStyledText(makeWrap);
            styledText.setText(newResult.getTextConcat());
            setMainSearchInfo(mainInfo);
            setGlobalObjects(silent);
            addViewResult(newResult, mainInfo);
        } else {
            stopCurrentProgress(contextMap.get(mainInfo.getSearchID()));
            setSearchStatus(mainInfo.getSearchID(), "Objects Found 0");
            setMainSearchInfo(mainInfo);
        }
    }

    public void setResult(IDisplayResult newResult, SearchInfo mainInfo, Boolean silent) {
        setMainSearchState(SearchResultActiveState.SEARCH_COMPLETED);
        if (newResult != null) {
            // 50 megabytes wrap size limit by default
            Integer maxLength = PerformanceUtils.getIntProperty(PERF_CONST_MAX_WRAP_LENGTH, 50);
            boolean makeWrap = false;
            textLength = newResult.getTextConcatLength();
            MPart thisPart = partContext.get(MPart.class);
            thisPart.setTooltip(thisPart.getTooltip() + " Size=" + String.format("%.2f", (textLength.doubleValue()*2)/(1024*1024)));
            if (maxLength.doubleValue() > (textLength.doubleValue()*2)/(1024*1024)) {
                makeWrap = true;
            }
            log.debug("WrapLimit " + maxLength + " usingWrap=" + makeWrap + " calcTextSize=" + (textLength.doubleValue()*2)/(1024*1024));
            addStyledText(makeWrap);
            String textResult = newResult.getTextConcatWithCleanup();
            System.gc();
            styledText.setText(textResult);
            styledText.setFocus();
            setGlobalObjects(silent);
        }
        disposeViewResult(mainInfo.getSearchID());
        IEclipseContext thisSearchCtxt = contextMap.get(mainInfo.getSearchID());
        if (thisSearchCtxt != null) {
            thisSearchCtxt.set(SearchResultActiveState.class, SearchResultActiveState.SEARCH_COMPLETED);
        }
        setMainSearchInfo(mainInfo);
    }

    protected void setGlobalObjects(final Boolean installSilently) {
        if (window.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) {
            log.debug("setGlobalObjects " + " " + mainSearchInfo);
            final Map<String, GlobalOOIInfo> globalObjects = (Map<String, GlobalOOIInfo>) window.getContext()
                    .get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
            if ((globalObjects != null) &&  (!globalObjects.isEmpty())) {
                final ProgressStatusDialog globObjProgressDialog = ContextInjectionFactory.make(ProgressStatusDialog.class,
                        partContext);
                if (!installSilently) {
                    globObjProgressDialog.init(globalObjects.size(), false);
                    globObjProgressDialog.open();
                    globObjProgressDialog.setStatus("Setting Global Objects");
                }
                final AtomicInteger progressCount = new AtomicInteger(0);
                final int totalGlobObj = globalObjects.values().size();
                final CountDownLatch globOOILatch = new CountDownLatch(totalGlobObj);
                Runnable installGlobalOOIRun = new Runnable() {

                    @Override
                    public void run() {
                        for (final GlobalOOIInfo globObj : globalObjects.values()) {
                            CompletableFuture<Integer> globObjFuture = setStyleForPattern(globObj.getTextPattern(), globObj.getStyle(), false).getFuture();
                            if (globObjFuture != null) {
                                globObjFuture.handle((Integer arg0, Throwable t) -> {
                                    if (arg0 != null) {
                                        log.debug("setGlobalObjects installed " + globObj.getDisplayString() + " SearchInfo " + mainSearchInfo);
                                    } else {
                                        log.error("Error setting a global object ", t);
                                    }
                                    uiSynch.asyncExec(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (!installSilently) {
                                                    globObjProgressDialog.increaseProgress(1);
                                                }
                                                if (totalGlobObj == progressCount.incrementAndGet()) {
                                                    if (!installSilently) {
                                                        globObjProgressDialog.setStatus("Complete");
                                                        globObjProgressDialog.getShell().dispose();
                                                    }
                                                    styledText.redraw();
                                                }
                                            } finally {
                                                if (installSilently) {
                                                    globOOILatch.countDown();
                                                }
                                            }
                                        }
                                    });
                                    return null;
                                });
                            }
                        }
                        if (installSilently) {
                            try {
                                globOOILatch.await();
                            } catch (InterruptedException e) {
                                log.error("setGlobalObjects latch interrupted", e);
                                // Restore interrupted state in accordance with the Sonar rule squid:S2142
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                };
                Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().execute(installGlobalOOIRun);
            } else {
                log.debug("setGlobalObjects 0 global objects");
            }
        }
    }

    public void setOOIForTable(StyledTable st) {
        if (!styleOOICache.isEmpty()) {
            for (OOIInfo currOOI : styleOOICache.values()) {
                st.setStyle(currOOI.getTextPattern(), currOOI.getStyle());
            }
        }
    }

    public void setSearchStatus(String searchID, String newStatus) {
        if (contextMap.containsKey(searchID)) {
            IEclipseContext workCtxt = contextMap.get(searchID);
            if (workCtxt.get(Label.class) != null) {
                Label statusLabel = workCtxt.get(Label.class);
                statusLabel.setText(newStatus);
            }
        }
    }

    public void setSearchStatus(String searchID, String newStatus, Integer currentProgress, Integer maxProgress) {
        if (contextMap.containsKey(searchID)) {
            IEclipseContext workCtxt = contextMap.get(searchID);
            Label statusLabel = workCtxt.get(Label.class);
            if (statusLabel != null) {
                statusLabel.setText(newStatus);
            }
            ProgressBar progBar = workCtxt.get(ProgressBar.class);
            if (progBar != null) {
                if ((progBar.getStyle() & SWT.INDETERMINATE) == SWT.INDETERMINATE) {
                    progBar.dispose();
                    createProgressBar(workCtxt, maxProgress, true, true);
                }
                progBar = workCtxt.get(ProgressBar.class);
                progBar.setSelection(currentProgress);
            }
        }
    }

    public void setErrorStatus(String searchID, String newStatus) {
        if (contextMap.containsKey(searchID)) {
            IEclipseContext workCtxt = contextMap.get(searchID);
            Label statusLabel = workCtxt.get(Label.class);
            if (statusLabel != null) {
                statusLabel.setText(newStatus);
            }
            this.stopProgress(searchID);
        }
    }

    protected void addStyledText(Boolean wrap) {
        int styles = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
        GUIProperty textReadOnlyProp = (GUIProperty)GUIConfigController.INSTANCE.getGUIObjectRepository().get(
                new GUIConfigKey(GUIConfigObjectType.GUI_PROPERTY, "TextReadOnly"));
        boolean textReadOnly = false;
        if (textReadOnlyProp != null) {
            textReadOnly = Boolean.valueOf(textReadOnlyProp.getValue().toLowerCase());
        }
        if (textReadOnly) {
            styles |= SWT.READ_ONLY;
        }
        if (wrap) {
            styledText = new StyledText2(placeHolderComposite, styles | SWT.WRAP);
        } else {
            styledText = new StyledText2(placeHolderComposite, styles);
        }
        stylingEngine.setClassname(styledText, IEclipse4Constants.STYLING_CLASS_SR_STYLED_TEXT);
        styledText.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                partContext.set(IEclipse4Constants.CONTEXT_SEARCH_RESULT_TEXT_SELECTED, ((StyledText)e.getSource()).getSelectionText());
                partContext.set(IEclipse4Constants.CONTEXT_SEARCH_RESULT_SELECTION_START, ((StyledText)e.getSource()).getSelection().x);
                partContext.set(IEclipse4Constants.CONTEXT_SEARCH_RESULT_SELECTION_END, ((StyledText)e.getSource()).getSelection().y);
            }

        });
        menuService.registerContextMenu(styledText, IEclipse4Constants.APP_MODEL_POPUP_MENU_SEARCH_RESULT);
        placeHolderComposite.layout();
    }

    protected void updateLocalBookmarks() {
        window.getContext().set(IEclipse4Constants.CONTEXT_LOCAL_BOOKMARKS_LIST, new ArrayList<>(localBookmarks));
        window.getContext().set(IEclipse4Constants.CONTEXT_LOCAL_BOOKMARKS_LIST_CHANGED, true);
    }

    protected void updateLocalOOI() {
        Map<String, GlobalOOIInfo> fullGlobOOIMap = new HashMap<>();
        if (window.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST) != null) {
            Map<String, GlobalOOIInfo> globalObjList = (Map<String, GlobalOOIInfo>)window.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_LIST);
            fullGlobOOIMap.putAll(globalObjList);
        }
        if (window.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST) != null) {
            Map<String, GlobalOOIInfo> globalOOIPendList = (Map<String, GlobalOOIInfo>)window.getContext().get(IEclipse4Constants.CONTEXT_GLOBAL_OOI_PENDING_LIST);
            fullGlobOOIMap.putAll(globalOOIPendList);
        }
        Map<String,OOIInfo> localMap = new HashMap<>();
        if (!fullGlobOOIMap.isEmpty()) {
            for (Map.Entry<String, OOIInfo> localOOI : styleOOICache.entrySet()) {
                boolean isGlobal = false;
                if (fullGlobOOIMap.containsKey(localOOI.getValue().getTextPattern().pattern())) {
                    GlobalOOIInfo globOOI = fullGlobOOIMap.get(localOOI.getValue().getTextPattern().pattern());
                    if (globOOI.getStyle().equals(localOOI.getValue().getStyle())) {
                        isGlobal = true;
                    }
                }
                if (!isGlobal) {
                    localMap.put(localOOI.getKey(), localOOI.getValue());
                }
            }
        } else {
            localMap.putAll(styleOOICache);
        }
        window.getContext().set(IEclipse4Constants.CONTEXT_LOCAL_OOI_LIST, localMap);
        window.getContext().set(IEclipse4Constants.CONTEXT_LOCAL_OOI_LIST_CHANGED, true);
    }

    public void addSelectionToLocalBookmarks(String bkmName, boolean updateGlobalList) {
        if (bkmName != null) {
            NamedBookmark newBkm = new NamedBookmark(bkmName,
                    styledText.getSelectionWithStyleRanges().x, styledText.getSelectionWithStyleRanges().y);
            log.info("addSelectionToLocalBookmarks SearchID=" + mainSearchInfo.getSearchID() + " " + newBkm);
            localBookmarks.add(newBkm);
            if (updateGlobalList) {
                updateLocalBookmarks();
            }
        }
    }

    public void addToLocalBookmarks(NamedBookmark newBookmark, boolean updateGlobalList) {
        if ((newBookmark != null) && (newBookmark.getBookmarkName() != null) && (newBookmark.getStartPos() != null) && (newBookmark.getEndPos() != null)){
            if ((newBookmark.getStartPos() >= 0) && (newBookmark.getStartPos() <= textLength)
                    && (newBookmark.getEndPos() >= 0) && (newBookmark.getEndPos() <= textLength)) {
                log.info("addToLocalBookmarks SearchID=" + mainSearchInfo.getSearchID() + " " + newBookmark);
                localBookmarks.add(newBookmark);
                if (updateGlobalList) {
                    updateLocalBookmarks();
                }
            } else {
                log.error("addToLocalBookmarks not in the text range " + newBookmark);
            }
        } else {
            log.error("addToLocalBookmarks null values passed bkmName=" + newBookmark);
        }
    }

    public void removeLocalBookmark(UUID bkmID, boolean updateGlobalList) {
        if ((bkmID != null) && (localBookmarks != null) && (!localBookmarks.isEmpty())) {
            NamedBookmark remBkm = null;
            for (NamedBookmark currBkm : localBookmarks) {
                if (bkmID.equals(currBkm.getId())) {
                    remBkm = currBkm;
                    break;
                }
            }
            if (remBkm != null) {
                localBookmarks.remove(remBkm);
            }
            if (updateGlobalList) {
                updateLocalBookmarks();
            }
        }
    }

    public FutureProgressMonitor<Integer> setStyleForPattern(final Pattern textPattern, final TextStyle newStyle, boolean mergeStyles) {
        log.debug("setStyleForPattern Text=" + textPattern + " newStyle=" + newStyle + " SearchInfo " + mainSearchInfo);
        if (getMainSearchState() != SearchResultActiveState.SEARCH_COMPLETED) {
            log.debug("setBackgroundColor search not completed. Part " + partContext.get(MPart.class).getLabel());
            CompletableFuture<Integer> resultFuture = CompletableFuture.completedFuture(0);
            //resultFuture.set(0);
            FutureProgressMonitor<Integer> returnMonitor = new FutureProgressMonitor<>();
            returnMonitor.setFuture(resultFuture);
            return returnMonitor;
        }
        if ((textPattern != null) && (textPattern.pattern().length() > 0)) {
            if (styleOOICache.containsKey(textPattern.pattern())) {
                if (styleOOICache.get(textPattern.pattern()).getStyle().equals(newStyle)) {
                    log.debug("Part " + partContext.get(MPart.class).getLabel()
                            + " Setting the same color for " + textPattern);
                    CompletableFuture<Integer> resultFuture = CompletableFuture.completedFuture(0);
                    //resultFuture.set(0);
                    FutureProgressMonitor<Integer> returnMonitor = new FutureProgressMonitor<>();
                    returnMonitor.setFuture(resultFuture);
                    return returnMonitor;
                } else if (mergeStyles) {
                    TextStyle currStyle = styleOOICache.get(textPattern.pattern()).getStyle();
                    if ((newStyle.font == null) && (currStyle.font != null)) {
                        newStyle.font = currStyle.font;
                    }
                    if ((newStyle.foreground == null) && (currStyle.foreground != null)) {
                        newStyle.foreground = currStyle.foreground;
                    }
                    if ((newStyle.background == null) && (currStyle.background != null)) {
                        newStyle.background = currStyle.background;
                    }
                }
            }

            int oneTableProgress = 0;
            if (tabFolder.getItemCount() > 0) {
                oneTableProgress = (int)((double)textLength/(double)tabFolder.getItemCount());
            }

            final int totalProgressLength = textLength.intValue() + oneTableProgress*tabFolder.getItemCount();
            final FutureProgressMonitor<Integer> returnMonitor = new FutureProgressMonitor<>();
            returnMonitor.setTotalProgress(totalProgressLength);
            returnMonitor.setCurrentProgress(0);

            Supplier<Integer> styleRangeBkgr = new Supplier<Integer>() {

                @Override
                public Integer get() {
                    final List<Matcher> matcherList = new ArrayList<>(1);
                    uiSynch.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            //textLengthList.add(styledText.getText().length());
                            matcherList.add(textPattern.matcher(styledText.getText()));
                        }
                    });
                    final Matcher mt = matcherList.get(0);
                    int srNumber = 0;
                    int totalSelectedStrings = 0;
                    List<StyleRange> srInProgress = new ArrayList<>();
                    final List<StyleRange> srToSet = new ArrayList<>();
                    //int gcCount = 0;
                    while (mt.find()) {
                        final int currPos = mt.end();
                        // later set the foreground color to the color of the styled text
                        srInProgress.add(new StyleRange(mt.start(), mt.end() - mt.start(), newStyle.foreground, newStyle.background));
                        srNumber++;
                        totalSelectedStrings++;
                        if (srNumber >= 100) {
                            srToSet.addAll(srInProgress);
                            returnMonitor.setCurrentProgress(currPos);
                            uiSynch.syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    if (!styledText.isDisposed()) {
                                        int gcCount = 0;
                                        for (StyleRange styleRange : srToSet) {
                                            // set the correct foreground before setting the style range
                                            styledText.setStyleRange(styleRange);
                                            gcCount++;
                                            if (gcCount > gcMaxCount) {
                                                System.gc();
                                                gcCount = 0;
                                            }
                                        }
                                        styledText.redraw();
                                    } else {
                                        log.warn("setBackgroundColor styledText disposed not setting style ranges");
                                    }
                                }
                            });
                            srInProgress.clear();
                            srToSet.clear();
                            srNumber = 0;
                            if (styledText.isDisposed()) {
                                log.warn("setBackgroundColor styledText disposed leaving the loop");
                                break;
                            }
                            System.gc();
                        }
                    }
                    srToSet.addAll(srInProgress);
                    returnMonitor.setCurrentProgress(totalProgressLength);
                    uiSynch.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (!styledText.isDisposed()) {
                                int gcCount = 0;
                                for (StyleRange styleRange : srToSet) {
                                    styledText.setStyleRange(styleRange);
                                    gcCount++;
                                    if (gcCount > gcMaxCount) {
                                        System.gc();
                                        gcCount = 0;
                                    }
                                }
                                styledText.redraw();
                            } else {
                                log.warn("setBackgroundColor styledText disposed not setting final style ranges");
                            }
                            for (Map.Entry<String, IEclipseContext> currCtxtEntry : contextMap.entrySet()) {
                                IEclipseContext currCtxt = currCtxtEntry.getValue();
                                if (currCtxt.get(SearchResultActiveState.class) == SearchResultActiveState.SEARCH_COMPLETED) {
                                    StyledTable st = currCtxt.get(StyledTable.class);
                                    if (!st.isDisposed()) {
                                        st.setStyle(textPattern, newStyle);
                                    } else {
                                        log.warn("setBackgroundColor table already disposed not setting style");
                                    }
                                    System.gc();
                                }
                            }
                            styleOOICache.put(textPattern.pattern(), new OOIInfo(textPattern, newStyle));
                            if (window.getContext().get(IServiceConstants.ACTIVE_PART) != null) {
                                MPart activePart = (MPart)window.getContext().get(IServiceConstants.ACTIVE_PART);
                                if (activePart.equals(partContext.get(MPart.class))) {
                                    updateLocalOOI();
                                }
                            }
                        }
                    });

                    System.gc();
                    return totalSelectedStrings;
                }
            };
            //returnMonitor.setFuture(Eclipse4GUIBridge.INSTANCE.getGuiFixedTPExecutor().submit(styleRangeBkgr));
            returnMonitor.setFuture(CompletableFuture.supplyAsync(styleRangeBkgr, Eclipse4GUIBridge.INSTANCE.getGuiFixedTPExecutor()));
            return returnMonitor;
        }
        // general ending if the pattern is incorrect
        log.error("No text to select");
        CompletableFuture<Integer> resultFuture = CompletableFuture.completedFuture(0);
        //resultFuture.set(0);
        FutureProgressMonitor<Integer> returnMonitor = new FutureProgressMonitor<>();
        returnMonitor.setFuture(resultFuture);
        return returnMonitor;
    }

    public FindRegexAnalysisResult findRegex(String patternStr, int aDirection,
            Boolean wrap, Boolean caseInsensitive) throws PatternSyntaxException {
        log.info("findRegex str=" + patternStr + " direction=" + aDirection
                + " wrap=" + wrap + " caseInsensitive=" + caseInsensitive);
        int startIndex = -1;
        int endIndex = -1;
        FindRegexAnalysisResult result = new FindRegexAnalysisResult();
        String selectedText = styledText.getSelectionText();
        int flags = 0;
        if (caseInsensitive) {
            flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
        Pattern pt = Pattern.compile(patternStr, flags);
        Matcher mt = pt.matcher(styledText.getText());
        if (aDirection == SWT.DOWN) {
            log.debug("findRegex Forward region " + styledText.getCaretOffset() + " " + styledText.getText().length());
            mt.region(styledText.getCaretOffset(), styledText.getText().length());
            if (mt.find()) {
                startIndex = mt.start();
                endIndex = mt.end();
            }
        } else if (aDirection == SWT.UP) {
            if ((selectedText != null) && (!"".equals(selectedText))) {
                log.debug("findRegex Backward selection region " + styledText.getCaretOffset() + " " + styledText.getText().length());
                mt.region(0, styledText.getCaretOffset() - selectedText.length() - 1);
            } else {
                log.debug("findRegex Backward region " + styledText.getCaretOffset() + " " + styledText.getText().length());
                mt.region(0, styledText.getCaretOffset());
            }
            int aboveObjects = 0;
            while (mt.find()) {
                startIndex = mt.start();
                endIndex = mt.end();
                aboveObjects++;
            }
            result.setAboveObjects(aboveObjects);
        }

        if ((startIndex < 0) && (wrap)) {
            if (aDirection == SWT.DOWN) {
                log.debug("findRegex Wrap Forward region " + 0 + " " + styledText.getText().length());
                mt.region(0, styledText.getText().length());
                if (mt.find()) {
                    startIndex = mt.start();
                    endIndex = mt.end();
                }
            } else if (aDirection == SWT.UP) {
                log.debug("findRegex Wrap Backward region " + 0 + " " + styledText.getText().length());
                mt.region(0, styledText.getText().length());
                while (mt.find()) {
                    startIndex = mt.start();
                    endIndex = mt.end();
                }
            }
        }

        if (startIndex >= 0) {
            styledText.setCaretOffset(startIndex);
            styledText.setSelection(startIndex, endIndex);
            result.setPositionStart(startIndex);
        }
        System.gc();
        return result;
    }

    public FindRegexAnalysisResult findRegexAnalysis(String patternStr, int aDirection,
            Boolean wrap, Boolean caseInsensitive) throws PatternSyntaxException {
        log.info("findRegexAnalysis str=" + patternStr + " direction=" + aDirection
                + " wrap=" + wrap + " caseInsensitive=" + caseInsensitive);
        FindRegexAnalysisResult result = new FindRegexAnalysisResult();
        Point selPoint = styledText.getSelection();
        log.info("Selection point " + selPoint);
        int flags = 0;
        if (caseInsensitive) {
            flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
        Pattern pt = Pattern.compile(patternStr, flags);
        Matcher mt = pt.matcher(styledText.getText());
        int caretPos = styledText.getCaretOffset();
        FindRegexAnalysisResult firstResult = null;
        FindRegexAnalysisResult lastResult = new FindRegexAnalysisResult();
        boolean resFound = false;
        while (mt.find()) {
            if (firstResult == null) {
                firstResult = new FindRegexAnalysisResult();
                firstResult.setPositionStart(mt.start());
                firstResult.setPositionEnd(mt.end());
                firstResult.setFoundStr(mt.group());
            }
            lastResult.setPositionStart(mt.start());
            lastResult.setPositionEnd(mt.end());
            lastResult.setFoundStr(mt.group());
            if ((mt.start() == selPoint.x) && (mt.end() == selPoint.y)) {
                if (aDirection == SWT.DOWN) {
                    caretPos = mt.end();
                } else if (aDirection == SWT.UP) {
                    caretPos = mt.start();
                    result.increaseBelowObjects();
                }
            }
            result.setObjectsCount(result.getObjectsCount() + 1);
            if (aDirection == SWT.DOWN) {
                if ((!resFound) && (mt.start() >= caretPos)) {
                    result.setPositionStart(mt.start());
                    result.setPositionEnd(mt.end());
                    result.setFoundStr(mt.group());
                    resFound = true;
                    continue;
                }
                if (mt.start() < caretPos) {
                    result.increaseAboveObjects();
                } else if (mt.start() > caretPos) {
                    result.increaseBelowObjects();
                }
            } else if (aDirection == SWT.UP) {
                if (mt.start() < caretPos) {
                    if (!resFound) {
                        result.setAboveObjects(-1);
                    }
                    result.setPositionStart(mt.start());
                    result.setPositionEnd(mt.end());
                    result.setFoundStr(mt.group());
                    resFound = true;
                }
                if (mt.start() < caretPos) {
                    result.increaseAboveObjects();
                } else if (mt.start() > caretPos) {
                    result.increaseBelowObjects();
                }
            }
        }

        if (wrap && (result.getFoundStr() == null)) {
            if ((aDirection == SWT.DOWN) && (firstResult != null) && (firstResult.getFoundStr() != null)) {
                result.setPositionStart(firstResult.getPositionStart());
                result.setPositionEnd(firstResult.getPositionEnd());
                result.setFoundStr(firstResult.getFoundStr());
                result.setBelowObjects(result.getObjectsCount() - 1);
                result.setAboveObjects(0);
            } else if ((aDirection == SWT.UP) && (lastResult.getFoundStr() != null)) {
                result.setPositionStart(lastResult.getPositionStart());
                result.setPositionEnd(lastResult.getPositionEnd());
                result.setFoundStr(lastResult.getFoundStr());
                result.setBelowObjects(0);
                result.setAboveObjects(result.getObjectsCount() - 1);
            }
        }

        if (result.getFoundStr() != null) {
            styledText.setCaretOffset(result.getPositionStart());
            styledText.setSelection(result.getPositionStart(), result.getPositionEnd());
        }

        System.gc();
        return result;
    }

    public void removeColorAtCursor() {
        int cursorIndex = styledText.getCaretOffset();
        List<StyleRange> allStyleRanges = new ArrayList<>(Arrays.asList(styledText.getStyleRanges()));
        List<StyleRange> removeStyleRange = new ArrayList<>();
        List<Color> currColors = new ArrayList<>();
        List<String> selItemsList = new ArrayList<>();
        for (StyleRange sr : allStyleRanges) {
            if ((cursorIndex >= sr.start) && (cursorIndex <= (sr.start + sr.length))) {
                if (!currColors.contains(sr.background)) {
                    currColors.add(sr.background);
                    break;
                }
            }
        }
        for (StyleRange sr : allStyleRanges) {
            if (currColors.contains(sr.background)) {
                removeStyleRange.add(sr);
                selItemsList.add(styledText.getText().substring(sr.start, sr.start + sr.length));
            }
        }
        allStyleRanges.removeAll(removeStyleRange);
        styledText.replaceStyleRanges(0, styledText.getText().length(), allStyleRanges.toArray(new StyleRange[]{}));
        for (Map.Entry<String, IEclipseContext> currCtxtEntry : contextMap.entrySet()) {
            IEclipseContext currCtxt = currCtxtEntry.getValue();
            StyledTable st = currCtxt.get(StyledTable.class);
            for (String removeStr : selItemsList) {
                st.removeStyle(removeStr, false);
            }
        }
        for (String selItem : selItemsList) {
            Pattern pt = null;
            for (Map.Entry<String,OOIInfo> cachedOOI : styleOOICache.entrySet()) {
                pt = Pattern.compile(cachedOOI.getKey());
                Matcher mt = pt.matcher(selItem);
                if (mt.matches()) {
                    styleOOICache.remove(cachedOOI.getKey());
                }
            }
        }
        updateLocalOOI();
    }

    public CompletableFuture<Void> removeStyleForPattern(final Pattern removePt) {
        log.debug("removeStyleForPattern " + removePt);
        if (getMainSearchState() != SearchResultActiveState.SEARCH_COMPLETED) {
            CompletableFuture<Void> resultFuture = CompletableFuture.completedFuture(null);
            //resultFuture.set(null);
            return resultFuture;
        }
        Runnable remCallable = new Runnable() {
            @Override
            public void run() {
                uiSynch.syncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<StyleRange> allStyleRanges = new ArrayList<>(Arrays.asList(styledText.getStyleRanges()));
                            List<StyleRange> removeStyleRange = new ArrayList<>();
                            for (StyleRange sr : allStyleRanges) {
                                Matcher mt = removePt.matcher(styledText.getText().substring(sr.start, sr.start + sr.length));
                                if (mt.matches()) {
                                    removeStyleRange.add(sr);
                                }
                            }
                            allStyleRanges.removeAll(removeStyleRange);
                            styledText.replaceStyleRanges(0, styledText.getText().length(), allStyleRanges.toArray(new StyleRange[]{}));
                            for (Map.Entry<String, IEclipseContext> currCtxtEntry : contextMap.entrySet()) {
                                IEclipseContext currCtxt = currCtxtEntry.getValue();
                                if (currCtxt.get(SearchResultActiveState.class) == SearchResultActiveState.SEARCH_COMPLETED) {
                                    StyledTable st = currCtxt.get(StyledTable.class);
                                    st.removeStyle(removePt.pattern(), true);
                                }
                            }
                        } catch (SWTException e) {
                            log.error("removeStyleForPattern SWT Exception",e);
                        }
                        styleOOICache.remove(removePt.pattern());
                        updateLocalOOI();
                    }
                });
                //return null;
            }
        };
        return CompletableFuture.runAsync(remCallable, Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor());
        //return Eclipse4GUIBridge.INSTANCE.getGuiCachedTPExecutor().submit(remCallable);
    }

    public void saveActiveTextToFile(final File saveFile) {
        uiSynch.syncExec(new Runnable() {

            @Override
            public void run() {
                if (styledText.isFocusControl()) {
                    try (PrintWriter pw = new PrintWriter(saveFile)) {
                        pw.print(styledText.getText());
                    } catch (FileNotFoundException e) {
                        log.error("",e);
                    }
                    System.gc();
                }
            }
        });
    }

    public void removeAllColors() {
        styledText.setStyleRanges(new StyleRange[] {});
        styleOOICache.clear();
        updateLocalOOI();
    }

    public void copyToClipboard() {
        styledText.copy();
    }

    public String getActiveViewTitle() {
        String actTitle = null;
        if ((tabFolder != null) && (tabFolder.getSelection() != null)) {
            CTabItem activeItem = tabFolder.getSelection();
            actTitle = activeItem.getText();
        } else {
            log.warn("No tabs not setting the active view title");
        }
        return actTitle;
    }

    public void setActiveViewTitle(String newViewTitle) {
        if ((tabFolder != null) && (tabFolder.getSelection() != null)) {
            CTabItem activeItem = tabFolder.getSelection();
            activeItem.setText(newViewTitle);
        } else {
            log.warn("No tabs not setting the active view title");
        }
    }

    public Integer getViewCount() {
        return tabFolder.getItemCount();
    }

    public String getText() {
        if (styledText != null) {
            return styledText.getText();
        } else {
            return null;
        }
    }

    public int getCaretPosition() {
        int res = 0;
        if (styledText != null) {
            res = styledText.getCaretOffset();
        }
        return res;
    }

    public void setCaretPosition(Integer newPos) {
        if ((styledText != null) && (newPos != null)) {
            styledText.setSelection(newPos);
        }
    }

    /* Use setSelectionWithStyles instead of simple selection
    */
    public void setSelectionWithStyles(Integer start, Integer end) {
        if ((start >= 0) && (start < styledText.getText().length())
                && (end >= 0) && (end <= styledText.getText().length())) {
            // a trick to scroll the selection into view first select the text
            // to scroll in into view then put the selection at the beginning
            styledText.setSelection(start, end);
            // put to the clipboard
            styledText.copy();
            // scroll to the beginning if the selection is too long it is scrolled to the end
            styledText.setSelection(start);
            // selection with style ranges marks with selection foreground and background colors
            // only the text that is not assigned any StyleRange
            styledText.setSelectionWithStyleRanges(start, end);
            //copyToClipboard(selectedItem.getStartPos(), selectedItem.getEndPos());
            styledText.setFocus();
        }
    }

    public int getSelectedIndex(String searchID) {
        int res = -1;
        if ((searchID != null) && (contextMap.containsKey(searchID))) {
            IEclipseContext ctxt = contextMap.get(searchID);
            if (ctxt.get(StyledTable.class) != null) {
                res = ctxt.get(StyledTable.class).getSelectionIndex();
            }
        }
        return res;
    }

    public void setSelectedIndex(String searchID, Integer newSelIndex) {
        if ((searchID != null) && (newSelIndex != null) && (contextMap.containsKey(searchID))) {
            IEclipseContext ctxt = contextMap.get(searchID);
            log.debug("setSelectedIndex " + newSelIndex + " soName " + ctxt.get(SearchInfo.class).getSearchObjectName());
            if (ctxt.get(StyledTable.class) != null) {
                StyledTable searchTable = ctxt.get(StyledTable.class);
                if ((newSelIndex >= 0) && (newSelIndex < searchTable.getItemCount())) {
                    searchTable.setSelection(newSelIndex);
                    activeContext.set(CONTEXT_KEY_SELECTED_ROW_INDEX, newSelIndex);
                }
            }
        }
    }

    public Map<String, OOIInfo> getStyleOOICache() {
        return styleOOICache;
    }

    public List<NamedBookmark> getLocalBookmarks() {
        return localBookmarks;
    }

    public String getLabel() {
        if (partContext.get(MPart.class) != null) {
            return partContext.get(MPart.class).getLabel();
        } else {
            return null;
        }
    }

    public boolean isSearchTabAvailable(String searchID) {
        return contextMap.containsKey(searchID);
    }

    public String getTabTitle(String searchID) {
        String title = null;
        IEclipseContext tabCtxt = contextMap.get(searchID);
        if (tabCtxt != null) {
            CTabItem tab = tabCtxt.get(CTabItem.class);
            if (tab != null) {
                title = tab.getText();
            }
        }
        return title;
    }

    public List<String> getViewNames(String searchID) {
        List<String> result = null;
        if (contextMap.containsKey(searchID)) {
            IEclipseContext thisCtxt = contextMap.get(searchID);
            result = (List<String>)thisCtxt.get(CONTEXT_KEY_VIEW_NAMES);
        }
        return result;
    }

    public SearchInfo getSearchInfo() {
        SearchInfo returnInfo = null;
        if (activeContext != null) {
            returnInfo = activeContext.get(SearchInfo.class);
        }
        return returnInfo;
    }

    public void setSearchInfo(SearchInfo searchInfo) {
        IEclipseContext thisSearchCtxt = contextMap.get(searchInfo.getSearchID());
        if (thisSearchCtxt != null) {
            thisSearchCtxt.set(SearchInfo.class, searchInfo);
        }
    }

    public SearchInfo getMainSearchInfo() {
        return mainSearchInfo;
    }

    public void setMainSearchInfo(SearchInfo mainSearchInfo) {
        log.debug("setMainSearchInfo " + mainSearchInfo);
        this.mainSearchInfo = mainSearchInfo;
        setSearchInfo(mainSearchInfo);
    }

    public SearchResultActiveState getMainSearchState() {
        return activeState;
    }

    public void setMainSearchState(SearchResultActiveState mainSearchState) {
        this.activeState = mainSearchState;
    }

    public ECommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ECommandService commandService) {
        this.commandService = commandService;
    }

    public EHandlerService getHandlerService() {
        return handlerService;
    }

    public void setHandlerService(EHandlerService handlerService) {
        this.handlerService = handlerService;
    }

    @PreDestroy
    public void dispose() {
        activeState = SearchResultActiveState.DISPOSED;
        CoreEventBus.INSTANCE.postSingleThreadAsync(new SRPartDisposedEvent(mainSearchID));
    }

    @Focus
    public void setFocus() {
        if (activeContext != null) {
            StyledTable activeTable = (activeContext.get(StyledTable.class));
            if (activeTable != null) {
                activeTable.setFocus();
                if (activeContext.get(CONTEXT_KEY_SELECTED_ROW_INDEX) != null) {
                    setSelectedIndex((String)activeContext.get(CONTEXT_KEY_SEARCH_ID), (Integer)activeContext.get(CONTEXT_KEY_SELECTED_ROW_INDEX));
                }
            }
        }
        window.getContext().set(IEclipse4Constants.CONTEXT_SEARCH_RESULT_ACTIVE_TAB, this);
        updateLocalOOI();
        updateLocalBookmarks();
    }
}
