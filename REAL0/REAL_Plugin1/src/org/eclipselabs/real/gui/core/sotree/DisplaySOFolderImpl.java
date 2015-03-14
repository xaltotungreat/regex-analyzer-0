package org.eclipselabs.real.gui.core.sotree;


public class DisplaySOFolderImpl extends DisplaySOTreeItemImpl implements IDisplaySOFolder {

    public DisplaySOFolderImpl(String aName, Boolean expandState) {
        super(DisplaySOTreeItemType.FOLDER, aName, expandState);
    }

}
