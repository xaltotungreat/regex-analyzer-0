package org.eclipselabs.real.gui.e4swt.dialogs;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

public class SingleDialog extends Dialog {

    private IEclipseContext singleScopeContext;

    protected Class<?> classRef;
    protected Shell shell;

    public SingleDialog(Shell parent) {
        super(parent);
        // TODO Auto-generated constructor stub
    }

    public SingleDialog(Shell parent, int style) {
        super(parent, style);
        // TODO Auto-generated constructor stub
    }

    public IEclipseContext getSingleScopeContext() {
        return singleScopeContext;
    }

    public <T> void setSingleScopeContext(IEclipseContext singleScopeContext, Class<T> classRef, T objRef) {
        this.singleScopeContext = singleScopeContext;
        this.classRef = classRef;
        this.singleScopeContext.set(classRef, objRef);
    }

    public <T> void removeFromScopeContext() {
        singleScopeContext.remove(classRef);
    }

    public void registerContextRemoveOnClose() {
        if (shell != null) {
            shell.addShellListener(new ShellAdapter() {

                @Override
                public void shellClosed(ShellEvent e) {
                    removeFromScopeContext();
                }

            });
        }
    }

    public boolean makeActive() {
        boolean result = false;
        if ((shell != null) && (!shell.isDisposed())) {
            shell.open();
            result = true;
        }
         return result;
    }

    public Boolean isClosed() {
        boolean result = false;
        if ((shell == null) || (shell.isDisposed())) {
            result = true;
        }
        return result;
    }

    public void close() {
        shell.dispose();
    }



}
