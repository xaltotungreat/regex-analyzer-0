package org.eclipselabs.real.core.distrib;

public class GenericError {

    private Throwable causedBy;

    public GenericError(Throwable t) {
        causedBy = t;
    }

    public Throwable getCausedBy() {
        return causedBy;
    }

    public void setCausedBy(Throwable causedBy) {
        this.causedBy = causedBy;
    }

}
