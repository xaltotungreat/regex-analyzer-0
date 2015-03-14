package org.eclipselabs.real.gui.e4swt.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamValueType;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.core.sotree.IDisplaySO;
import org.eclipselabs.real.gui.e4swt.E4SwtSearchObjectHelper;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.eclipselabs.real.gui.e4swt.conveyor.ConvSearchRequest;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;
import org.eclipselabs.real.gui.e4swt.util.FindRegexAnalysisResult;

@Creatable
public class FindDialog extends SingleDialog {
    private static final Logger log = LogManager.getLogger(FindDialog.class);

    public static class FindDialogState {
        protected List<String> findItems = new ArrayList<String>();
        protected String findText;
        protected Boolean forwardSelected;
        protected Boolean backwardSelected;
        protected Boolean wrapSelected;
        protected Boolean caseSensitiveSelected;
        protected Boolean regularExpressionsSelected;
        protected Boolean performAnalysisSelected;

        public void addFindItem(String aNewtText) {
            findItems.add(aNewtText);
        }

        public List<String> getFindItems() {
            return findItems;
        }
        public void setFindItems(List<String> findText) {
            this.findItems = findText;
        }
        public String getFindText() {
            return findText;
        }

        public void setFindText(String findText) {
            this.findText = findText;
        }

        public Boolean getForwardSelected() {
            return forwardSelected;
        }
        public void setForwardSelected(Boolean forwardSelected) {
            this.forwardSelected = forwardSelected;
        }
        public Boolean getBackwardSelected() {
            return backwardSelected;
        }
        public void setBackwardSelected(Boolean backwardSelected) {
            this.backwardSelected = backwardSelected;
        }
        public Boolean getWrapSelected() {
            return wrapSelected;
        }
        public void setWrapSelected(Boolean wrapSelected) {
            this.wrapSelected = wrapSelected;
        }
        public Boolean getCaseSensitiveSelected() {
            return caseSensitiveSelected;
        }
        public void setCaseSensitiveSelected(Boolean csSelected) {
            this.caseSensitiveSelected = csSelected;
        }

        public Boolean getRegularExpressionsSelected() {
            return regularExpressionsSelected;
        }

        public void setRegularExpressionsSelected(Boolean regularExpressionsSelected) {
            this.regularExpressionsSelected = regularExpressionsSelected;
        }

        public Boolean getPerformAnalysisSelected() {
            return performAnalysisSelected;
        }

        public void setPerformAnalysisSelected(Boolean performAnalysisSelected) {
            this.performAnalysisSelected = performAnalysisSelected;
        }

    }

    protected FindDialogState previousState = new FindDialogState();

    @Inject
    MWindow mainWindow;

    @Inject
    EModelService modelService;

    private Combo comboFindText;
    private Group grpDirection;
    private Button btnrForward;
    private Button btnrBackward;
    private Button btnFind;
    private Button btnSearchAllInCurrent;
    private Button btnClose;
    //private Button btncRegex;
    private Button btncFindWrap;
    private Button btncCaseSensitive;
    private Button btncRegularExpressions;
    private Button btncPerformAnalysis;
    private Label lblStatus;

    protected RGB greenStatusRGB = new RGB(0, 128, 128);
    protected RGB redStatusRGB = new RGB(255, 0, 0);


