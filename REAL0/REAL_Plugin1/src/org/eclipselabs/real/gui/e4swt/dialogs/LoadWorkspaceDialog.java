package org.eclipselabs.real.gui.e4swt.dialogs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipselabs.real.core.util.IRealCoreConstants;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.persist.ReplaceableParamKeyPersist;
import org.eclipselabs.real.gui.e4swt.persist.ReplaceableParamPersist;
import org.eclipselabs.real.gui.e4swt.persist.SearchResultCurrentInfo;
import org.eclipselabs.real.gui.e4swt.persist.SearchResultPartInfo;

@Creatable
public class LoadWorkspaceDialog extends Dialog {

    @Inject
    IStylingEngine stylingEngine;

    protected DialogResult<List<SearchResultPartInfo>> result = new DialogResult<List<SearchResultPartInfo>>();
    protected Shell shell;
    private CTabFolder tabfldWorkspace;
    private List<SearchResultPartInfo> allResultsInitial;
    private List<EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>>> workList;

    private static String CONTEXT_KEY_PART_INFO = "PartInfo";
    private static String CONTEXT_KEY_CURRENT_SEARCH_INFO = "CurrSearchInfo";

    private static class EnabledChildWrapper<T> {
        private T obj;
        private boolean enabled;

        public EnabledChildWrapper() { }

        public EnabledChildWrapper(T o, boolean enb) {
            obj = o;
            enabled = enb;
        }

