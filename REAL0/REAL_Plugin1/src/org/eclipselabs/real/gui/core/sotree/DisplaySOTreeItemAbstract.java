package org.eclipselabs.real.gui.core.sotree;

public class DisplaySOTreeItemAbstract implements IDisplaySOTreeItemAbstract {

    protected DisplaySOTreeItemType treeItemType;
    protected String name;
    
    public DisplaySOTreeItemAbstract(DisplaySOTreeItemType aType, String aName) {
        treeItemType = aType;
        name = aName;
    }

    @Override
    public DisplaySOTreeItemType getType() {
        return treeItemType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
