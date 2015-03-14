package org.eclipselabs.real.gui.e4swt.dialogs;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

@Creatable
public class EnableLogTypesDialog extends SingleDialog {

    protected Map<String, Boolean> initialValues;
    protected DialogResult<Map<String, Boolean>> result;
    private Table table;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    @Inject
    public EnableLogTypesDialog(@Named(IServiceConstants.ACTIVE_SHELL)Shell parent) {
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
        setText("Enable/Disable Log Types");
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public DialogResult<Map<String,Boolean>> open() {
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
        //result = new EnableLogTypesResult(SWT.CANCEL, initialValues);
        result = new DialogResult<Map<String,Boolean>>(SWT.CANCEL, initialValues);
        shell = new Shell(getParent(), getStyle());
        shell.setSize(661, 479);
        shell.setText(getText());
        shell.setLayout(new FormLayout());
        registerContextRemoveOnClose();

        Composite compTable = new Composite(shell, SWT.NONE);
        compTable.setLayout(new FillLayout(SWT.HORIZONTAL));
        FormData fd_compTable = new FormData();
        fd_compTable.bottom = new FormAttachment(100, -60);
        fd_compTable.right = new FormAttachment(100);
        fd_compTable.top = new FormAttachment(0);
        fd_compTable.left = new FormAttachment(0);
        compTable.setLayoutData(fd_compTable);

        table = new Table(compTable, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tcLogType = new TableColumn(table, SWT.NONE);
        tcLogType.setText("Log Type");
        for (Map.Entry<String, Boolean> typeEntry : initialValues.entrySet()) {
            TableItem newItem = new TableItem(table, SWT.NONE);
            newItem.setText(typeEntry.getKey());
            if (typeEntry.getValue()) {
                newItem.setChecked(true);
            }
        }

        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    TableItem checkedItem = (TableItem)e.item;
                    result.getResult().put(checkedItem.getText(), checkedItem.getChecked());
                }
            }
        });
        table.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                Table tableVar = (Table)e.getSource();
                tableVar.getColumn(0).setWidth(tableVar.getClientArea().width);
            }
        });
        tcLogType.pack();
        table.pack();

        Composite compButtons = new Composite(shell, SWT.NONE);
        compButtons.setLayout(new FormLayout());
        FormData fd_compButtons = new FormData();
        fd_compButtons.top = new FormAttachment(100, -60);
        fd_compButtons.right = new FormAttachment(100);
        fd_compButtons.bottom = new FormAttachment(100);
        fd_compButtons.left = new FormAttachment(0);
        compButtons.setLayoutData(fd_compButtons);

        Button btnOK = new Button(compButtons, SWT.FLAT);
        FormData fd_btnOK = new FormData();
        fd_btnOK.bottom = new FormAttachment(80);
        fd_btnOK.right = new FormAttachment(40);
        fd_btnOK.top = new FormAttachment(20);
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

        Button btnCancel = new Button(compButtons, SWT.FLAT);
        FormData fd_btnCancel = new FormData();
        fd_btnCancel.top = new FormAttachment(20);
        fd_btnCancel.right = new FormAttachment(80);
        fd_btnCancel.left = new FormAttachment(60);
        fd_btnCancel.bottom = new FormAttachment(80);
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

    public Map<String, Boolean> getInitialValues() {
        return initialValues;
    }

    public void setInitialValues(Map<String, Boolean> initialValues) {
        this.initialValues = initialValues;
    }
}
