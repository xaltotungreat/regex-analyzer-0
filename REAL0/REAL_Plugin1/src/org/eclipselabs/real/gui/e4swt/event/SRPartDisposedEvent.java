package org.eclipselabs.real.gui.e4swt.event;

public class SRPartDisposedEvent {

    private String searchID;

    public SRPartDisposedEvent(String srID) {
        searchID = srID;
    }

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }
}
