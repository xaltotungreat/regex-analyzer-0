package org.eclipselabs.real.gui.core.sotree;


public class DisplaySOTreeItemImpl extends DisplaySOTreeItemAbstract implements IDisplaySOTreeItem {
    
    protected Boolean expanded;
    
    public DisplaySOTreeItemImpl(DisplaySOTreeItemType aType, String aName) {
        this(aType, aName, false);
    }
    
    public DisplaySOTreeItemImpl(DisplaySOTreeItemType aType, String aName, Boolean expandState) {
        super(aType, aName);
        expanded = expandState;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }
    
}
