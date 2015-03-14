package org.eclipselabs.real.core.util;

public class RepositorySizeChangedEvent {

    protected Integer oldSize;
    protected Integer newSize;
    
    public RepositorySizeChangedEvent(int aOldSize, int aNewSize) {
        oldSize = aOldSize;
        newSize = aNewSize;
    }

    public Integer getOldSize() {
        return oldSize;
    }

    public void setOldSize(Integer oldSize) {
        this.oldSize = oldSize;
    }

    public Integer getNewSize() {
        return newSize;
    }

    public void setNewSize(Integer newSize) {
        this.newSize = newSize;
    }

}
