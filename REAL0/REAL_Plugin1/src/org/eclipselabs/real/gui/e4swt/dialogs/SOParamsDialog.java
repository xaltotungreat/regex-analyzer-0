package org.eclipselabs.real.gui.e4swt.dialogs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.util.IRealCoreConstants;

public class SOParamsDialog extends Dialog {

    private static final Logger log = LogManager.getLogger(SOParamsDialog.class);

    public static final String DATA_VALUE_INDEX = "DATA_VALUE_INDEX";
    public static final String DATA_VALUE_PARAM = "DATA_VALUE_PARAM";

    protected DialogResult<Map<ReplaceParamKey, IReplaceParam<?>>> result = new DialogResult<Map<ReplaceParamKey,IReplaceParam<?>>>(SWT.CANCEL);
    protected Shell shell;

    Map<String,String> initialParams;
    String soName;

    protected List<String[]> tableValues2;
    protected List<IReplaceParam<?>> tableValues;
    protected Map<ReplaceParamKey, IReplaceParam<?>> oldValues;
    protected String[] tableHeader;
    protected int columnValueIndex = 0;

    protected Label lblSOName;
    protected Table tableParams;
    protected Button btnOK;
    protected Button btnCancel;
    protected Double nameSpace = 0.3;
    protected Double valueSpace = 0.35;
    protected Double hintSpace = 0.35;

