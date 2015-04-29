package org.eclipselabs.real.gui.e4swt.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * This is a simple dialog with one label that covers the whole area
 * and one OK button. It is used instead of the standard MessageBox
 * because all MessageBox dialogs use annoying sounds.
 *
 * @author Vadim Korkin
 *
 */
public class SimpleMessageDialog extends Dialog {

    protected Object result;
    protected Shell shell;
    protected String caption;
    protected String labelText;


    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public SimpleMessageDialog(Shell parent, int style) {
        super(parent, style);
    }

    public SimpleMessageDialog(Shell parent) {
        super(parent, SWT.NONE);
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public Object open() {
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
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
        shell.setSize(450, 300);
        shell.setText(getCaption());
        shell.setLayout(new FormLayout());

        Button btnOK = new Button(shell, SWT.NONE);
        FormData fd_btnOK = new FormData();
        fd_btnOK.right = new FormAttachment(65, -5);
        fd_btnOK.bottom = new FormAttachment(100, -20);
        fd_btnOK.left = new FormAttachment(35, 5);
        btnOK.setLayoutData(fd_btnOK);
        btnOK.setText("OK");
        btnOK.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });

        Label lblText = new Label(shell, SWT.NONE);
        lblText.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
        FormData fd_lblText = new FormData();
        fd_lblText.bottom = new FormAttachment(100, -54);
        fd_lblText.right = new FormAttachment(100, -10);
        fd_lblText.top = new FormAttachment(0, 10);
        fd_lblText.left = new FormAttachment(0, 10);
        lblText.setLayoutData(fd_lblText);
        lblText.setText(getLabelText());

    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }


}
