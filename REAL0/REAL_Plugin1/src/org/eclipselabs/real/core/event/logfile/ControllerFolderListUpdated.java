package org.eclipselabs.real.core.event.logfile;

import java.util.ArrayList;
import java.util.List;

public class ControllerFolderListUpdated extends LogFileEventImpl {

    protected List<String> oldFolders = new ArrayList<>();
    protected List<String> newFolders = new ArrayList<>();
    
    public ControllerFolderListUpdated(List<String> oldFldList, List<String> newFldList) {
        super();
        oldFolders = oldFldList;
        newFolders = newFldList;
    }

    public List<String> getOldFolders() {
        return oldFolders;
    }

    public void setOldFolders(List<String> oldFolders) {
        this.oldFolders = oldFolders;
    }

    public List<String> getNewFolders() {
        return newFolders;
    }

    public void setNewFolders(List<String> newFolders) {
        this.newFolders = newFolders;
    }


}
