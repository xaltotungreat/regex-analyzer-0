package org.eclipselabs.real.gui.core.sotree;

import java.util.HashMap;
import java.util.Map;

public abstract class DisplaySOTemplateAbstractImpl extends DisplaySOTreeItemImpl implements IDisplaySOTemplateAbstract {

    protected Map<String,Object> guiProperties = new HashMap<String,Object>();
    protected IDisplaySOTemplateAbstract parent;
    
    public DisplaySOTemplateAbstractImpl(DisplaySOTreeItemType aType, String aName) {
        super(aType, aName);
    }
    
    public DisplaySOTemplateAbstractImpl(DisplaySOTreeItemType aType, String aName, Boolean expandState) {
        super(aType, aName, expandState);
    }
    
    @Override
    public IDisplaySOTemplateAbstract getParent() {
        return parent;
    }

    @Override
    public void setParent(IDisplaySOTemplateAbstract prt) {
        parent = prt;
    }

    @Override
    public Map<String,Object> getGuiProperties() {
        return guiProperties;
    }

    @Override
    public void setGuiProperties(Map<String,Object> guiProperties) {
        this.guiProperties = guiProperties;
    }

}
