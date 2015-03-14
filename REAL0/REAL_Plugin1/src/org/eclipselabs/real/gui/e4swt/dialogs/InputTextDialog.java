package org.eclipselabs.real.gui.e4swt.dialogs;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

@Creatable
public class InputTextDialog extends Dialog {

    private static final Logger log = LogManager.getLogger(LogFilesInfoDialog.class);

    protected Shell shell;
    protected DialogResult<String> result = new DialogResult<String>(SWT.CANCEL);
    private Text text;
    private Button btnCancel;
    private Button btnOK;
    private String initialText;

    @Inject
    public InputTextDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
    }

    public void init(String aCaption) {
        setText(aCaption);
    }

    public DialogResult<String> open() {
        createContents();
        shell.layout();
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    public void setInitialText(String intStr) {
        initialText = intStr;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(400, 150);
        shell.setText(getText());
        shell.setLayout(new FormLayout());

        text = new Text(shell, SWT.BORDER);
        text.setFont(SWTResourceManager.getFont("Tahoma", 11, SWT.NORMAL));
        FormData fd_text = new FormData();
        fd_text.bottom = new FormAttachment(40);
        fd_text.right = new FormAttachment(100, -10);
        fd_text.top = new FormAttachment(5, 10);
        fd_text.left = new FormAttachment(0, 10);
        text.setLayoutData(fd_text);
        if (initialText != null) {
            text.setText(initialText);
            text.setSelection(0, initialText.length());
        }

        btnOK = new Button(shell, SWT.NONE);
        FormData fd_btnOK = new FormData();
        fd_btnOK.top = new FormAttachment(48, 10);
        fd_btnOK.right = new FormAttachment(45, -10);
        fd_btnOK.bottom = new FormAttachment(95, -10);
        fd_btnOK.left = new FormAttachment(7, 10);
        btnOK.setLayoutData(fd_btnOK);
        btnOK.setText("OK");
        btnOK.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                result.setAction(SWT.OK);
                result.setResult(text.getText());
                shell.close();
            }

        });
        shell.setDefaultButton(btnOK);

        btnCancel = new Button(shell, SWT.NONE);
        FormData fd_btnCancel = new FormData();
        fd_btnCancel.top = new FormAttachment(48, 10);
        fd_btnCancel.left = new FormAttachment(55, 10);
        fd_btnCancel.right = new FormAttachment(93, -10);
        fd_btnCancel.bottom = new FormAttachment(95, -10);
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
}