        public T getObj() {
            return obj;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    private static class EnabledParentWrapper<T, C> {

        private T obj;
        private boolean enabled;
        private List<C> children = new ArrayList<C>();

        public EnabledParentWrapper() { }

        public EnabledParentWrapper(T o, boolean enb) {
            obj = o;
            enabled = enb;
        }

        public T getObj() {
            return obj;
        }

        public void setObj(T obj) {
            this.obj = obj;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<C> getChildren() {
            return children;
        }

        public void setChildren(List<C> children) {
            this.children = children;
        }
    }

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public LoadWorkspaceDialog(Shell parent, int style) {
        super(parent, style);
        setText("Custom Load Workspace");
    }

    @Inject
    public LoadWorkspaceDialog(@Named(IServiceConstants.ACTIVE_SHELL) final Shell parent) {
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
        setText("Custom Load Workspace");
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public DialogResult<List<SearchResultPartInfo>> open() {
        if (allResultsInitial != null) {
            workList = convertInitialResults(allResultsInitial);
        }
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (result.getAction() == SWT.OK) {
            result.setResult(getFinalResults(workList));
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE);
        shell.setSize(800, 600);
        shell.setText(getText());
        shell.setLayout(new FormLayout());

        tabfldWorkspace = new CTabFolder(shell, SWT.NONE);
        FormData fd_tabfldWorkspace = new FormData();
        fd_tabfldWorkspace.bottom = new FormAttachment(100, -41);
        fd_tabfldWorkspace.right = new FormAttachment(100);
        fd_tabfldWorkspace.top = new FormAttachment(0);
        fd_tabfldWorkspace.left = new FormAttachment(0);
        tabfldWorkspace.setLayoutData(fd_tabfldWorkspace);
        stylingEngine.setClassname(tabfldWorkspace, IEclipse4Constants.STYLING_CLASS_CTABFOLDER_LOAD_WS);

        for (EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>> currPart : workList) {
            createPartInfo(currPart, tabfldWorkspace);
        }
        if (tabfldWorkspace.getItemCount() > 0) {
            tabfldWorkspace.setSelection(0);
        }

        Composite compButtons = new Composite(shell, SWT.NONE);
        compButtons.setLayout(new FormLayout());
        FormData fd_compButtons = new FormData();
        fd_compButtons.top = new FormAttachment(100, -40);
        fd_compButtons.right = new FormAttachment(100, -1);
        fd_compButtons.bottom = new FormAttachment(100);
        fd_compButtons.left = new FormAttachment(0, 1, 0);
        compButtons.setLayoutData(fd_compButtons);

        Button btnOK = new Button(compButtons, SWT.NONE);
        FormData fd_btnOK = new FormData();
        fd_btnOK.top = new FormAttachment(10);
        fd_btnOK.right = new FormAttachment(40);
        fd_btnOK.bottom = new FormAttachment(90);
        fd_btnOK.left = new FormAttachment(20);
        btnOK.setLayoutData(fd_btnOK);
        btnOK.setText("OK");
        btnOK.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                result.setAction(SWT.OK);
                shell.close();
            }

        });

        Button btnCancel = new Button(compButtons, SWT.NONE);
        FormData fd_btnCancel = new FormData();
        fd_btnCancel.top = new FormAttachment(10);
        fd_btnCancel.right = new FormAttachment(80);
        fd_btnCancel.left = new FormAttachment(60);
        fd_btnCancel.bottom = new FormAttachment(90);
        btnCancel.setLayoutData(fd_btnCancel);
        btnCancel.setText("Cancel");

        btnCancel.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                result.setAction(SWT.CANCEL);
                shell.close();
            }

        });

    }

    private List<EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>>> convertInitialResults(List<SearchResultPartInfo> initialList) {
        List<EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>>> newWorkList = new ArrayList<>();
        for (SearchResultPartInfo currPart : initialList) {
            EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>> newParentWrapper =
                    new EnabledParentWrapper<>(currPart, true);
            if (currPart.getCurrentSearchInfos() != null) {
                for (SearchResultCurrentInfo childRes : currPart.getCurrentSearchInfos()) {
                    EnabledChildWrapper<SearchResultCurrentInfo> newChild = new EnabledChildWrapper<SearchResultCurrentInfo>(childRes, true);
                    newParentWrapper.getChildren().add(newChild);
                }
            }
            newWorkList.add(newParentWrapper);
        }
        return newWorkList;
    }

    private List<SearchResultPartInfo> getFinalResults(List<EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>>> finalWorkList) {
        List<SearchResultPartInfo> resList = new ArrayList<SearchResultPartInfo>();
        for (EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>> currParent : finalWorkList) {
            if (currParent.isEnabled()) {
                SearchResultPartInfo parentInfo = currParent.getObj();
                if (parentInfo.getCurrentSearchInfos() != null) {
                    parentInfo.getCurrentSearchInfos().clear();
                    for (EnabledChildWrapper<SearchResultCurrentInfo> currChild : currParent.getChildren()) {
                        if (currChild.isEnabled()) {
                            parentInfo.getCurrentSearchInfos().add(currChild.getObj());
                        }
                    }
                }
                resList.add(parentInfo);
            }
        }
        return resList;
    }

    protected CTabItem createPartInfo(EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>> partInfoWrapper, CTabFolder tabFld) {
        CTabItem tbtmPartInfo = new CTabItem(tabFld, SWT.NONE);
        tbtmPartInfo.setText(partInfoWrapper.getObj().getSearchObjectName());
        tbtmPartInfo.setShowClose(false);
        stylingEngine.setClassname(tbtmPartInfo, IEclipse4Constants.STYLING_CLASS_CTABITEM_LOAD_WS);

        Composite compTabControl = new Composite(tabFld, SWT.NONE);
        tbtmPartInfo.setControl(compTabControl);
        compTabControl.setLayout(new FormLayout());

        Button btnchPartEnabled = new Button(compTabControl, SWT.CHECK);
        FormData fd_btnchPartEnabled = new FormData();
        fd_btnchPartEnabled.bottom = new FormAttachment(10);
        fd_btnchPartEnabled.right = new FormAttachment(100);
        fd_btnchPartEnabled.top = new FormAttachment(0);
        fd_btnchPartEnabled.left = new FormAttachment(0, 10);
        btnchPartEnabled.setLayoutData(fd_btnchPartEnabled);
        btnchPartEnabled.setText("Enabled");
        btnchPartEnabled.setData(CONTEXT_KEY_PART_INFO, partInfoWrapper);
        btnchPartEnabled.setSelection(true);
        btnchPartEnabled.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Button thisBtn = (Button)e.getSource();
                EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>> currObj
                     = (EnabledParentWrapper<SearchResultPartInfo, EnabledChildWrapper<SearchResultCurrentInfo>>)thisBtn.getData(CONTEXT_KEY_PART_INFO);
                if (currObj != null) {
                    currObj.setEnabled(thisBtn.getSelection());
                }
                super.widgetSelected(e);
            }
        });
        stylingEngine.setClassname(btnchPartEnabled, IEclipse4Constants.STYLING_CLASS_CHECKBOX_WHITE);

        Text txtPartText = new Text(compTabControl, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
        FormData fd_txtPartText = new FormData();
        fd_txtPartText.bottom = new FormAttachment(55);
        fd_txtPartText.right = new FormAttachment(100);
        fd_txtPartText.top = new FormAttachment(10);
        fd_txtPartText.left = new FormAttachment(0);
        txtPartText.setLayoutData(fd_txtPartText);
        stylingEngine.setClassname(txtPartText, IEclipse4Constants.STYLING_CLASS_SR_STYLED_TEXT);

        CTabFolder tabFolder = new CTabFolder(compTabControl, SWT.NONE);
        FormData fd_tabFolder = new FormData();
        fd_tabFolder.bottom = new FormAttachment(100);
        fd_tabFolder.right = new FormAttachment(100);
        fd_tabFolder.top = new FormAttachment(55);
        fd_tabFolder.left = new FormAttachment(0);
        tabFolder.setLayoutData(fd_tabFolder);

        StringBuilder sbPartText = new StringBuilder();
        sbPartText.append(partInfoWrapper.getObj().getSearchObjectName() + "\n");
        sbPartText.append(partInfoWrapper.getObj().getSearchObjectGroup() + "\n");
        SimpleDateFormat fmt = new SimpleDateFormat(IRealCoreConstants.DEFAULT_FORMAT_DATE_LONG, IRealCoreConstants.DEFAULT_DATE_LOCALE);
        sbPartText.append(fmt.format(partInfoWrapper.getObj().getSearchTime().getTime()) + "\n");
        if ((partInfoWrapper.getObj().getCustomReplaceTable() != null) && (!partInfoWrapper.getObj().getCustomReplaceTable().isEmpty())) {
            for (Map.Entry<ReplaceableParamKeyPersist, ReplaceableParamPersist<?>> currParam : partInfoWrapper.getObj().getCustomReplaceTable().entrySet()) {
                sbPartText.append(currParam.getKey().getName() + "=" + currParam.getValue().getValue() + "\n");
            }
        }
        txtPartText.setText(sbPartText.toString());

        CTabItem firstItem = null;
        for (EnabledChildWrapper<SearchResultCurrentInfo> currInfoWrapper : partInfoWrapper.getChildren()) {
            CTabItem tbtmCurrSearchTab = new CTabItem(tabFolder, SWT.NONE);
            tbtmCurrSearchTab.setShowClose(false);
            tbtmCurrSearchTab.setText(currInfoWrapper.getObj().getSearchObjectName());
            if (firstItem == null) {
                firstItem = tbtmCurrSearchTab;
            }

            Composite compCurrSearch = new Composite(tabFolder, SWT.NONE);
            tbtmCurrSearchTab.setControl(compCurrSearch);
            compCurrSearch.setLayout(new FormLayout());

            Button btnchCurrSearchEnabled = new Button(compCurrSearch, SWT.CHECK);
            FormData fd_btnchCurrSearchEnabled = new FormData();
            fd_btnchCurrSearchEnabled.bottom = new FormAttachment(18);
            fd_btnchCurrSearchEnabled.right = new FormAttachment(100);
            fd_btnchCurrSearchEnabled.top = new FormAttachment(0);
            fd_btnchCurrSearchEnabled.left = new FormAttachment(0, 10);
            btnchCurrSearchEnabled.setLayoutData(fd_btnchCurrSearchEnabled);
            btnchCurrSearchEnabled.setText("Enabled");
            btnchCurrSearchEnabled.setData(CONTEXT_KEY_CURRENT_SEARCH_INFO, currInfoWrapper);
            btnchCurrSearchEnabled.setSelection(true);
            btnchCurrSearchEnabled.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button thisBtn = (Button)e.getSource();
                    EnabledChildWrapper<SearchResultCurrentInfo> currObj
                         = (EnabledChildWrapper<SearchResultCurrentInfo>)thisBtn.getData(CONTEXT_KEY_CURRENT_SEARCH_INFO);
                    if (currObj != null) {
                        currObj.setEnabled(thisBtn.getSelection());
                    }
                    super.widgetSelected(e);
                }
            });
            stylingEngine.setClassname(btnchCurrSearchEnabled, IEclipse4Constants.STYLING_CLASS_CHECKBOX_WHITE);

            Table table = new Table(compCurrSearch, SWT.BORDER | SWT.FULL_SELECTION);
            FormData fd_table = new FormData();
            fd_table.bottom = new FormAttachment(100);
            fd_table.top = new FormAttachment(18);
            fd_table.right = new FormAttachment(100);
            fd_table.left = new FormAttachment(0);
            table.setLayoutData(fd_table);
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
            stylingEngine.setClassname(table, IEclipse4Constants.STYLING_CLASS_SR_STYLED_TABLE);

            TableColumn infoCol = new TableColumn(table, SWT.CENTER);
            infoCol.setText("Information");
            infoCol.setResizable(true);
            TableItem itemSOName = new TableItem(table, SWT.BOLD);
            itemSOName.setText(currInfoWrapper.getObj().getSearchObjectName());

            TableItem itemSOGroup = new TableItem(table, SWT.BOLD);
            itemSOGroup.setText(currInfoWrapper.getObj().getSearchObjectGroup().toString());
            if ((currInfoWrapper.getObj().getCustomReplaceTable() != null) && (!currInfoWrapper.getObj().getCustomReplaceTable().isEmpty())) {
                for (Map.Entry<ReplaceableParamKeyPersist, ReplaceableParamPersist<?>> currParam : currInfoWrapper.getObj().getCustomReplaceTable().entrySet()) {
                    TableItem newItem = new TableItem(table, SWT.BOLD);
                    newItem.setText(currParam.getKey().getName() + "=" + currParam.getValue().getValue() + "\n");
                }
            }
            // the only column must always occupy the whole width
            table.addControlListener(new ControlAdapter() {

                @Override
                public void controlResized(ControlEvent e) {
                    Table table1 = (Table)e.getSource();
                    TableColumn tc = table1.getColumn(0);
                    tc.setWidth((int)(table1.getClientArea().width));
                }
            });
            infoCol.pack();
            table.pack();
        }
        if (firstItem != null) {
            tabFolder.setSelection(firstItem);
        }

        return tbtmPartInfo;
    }

    public List<SearchResultPartInfo> getAllResultsInitial() {
        return allResultsInitial;
    }

    public void setAllResultsInitial(List<SearchResultPartInfo> allResultsInitial) {
        this.allResultsInitial = allResultsInitial;
    }
}
