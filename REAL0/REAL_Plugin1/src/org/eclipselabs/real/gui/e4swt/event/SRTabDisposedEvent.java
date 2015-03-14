package org.eclipselabs.real.gui.e4swt.event;

public class SRTabDisposedEvent {

    private String searchID;

    private boolean tabForMainSearch;

    public SRTabDisposedEvent(String srID, boolean tabMain) {
        searchID = srID;
        tabForMainSearch = tabMain;
    }

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    public boolean isTabForMainSearch() {
        return tabForMainSearch;
    }

    public void setTabForMainSearch(boolean tabForMainSearch) {
        this.tabForMainSearch = tabForMainSearch;
    }
}
