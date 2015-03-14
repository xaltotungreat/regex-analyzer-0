package org.eclipselabs.real.gui.e4swt.dialogs;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

@Creatable
public class ProgressStatusDialog extends Dialog {

    private static final Logger log = LogManager.getLogger(ProgressStatusDialog.class);
    protected Object result;
    protected Shell shell;
    
    protected ProgressBar progressBar;
    protected Label lblStatus;
    
    int maxProgress;
    Boolean indeterminate;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    @Inject
    public ProgressStatusDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {
        super(parent, SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
        setText("Progress");
    }
    
    public ProgressStatusDialog(Shell parent, int styles, String txt) {
        super(parent, styles);
        setText(txt);
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public void open() {
        createContents();
        shell.open();
        //shell.setVisible(true);
        //shell.setEnabled(true);
        shell.layout();
        /*Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }*/
        //return result;
    }
    
    public void init(int aMaxProgress, Boolean aIndeterminate) {
        this.maxProgress = aMaxProgress;
        this.indeterminate = aIndeterminate;
    }
    
    public void setStatus(String newStatus) {
        lblStatus.setText(newStatus);
        lblStatus.redraw();
    }
    
    public synchronized void setProgress(int newProgress) {
        progressBar.setSelection(newProgress);
    }
    
    public synchronized int getProgress() {
        return progressBar.getSelection();
    }
    
    public synchronized void increaseProgress(int increaseValue) {
        if (progressBar.getSelection() + increaseValue <= progressBar.getMaximum()) {
            progressBar.setSelection(progressBar.getSelection() + increaseValue);
        } else {
            log.error("Value to increase too big " + increaseValue + " maximum=" + progressBar.getMaximum());
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(600, 140);
        shell.setText(getText());
        shell.setLayout(new FormLayout());
        if (indeterminate) {
            progressBar = new ProgressBar(shell, SWT.INDETERMINATE);
        } else {
            progressBar = new ProgressBar(shell, SWT.NONE);
        }
        if (maxProgress > 0) {
            progressBar.setMaximum(maxProgress);
        }
        FormData fd_progressBar = new FormData();
        fd_progressBar.right = new FormAttachment(100, -10);
        fd_progressBar.left = new FormAttachment(0, 10);
        fd_progressBar.bottom = new FormAttachment(40, 10);
        fd_progressBar.top = new FormAttachment(5, 10);
        progressBar.setLayoutData(fd_progressBar);
        
        lblStatus = new Label(shell, SWT.NONE);
        lblStatus.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.NORMAL));
        FormData fd_lblNewLabel = new FormData();
        fd_lblNewLabel.top = new FormAttachment(65, 5);
        fd_lblNewLabel.right = new FormAttachment(100, -10);
        fd_lblNewLabel.bottom = new FormAttachment(100, -5);
        fd_lblNewLabel.left = new FormAttachment(0, 10);
        lblStatus.setLayoutData(fd_lblNewLabel);
        lblStatus.setText("New Label");
    }
    
    public Shell getShell() {
        return shell;
    }
}
