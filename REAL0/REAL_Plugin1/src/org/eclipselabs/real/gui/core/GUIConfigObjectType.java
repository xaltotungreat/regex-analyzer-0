package org.eclipselabs.real.gui.core;

public enum GUIConfigObjectType {
    SEARCH_OBJECT_TREE("Search Object Tree"),
    GUI_PROPERTY("GUI Property");
    
    protected String typeName;
    
    public String getTypeName() {
        return typeName;
    }

    private GUIConfigObjectType(String aTypeName) {
        typeName = aTypeName;
    }
}