    protected SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", IRealCoreConstants.MAIN_DATE_LOCALE);

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    @Inject
    public SOParamsDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent, @Named("SearchObjectName") String soName) {
        //super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
        setText("Search Object Parameters");
        this.soName = soName;
    }

    public void setInitialParams(Map<String,String> iParams) {
        initialParams = iParams;
    }

    public List<IReplaceParam<?>> getTableValues() {
        return tableValues;
    }

    public void setTableValues(List<IReplaceParam<?>> tableValues) {
        this.tableValues = tableValues;
    }

    public Map<ReplaceParamKey, IReplaceParam<?>> getOldValues() {
        return oldValues;
    }

    public void setOldValues(Map<ReplaceParamKey, IReplaceParam<?>> oldValues) {
        this.oldValues = oldValues;
    }

    public String[] getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(String[] tableHeader) {
        this.tableHeader = tableHeader;
    }

    public int getColumnValueIndex() {
        return columnValueIndex;
    }

    public void setColumnValueIndex(int columnValueIndex) {
        this.columnValueIndex = columnValueIndex;
    }

    protected Map<ReplaceParamKey, IReplaceParam<?>> getParamMap() {
        Map<ReplaceParamKey, IReplaceParam<?>> resMap = new HashMap<ReplaceParamKey, IReplaceParam<?>>();
        TableItem[] allTI = tableParams.getItems();
        for (TableItem ti : allTI) {
            IReplaceParam<?> editedParam = (IReplaceParam<?>)ti.getData(DATA_VALUE_PARAM);
            switch (editedParam.getType()) {
            case BOOLEAN:
                IReplaceParam<Boolean> booleanParam = (IReplaceParam<Boolean>)editedParam;
                Boolean val = Boolean.parseBoolean(ti.getText(columnValueIndex));
                booleanParam.setValue(val);
                break;
            case DATE:
                IReplaceParam<Calendar> calParam = (IReplaceParam<Calendar>)editedParam;
                Date cellDt;
                try {
                    cellDt = dateFmt.parse(ti.getText(columnValueIndex));
                    if (cellDt != null) {
                        calParam.getValue().setTime(cellDt);
                    }
                } catch (ParseException e1) {
                    log.error("widgetSelected ", e1);
                }
                break;
            case INTEGER:
                IReplaceParam<Integer> intParam = (IReplaceParam<Integer>)editedParam;
                try {
                    Integer intVal = Integer.parseInt(ti.getText(columnValueIndex));
                    intParam.setValue(intVal);
                } catch (NumberFormatException nfe) {
                    log.error("getParamMap Incorrect number format", nfe);
                }
                break;
            case STRING:
            default:
                IReplaceParam<String> strParam = (IReplaceParam<String>)editedParam;
                strParam.setValue(ti.getText(columnValueIndex));
                break;
            }
            resMap.put(editedParam.getKey(), editedParam);
        }
        return resMap;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public DialogResult<Map<ReplaceParamKey, IReplaceParam<?>>> open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(650, 300);
        shell.setText(getText());

        shell.setLayout(new FormLayout());

        lblSOName = new Label(shell, SWT.CENTER);
        lblSOName.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
        FormData fd_lblSOName = new FormData();
        fd_lblSOName.bottom = new FormAttachment(0, 20);
        fd_lblSOName.right = new FormAttachment(100, -2);
        fd_lblSOName.top = new FormAttachment(0, 2);
        fd_lblSOName.left = new FormAttachment(0, 2);
        lblSOName.setLayoutData(fd_lblSOName);
        lblSOName.setText(soName);

        tableParams = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        tableParams.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
        FormData fd_tableParams = new FormData();
        fd_tableParams.bottom = new FormAttachment(100, -40);
        fd_tableParams.right = new FormAttachment(100, -2);
        fd_tableParams.top = new FormAttachment(0, 22);
        fd_tableParams.left = new FormAttachment(0, 2);
        tableParams.setLayoutData(fd_tableParams);
        tableParams.setHeaderVisible(true);
        tableParams.setLinesVisible(true);
        fillInTable(tableParams);
        tableParams.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                Table table = (Table)e.getSource();
                table.getColumn(0).setWidth((int)(table.getClientArea().width*nameSpace));
                table.getColumn(1).setWidth((int)(table.getClientArea().width*valueSpace));
                table.getColumn(2).setWidth((int)(table.getClientArea().width*hintSpace));
            }
        });
        tableParams.pack();
        //paramName.pack();
        //paramValue.pack();

        final TableEditor editor = new TableEditor(tableParams);
        //The editor must have the same size as the cell and must
        //not be any smaller than 50 pixels.
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;
        // editing the second column
        tableParams.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Identify the selected row
                TableItem item = (TableItem) e.item;
                if (item == null) {
                    return;
                }

                // The control that will be the editor must be a child of the Table
                IReplaceParam<?> editedParam = (IReplaceParam<?>)item.getData(DATA_VALUE_PARAM);
                if (editedParam != null) {
                    // Clean up any previous editor control
                    Control oldEditor = editor.getEditor();
                    if (oldEditor != null) {
                        oldEditor.dispose();
                    }

                    Control editControl = null;
                    String cellText = item.getText(columnValueIndex);
                    switch (editedParam.getType()) {
                    case BOOLEAN:
                        Combo cmb = new Combo(tableParams, SWT.READ_ONLY);
                        String[] booleanItems = {Boolean.toString(true), Boolean.toString(false)};
                        cmb.setItems(booleanItems);
                        cmb.select(1);
                        if (Boolean.parseBoolean(cellText)) {
                            cmb.select(0);
                        }
                        editControl = cmb;
                        cmb.addSelectionListener(new SelectionAdapter() {

                            @Override
                            public void widgetSelected(SelectionEvent se) {
                                Combo text = (Combo) editor.getEditor();
                                editor.getItem().setText(columnValueIndex, text.getText());
                            }
                        });
                        break;
                    case DATE:
                        IReplaceParam<Calendar> calParam = (IReplaceParam<Calendar>)editedParam;
                        Calendar calToUse = calParam.getValue();
                        Composite dateAndTime = new Composite(tableParams, SWT.NONE);
                        RowLayout rl = new RowLayout();
                        rl.wrap = false;
                        dateAndTime.setLayout(rl);
                        Date cellDt;
                        try {
                            cellDt = dateFmt.parse(cellText);
                            if (cellDt != null) {
                                calToUse.setTime(cellDt);
                            }
                        } catch (ParseException e1) {
                            log.error("widgetSelected ", e1);
                        }

                        DateTime datePicker = new DateTime(dateAndTime, SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
                        datePicker.setData(DATA_VALUE_PARAM, calParam);
                        DateTime timePicker = new DateTime(dateAndTime, SWT.TIME | SWT.LONG);
                        timePicker.setData(DATA_VALUE_PARAM, calParam);

                        datePicker.setDate(calToUse.get(Calendar.YEAR), calToUse.get(Calendar.MONTH),
                                calToUse.get(Calendar.DAY_OF_MONTH));
                        timePicker.setTime(calToUse.get(Calendar.HOUR_OF_DAY), calToUse.get(Calendar.MINUTE),
                                calToUse.get(Calendar.SECOND));

                        datePicker.addSelectionListener(new SelectionAdapter() {

                            @Override
                            public void widgetSelected(SelectionEvent se) {
                                DateTime source = (DateTime)se.getSource();
                                IReplaceParam<Calendar> calParam = (IReplaceParam<Calendar>)source.getData(DATA_VALUE_PARAM);
                                if (calParam != null) {
                                    calParam.getValue().set(Calendar.YEAR, source.getYear());
                                    calParam.getValue().set(Calendar.MONTH, source.getMonth());
                                    calParam.getValue().set(Calendar.DAY_OF_MONTH, source.getDay());
                                    String resultText = dateFmt.format(calParam.getValue().getTime());
                                    log.debug("Result Text " + resultText);
                                    editor.getItem().setText(columnValueIndex, resultText);
                                } else {
                                    log.warn("widgetSelected param is null");
                                }
                            }

                        });
                        timePicker.addSelectionListener(new SelectionAdapter() {

                            @Override
                            public void widgetSelected(SelectionEvent se) {
                                DateTime source = (DateTime)se.getSource();
                                IReplaceParam<Calendar> calParam = (IReplaceParam<Calendar>)source.getData(DATA_VALUE_PARAM);
                                if (calParam != null) {
                                    calParam.getValue().set(Calendar.HOUR_OF_DAY, source.getHours());
                                    calParam.getValue().set(Calendar.MINUTE, source.getMinutes());
                                    calParam.getValue().set(Calendar.SECOND, source.getSeconds());
                                    String resultText = dateFmt.format(calParam.getValue().getTime());
                                    log.debug("Result Text " + resultText);
                                    editor.getItem().setText(columnValueIndex, resultText);
                                } else {
                                    log.warn("widgetSelected param is null");
                                }
                            }

                        });
                        dateAndTime.layout();
                        editControl = dateAndTime;
                        break;
                    case INTEGER:
                        Text intEditor = new Text(tableParams, SWT.NONE);
                        intEditor.setText(item.getText(columnValueIndex));
                        intEditor.selectAll();
                        intEditor.addModifyListener(new ModifyListener() {
                            @Override
                            public void modifyText(ModifyEvent se) {
                                Text text = (Text) editor.getEditor();
                                Integer resultInt = null;
                                try {
                                    resultInt = Integer.parseInt(text.getText());
                                } catch (NumberFormatException nfe) {
                                    log.error("NFE ", nfe);
                                }
                                if (resultInt != null) {
                                    editor.getItem().setText(columnValueIndex, resultInt.toString());
                                }
                            }
                        });
                        editControl = intEditor;
                        break;
                    case STRING:
                    default:
                        Text newEditor = new Text(tableParams, SWT.NONE);
                        newEditor.setText(item.getText(columnValueIndex));
                        newEditor.setFont(tableParams.getFont());
                        newEditor.selectAll();
                        newEditor.addModifyListener(new ModifyListener() {
                            @Override
                            public void modifyText(ModifyEvent se) {
                                Text text = (Text) editor.getEditor();
                                editor.getItem().setText(columnValueIndex, text.getText());
                            }
                        });
                        editControl = newEditor;
                        break;
                    }

                    editControl.setFont(tableParams.getFont());
                    editControl.setFocus();
                    editor.setEditor(editControl, item, columnValueIndex);
                }
            }
        });


        btnOK = new Button(shell, SWT.NONE);
        btnOK.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
        FormData fd_btnOK = new FormData();
        fd_btnOK.bottom = new FormAttachment(100, -7);
        fd_btnOK.right = new FormAttachment(40, 2);
        fd_btnOK.top = new FormAttachment(100, -35);
        fd_btnOK.left = new FormAttachment(15, 2);
        btnOK.setLayoutData(fd_btnOK);
        btnOK.setText("OK");
        btnOK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                result = new DialogResult<Map<ReplaceParamKey,IReplaceParam<?>>>(SWT.OK, getParamMap());
                shell.close();
            }

        });
        shell.setDefaultButton(btnOK);

        btnCancel = new Button(shell, SWT.NONE);
        btnCancel.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
        FormData fd_btnCancel = new FormData();
        fd_btnCancel.bottom = new FormAttachment(100, -7);
        fd_btnCancel.left = new FormAttachment(60, -2);
        fd_btnCancel.top = new FormAttachment(100, -35);
        fd_btnCancel.right = new FormAttachment(85, -2);
        btnCancel.setLayoutData(fd_btnCancel);
        btnCancel.setText("Cancel");
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                result = new DialogResult<Map<ReplaceParamKey,IReplaceParam<?>>>(SWT.CANCEL, null);
                shell.close();
            }

        });
        tableParams.redraw();
    }

    protected void fillInTable(Table table) {
        if ((tableHeader != null) && (tableValues != null)) {
            for (String currHeader : tableHeader) {
                TableColumn newCol = new TableColumn(table, SWT.NONE);
                newCol.setText(currHeader);
            }
            for (int i = 0; i < tableValues.size(); i++) {
                IReplaceParam<?> currRow = tableValues.get(i);
                IReplaceParam<?> prevValue = null;
                if ((oldValues != null) && (!oldValues.isEmpty())){
                    prevValue = oldValues.get(currRow.getKey());
                }
                TableItem currItem = new TableItem(table, SWT.NONE);
                currItem.setText(0, currRow.getName());
                currItem.setData(DATA_VALUE_INDEX, i);
                currItem.setData(DATA_VALUE_PARAM, currRow);
                String cellText = null;
                switch (currRow.getType()) {
                case BOOLEAN:
                    cellText = ((IReplaceParam<Boolean>)currRow).getValue().toString();
                    if (prevValue != null) {
                        cellText = ((IReplaceParam<Boolean>)prevValue).getValue().toString();
                        ((IReplaceParam<Boolean>)currRow).setValue(((IReplaceParam<Boolean>)prevValue).getValue());
                    }
                    break;
                case INTEGER:
                    cellText = ((IReplaceParam<Integer>)currRow).getValue().toString();
                    if (prevValue != null) {
                        cellText = ((IReplaceParam<Integer>)prevValue).getValue().toString();
                        ((IReplaceParam<Integer>)currRow).setValue(((IReplaceParam<Integer>)prevValue).getValue());
                    }
                    break;
                case DATE:
                    cellText = dateFmt.format(((IReplaceParam<Calendar>)currRow).getValue().getTime());
                    if (prevValue != null) {
                        cellText = dateFmt.format(((IReplaceParam<Calendar>)prevValue).getValue().getTime());
                        ((IReplaceParam<Calendar>)currRow).setValue((Calendar)((IReplaceParam<Calendar>)prevValue).getValue().clone());
                    }
                    break;
                case STRING:
                default:
                    cellText = ((IReplaceParam<String>)currRow).getValue();
                    if (prevValue != null) {
                        cellText = ((IReplaceParam<String>)prevValue).getValue();
                        ((IReplaceParam<String>)currRow).setValue(((IReplaceParam<String>)prevValue).getValue());
                    }
                    break;
                }
                currItem.setText(1, cellText);
                currItem.setText(2, currRow.getDescription());
            }
            for (TableColumn currCol : table.getColumns()) {
                currCol.pack();
            }
        }
    }

}