    @Inject
    public FindDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
        setText("Find");
    }

    public void setInitialText(String aText) {
        getPreviousState().setFindText(aText);
    }

    public void init() {
        createControls();
        shell.layout();
        comboFindText.setSelection(new Point(0, comboFindText.getText().length()));
    }

    public void open() {
        //createControls();
        //shell.layout();
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public void createControls() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(450, 350);
        shell.setText(getText());

        shell.setLayout(new FormLayout());
        shell.addShellListener(new ShellAdapter() {

            @Override
            public void shellActivated(ShellEvent e) {
                if ((getPreviousState().getFindText() != null)
                        && (!getPreviousState().getFindText().equals(comboFindText.getText()))) {
                    comboFindText.setText(getPreviousState().getFindText());
                }
            }

            @Override
            public void shellClosed(ShellEvent e) {
                savePreviousState(getPreviousState());
            }
        });


        grpDirection = new Group(shell, SWT.NONE);
        grpDirection.setText("Direction");
        RowLayout rl_grpDirection = new RowLayout(SWT.VERTICAL);
        rl_grpDirection.justify = true;
        rl_grpDirection.fill = true;
        grpDirection.setLayout(rl_grpDirection);
        FormData fd_grpDirection = new FormData();
        fd_grpDirection.bottom = new FormAttachment(60, 15);
        fd_grpDirection.top = new FormAttachment(15, 15);
        fd_grpDirection.left = new FormAttachment(0, 10);
        fd_grpDirection.right = new FormAttachment(48);
        grpDirection.setLayoutData(fd_grpDirection);

        btnrForward = new Button(grpDirection, SWT.RADIO);
        btnrForward.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
        btnrForward.setText("Forward");
        btnrForward.setSelection(true);
        btnrForward.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button)e.getSource()).getSelection()) {
                    btnrBackward.setSelection(false);
                }
            }
        });

        btnrBackward = new Button(grpDirection, SWT.RADIO);
        btnrBackward.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
        btnrBackward.setText("Backward");
        btnrBackward.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button)e.getSource()).getSelection()) {
                    btnrForward.setSelection(false);
                }
            }
        });
        grpDirection.setTabList(new Control[]{btnrForward,btnrBackward});

        btnFind = new Button(shell, SWT.NONE);
        FormData fd_btnOK = new FormData();
        fd_btnOK.top = new FormAttachment(80);
        fd_btnOK.right = new FormAttachment(32);
        fd_btnOK.bottom = new FormAttachment(100, -25);
        fd_btnOK.left = new FormAttachment(4);
        btnFind.setLayoutData(fd_btnOK);
        btnFind.setText("Find");
        btnFind.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MPartStack srStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, mainWindow);
                MPart activeSR = (MPart)srStack.getSelectedElement();
                GUISearchResult searchResult = (GUISearchResult)activeSR.getObject();
                if (!"".equals(comboFindText.getText())) {
                    int direction;
                    if (btnrForward.getSelection()) {
                        direction = SWT.DOWN;
                    } else {
                        direction = SWT.UP;
                    }

                    String findStr = null;
                    if (btncRegularExpressions.getSelection()) {
                        findStr = comboFindText.getText();
                    } else {
                        findStr = Pattern.quote(comboFindText.getText());
                    }
                    FindRegexAnalysisResult findRes = null;
                    if (btncPerformAnalysis.getSelection()) {
                        findRes = searchResult.findRegexAnalysis(findStr, direction,
                                btncFindWrap.getSelection(), !btncCaseSensitive.getSelection());
                    } else {
                        findRes = searchResult.findRegex(findStr, direction,
                            btncFindWrap.getSelection(), !btncCaseSensitive.getSelection());
                    }
                    if (!Arrays.asList(comboFindText.getItems()).contains(comboFindText.getText())) {
                        comboFindText.add(comboFindText.getText(), 0);
                    }
                    if (findRes != null) {
                        setFindStatus(findRes);
                    }
                }
            }
        });
        shell.setDefaultButton(btnFind);

        btnClose = new Button(shell, SWT.NONE);
        FormData fd_btnCancel = new FormData();
        fd_btnCancel.bottom = new FormAttachment(100, -25);
        fd_btnCancel.right = new FormAttachment(96);
        fd_btnCancel.top = new FormAttachment(80);
        fd_btnCancel.left = new FormAttachment(68);
        btnClose.setLayoutData(fd_btnCancel);
        btnClose.setText("Close");
        btnClose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });

        comboFindText = new Combo(shell, SWT.DROP_DOWN);
        comboFindText.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NORMAL));
        FormData fd_comboFindText = new FormData();
        fd_comboFindText.bottom = new FormAttachment(11, 15);
        fd_comboFindText.right = new FormAttachment(100, -10);
        fd_comboFindText.top = new FormAttachment(0, 15);
        fd_comboFindText.left = new FormAttachment(0, 10);
        comboFindText.setLayoutData(fd_comboFindText);
        comboFindText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getPreviousState().setFindText(((Combo)e.getSource()).getText());
            }
        });
        comboFindText.redraw();
        comboFindText.setFocus();

        btncFindWrap = new Button(shell, SWT.CHECK);
        btncFindWrap.setSelection(true);
        FormData fd_btncFindWrap = new FormData();
        fd_btncFindWrap.top = new FormAttachment(15, 25);
        fd_btncFindWrap.left = new FormAttachment(52);
        fd_btncFindWrap.bottom = new FormAttachment(26, 25);
        fd_btncFindWrap.right = new FormAttachment(100, -10);
        btncFindWrap.setLayoutData(fd_btncFindWrap);
        btncFindWrap.setText("Wrap");

        btncCaseSensitive = new Button(shell, SWT.CHECK);
        FormData fd_btnCaseSensitive = new FormData();
        fd_btnCaseSensitive.top = new FormAttachment(25, 25);
        fd_btnCaseSensitive.left = new FormAttachment(52);
        fd_btnCaseSensitive.bottom = new FormAttachment(37, 25);
        fd_btnCaseSensitive.right = new FormAttachment(100, -10);
        btncCaseSensitive.setLayoutData(fd_btnCaseSensitive);
        btncCaseSensitive.setText("Case sensitive");

        btncRegularExpressions = new Button(shell, SWT.CHECK);
        FormData fd_btncRegularExpressions = new FormData();
        fd_btncRegularExpressions.top = new FormAttachment(35, 25);
        fd_btncRegularExpressions.left = new FormAttachment(52);
        fd_btncRegularExpressions.bottom = new FormAttachment(48, 25);
        fd_btncRegularExpressions.right = new FormAttachment(100, -10);
        btncRegularExpressions.setLayoutData(fd_btncRegularExpressions);
        btncRegularExpressions.setText("Regular Expressions");

        btncPerformAnalysis = new Button(shell, SWT.CHECK);
        btncPerformAnalysis.setSelection(true);
        FormData fd_btncPerformAnalysis = new FormData();
        fd_btncPerformAnalysis.top = new FormAttachment(45, 25);
        fd_btncPerformAnalysis.left = new FormAttachment(52);
        fd_btncPerformAnalysis.bottom = new FormAttachment(59, 25);
        fd_btncPerformAnalysis.right = new FormAttachment(100, -10);
        btncPerformAnalysis.setLayoutData(fd_btncPerformAnalysis);
        btncPerformAnalysis.setText("Perform Analysis");

        lblStatus = new Label(shell, SWT.NONE);
        lblStatus.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.BOLD));
        FormData fd_lblStatus = new FormData();
        fd_lblStatus.top = new FormAttachment(100, -22);
        fd_lblStatus.right = new FormAttachment(100, -10);
        fd_lblStatus.bottom = new FormAttachment(100, -5);
        fd_lblStatus.left = new FormAttachment(0, 10);
        lblStatus.setLayoutData(fd_lblStatus);

        btnSearchAllInCurrent = new Button(shell, SWT.NONE);
        FormData fd_btnFindAllInCurrent = new FormData();
        fd_btnFindAllInCurrent.bottom = new FormAttachment(100, -25);
        fd_btnFindAllInCurrent.left = new FormAttachment(36);
        fd_btnFindAllInCurrent.top = new FormAttachment(80);
        fd_btnFindAllInCurrent.right = new FormAttachment(64);
        btnSearchAllInCurrent.setLayoutData(fd_btnFindAllInCurrent);
        btnSearchAllInCurrent.setText("Search in current");

        btnSearchAllInCurrent.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String searchID = String.valueOf(System.currentTimeMillis());
                MPartStack srStack = (MPartStack)modelService.find(IEclipse4Constants.APP_MODEL_PSTACK_SEARCH_RESULTS, mainWindow);
                MPart activeSR = (MPart)srStack.getSelectedElement();
                GUISearchResult searchResult = (GUISearchResult)activeSR.getObject();
                if (searchResult != null) {
                    log.debug("Find all for name=" + searchResult.getMainSearchInfo().getSearchObjectName()
                            + " group=" + searchResult.getMainSearchInfo().getSearchObjectGroup()
                            + " tags=" + searchResult.getMainSearchInfo().getSearchObjectTags());
                    IDisplaySO anyRecDSO = E4SwtSearchObjectHelper.getAnyRecordForGroupUpstream(
                            searchResult.getMainSearchInfo().getSearchObjectGroup());
                    if (anyRecDSO != null) {
                        if (btncCaseSensitive.getSelection()) {
                            // TODO maybe execute with the regex flag not set them?
                            anyRecDSO.getSearchObject().setRegexFlags(0);
                        } else {
                            anyRecDSO.getSearchObject().setRegexFlags(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                        }
                        anyRecDSO.setDisplayName("Any");
                        Map<ReplaceParamKey, IReplaceParam<?>> replaceParams = new HashMap<>();
                        List<IReplaceParam<?>> clonedParams = anyRecDSO.getSearchObject().getCloneParamList();
                        if (btncRegularExpressions.getSelection()) {
                            for (IReplaceParam<?> rp : clonedParams) {
                                if (ReplaceParamValueType.STRING.equals(rp.getType())) {
                                    IReplaceParam<String> stringRp = (IReplaceParam<String>)rp;
                                    stringRp.setValue(comboFindText.getText());
                                    replaceParams.put(stringRp.getKey(), stringRp);
                                }
                            }
                        } else {
                            for (IReplaceParam<?> rp : clonedParams) {
                                if (ReplaceParamValueType.STRING.equals(rp.getType())) {
                                    IReplaceParam<String> stringRp = (IReplaceParam<String>)rp;
                                    stringRp.setValue(Pattern.quote(comboFindText.getText()));
                                    replaceParams.put(stringRp.getKey(), stringRp);
                                }
                            }
                        }
                        // set the request in the context
                        ConvSearchRequest convReq = new ConvSearchRequest(anyRecDSO, srStack, new TimeUnitWrapper((long)30, TimeUnit.MINUTES));
                        convReq.setPreparedParams(replaceParams);
                        convReq.setSearchInCurrent(true);
                        String randomKey = UUID.randomUUID().toString();
                        mainWindow.getContext().set(randomKey, convReq);

                        mainWindow.getContext().set("DSO" + searchID, anyRecDSO);
                        mainWindow.getContext().set("DSO" + searchID, anyRecDSO);
                        mainWindow.getContext().set("SOParams" + searchID, replaceParams);
                        Command dsoCommand = searchResult.getCommandService().getCommand(IEclipse4Constants.APP_MODEL_COMMAND_INTERNAL_SEARCH);
                        //Command dsoCommand = commandService.getCommand(IEclipse4Constants.APP_MODEL_COMMAND_INTERNAL_SEARCH);
                        try {
                            //IParameter dsoParam = dsoCommand.getParameter(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_INTSEARCH_DSO_KEY);
                            //IParameter paramsParam = dsoCommand.getParameter(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_INTSEARCH_PARAMS_KEY);
                            IParameter reqParam = dsoCommand.getParameter(IEclipse4Constants.APP_MODEL_COMMAND_PARAM_INTSEARCH_REQ_KEY);
                            ParameterizedCommand execComm = new ParameterizedCommand(dsoCommand, new Parameterization[] {
                                    new Parameterization(reqParam, randomKey)});
                            if (searchResult.getHandlerService().canExecute(execComm, activeSR.getContext())) {
                                searchResult.getHandlerService().executeHandler(execComm, activeSR.getContext());
                            } else {
                                log.warn("Command " + IEclipse4Constants.APP_MODEL_COMMAND_INTERNAL_SEARCH + " is disabled cannot execute");
                            }
                        } catch (NotDefinedException e1) {
                            log.error("Parameter not defined ", e1);
                        }
                    } else {
                        log.error("No AnyRecord DSO for SO group=" + searchResult.getMainSearchInfo().getSearchObjectGroup());
                        setStatus("Not found the 'any record' SO for this group", SWTResourceManager.getColor(redStatusRGB));
                    }
                }
            }
        });

        shell.setTabList(new Control[]{comboFindText,grpDirection, btncFindWrap,
                btncCaseSensitive, btncRegularExpressions, btnFind, btnSearchAllInCurrent, btnClose});
        readPreviousState(getPreviousState());
    }

    protected void setFindStatus(FindRegexAnalysisResult findResult) {
        StringBuilder sb = new StringBuilder();
        Color statusColor;
        if (findResult.getPositionStart() != null) {
            sb.append("Found at " + findResult.getPositionStart());
            statusColor = SWTResourceManager.getColor(greenStatusRGB);
        } else {
            sb.append("Not found");
            statusColor = SWTResourceManager.getColor(redStatusRGB);
        }
        if (findResult.getAboveObjects() != null) {
            sb.append(" above " + findResult.getAboveObjects());
        }
        if (findResult.getBelowObjects() != null) {
            sb.append(" below " + findResult.getBelowObjects());
        }
        if (findResult.getObjectsCount() != null) {
            sb.append(" count " + findResult.getObjectsCount());
        }
        setStatus(sb.toString(), statusColor);
    }

    public void setStatus(String newStatus, Color statusColor) {
        lblStatus.setText(newStatus);
        lblStatus.setForeground(statusColor);
    }

    protected void readPreviousState(FindDialogState prevState) {
        if ((prevState.getFindItems() != null) && !prevState.getFindItems().isEmpty()) {
            for (String cmbStr : prevState.getFindItems()) {
                comboFindText.add(cmbStr);
            }
            if (prevState.getFindText() != null) {
                comboFindText.setText(prevState.getFindText());
            } else {
                comboFindText.setText(prevState.getFindItems().get(0));
            }

        }
        if (prevState.getForwardSelected() != null) {
            btnrForward.setSelection(prevState.getForwardSelected());
        }
        if (prevState.getBackwardSelected() != null) {
            btnrBackward.setSelection(prevState.getBackwardSelected());
        }
        if (prevState.getCaseSensitiveSelected() != null) {
            btncCaseSensitive.setSelection(prevState.getCaseSensitiveSelected());
        }
        if (prevState.getRegularExpressionsSelected() != null) {
            btncRegularExpressions.setSelection(prevState.getRegularExpressionsSelected());
        }
        if (prevState.getPerformAnalysisSelected() != null) {
            btncPerformAnalysis.setSelection(prevState.getPerformAnalysisSelected());
        }
    }

    protected void savePreviousState(FindDialogState prevState) {
        prevState.getFindItems().clear();
        prevState.getFindItems().addAll(Arrays.asList(comboFindText.getItems()));
        prevState.setForwardSelected(btnrForward.getSelection());
        prevState.setBackwardSelected(btnrBackward.getSelection());
        prevState.setCaseSensitiveSelected(btncCaseSensitive.getSelection());
        prevState.setRegularExpressionsSelected(btncRegularExpressions.getSelection());
        prevState.setPerformAnalysisSelected(btncPerformAnalysis.getSelection());
    }

    public FindDialogState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(FindDialogState previousState) {
        this.previousState = previousState;
    }

}
