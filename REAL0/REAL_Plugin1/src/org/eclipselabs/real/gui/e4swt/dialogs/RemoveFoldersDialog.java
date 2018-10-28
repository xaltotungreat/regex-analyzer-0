package org.eclipselabs.real.gui.e4swt.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.di.Focus;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

@Creatable
public class RemoveFoldersDialog extends SingleDialog {
    private Table table;

    //protected Shell shell;
    protected DialogResult<List<String>> result = new DialogResult<>(SWT.CANCEL, new ArrayList<String>());

    protected Set<String> allFoldersList = new HashSet<>();

    @Inject
    public RemoveFoldersDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
        setText("Remove folder");
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public DialogResult<List<String>> open() {
        createControls();
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


    public void setAllFoldersList(Set<String> foldersList) {
        this.allFoldersList = foldersList;
    }

    /**
     * Create contents of the view part.
     */
    //@PostConstruct
    public void createControls() {

        shell = new Shell(getParent(), getStyle());
        shell.setSize(450, 300);
        shell.setText(getText());

        shell.setLayout(new FormLayout());
        registerContextRemoveOnClose();

        table = new Table(shell, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        FormData fd_table = new FormData();
        fd_table.bottom = new FormAttachment(100, -35);
        fd_table.right = new FormAttachment(100);
        fd_table.top = new FormAttachment(0);
        fd_table.left = new FormAttachment(0);
        table.setLayoutData(fd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    TableItem checkedItem = (TableItem)e.item;
                    if (checkedItem.getChecked()) {
                        result.getResult().add(checkedItem.getText());
                    } else {
                        result.getResult().remove(checkedItem.getText());
                    }
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
        TableColumn tcFld = new TableColumn(table, SWT.NONE);
        tcFld.setText("Folder");
        for (String fldStr : allFoldersList) {
            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setText(fldStr);
        }
        tcFld.pack();
        table.pack();

        Button btnOK = new Button(shell, SWT.NONE);
        FormData fd_btnOK = new FormData();
        fd_btnOK.bottom = new FormAttachment(100, -5);
        fd_btnOK.right = new FormAttachment(43);
        fd_btnOK.top = new FormAttachment(100, -30);
        fd_btnOK.left = new FormAttachment(15);
        btnOK.setLayoutData(fd_btnOK);
        btnOK.setText("OK");
        btnOK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                result.setAction(SWT.OK);
                shell.close();
            }
        });

        Button btnCancel = new Button(shell, SWT.NONE);
        btnCancel.setText("Cancel");
        FormData fd_btnCancel = new FormData();
        fd_btnCancel.bottom = new FormAttachment(100, -5);
        fd_btnCancel.left = new FormAttachment(57);
        fd_btnCancel.top = new FormAttachment(100, -30);
        fd_btnCancel.right = new FormAttachment(85);
        btnCancel.setLayoutData(fd_btnCancel);
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                result.setAction(SWT.CANCEL);
                shell.close();
            }
        });
        table.redraw();
    }

    /*@PreDestroy
    public void dispose() {
    }*/

    @Focus
    public void setFocus() {
        table.setFocus();
    }
}
