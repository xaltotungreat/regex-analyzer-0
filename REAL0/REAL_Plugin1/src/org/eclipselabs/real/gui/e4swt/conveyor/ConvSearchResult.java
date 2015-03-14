package org.eclipselabs.real.gui.e4swt.conveyor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.gui.core.util.SearchInfo;
import org.eclipselabs.real.gui.e4swt.parts.GUISearchResult;


public class ConvSearchResult {

    private static final Logger log = LogManager.getLogger(ConvSearchResult.class);

    private SearchInfo searchInfo;

    private volatile GUISearchResult guiObjectRef;

    public static ConvSearchResult getFromContext(ConvProductContext ctxt) {
        log.info("getFromContext CALLED");
        ConvSearchResult convRes = new ConvSearchResult();
        convRes.searchInfo = ctxt.getSearchInfo();
        convRes.guiObjectRef = ctxt.getGuiObjectRef();
        return new ConvSearchResult();
    }

    public SearchInfo getSearchInfo() {
        return searchInfo;
    }

    public void setSearchInfo(SearchInfo searchInfo) {
        this.searchInfo = searchInfo;
    }

    public GUISearchResult getGuiObjectRef() {
        return guiObjectRef;
    }

    public void setGuiObjectRef(GUISearchResult guiObjectRef) {
        this.guiObjectRef = guiObjectRef;
    }
}
