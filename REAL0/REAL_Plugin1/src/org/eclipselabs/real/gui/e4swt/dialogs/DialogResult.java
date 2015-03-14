package org.eclipselabs.real.gui.e4swt.dialogs;

public class DialogResult<T> {

    private int action;
    private T result;

    public DialogResult() {

    }

    public DialogResult(int act) {
        action = act;
    }

    public DialogResult(int act, T val) {
        action = act;
        result = val;
    }

    public int getAction() {
        return action;
    }
    public void setAction(int action) {
        this.action = action;
    }
    public T getResult() {
        return result;
    }
    public void setResult(T result) {
        this.result = result;
    }

}
