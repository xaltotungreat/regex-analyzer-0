package org.eclipselabs.real.gui.core.sotree;

public class DisplaySORoot extends DisplaySOTreeItemImpl implements
        IDisplaySORoot {

    public DisplaySORoot(String aName, Boolean expandState) {
        super(DisplaySOTreeItemType.ROOT, aName, expandState);
    }

}
