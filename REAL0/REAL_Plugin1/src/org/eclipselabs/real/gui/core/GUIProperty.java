package org.eclipselabs.real.gui.core;

import org.eclipselabs.real.core.util.NameValuePair;

public class GUIProperty extends NameValuePair<String, String> {

    public GUIProperty() {
        
    }
    
    public GUIProperty(String aName, String aValue) {
        super(aName, aValue);
    }
    
    @Override
    public String toString() {
        return "GUIProperty [name=" + name + ", value=" + value + "]";
    }
}
