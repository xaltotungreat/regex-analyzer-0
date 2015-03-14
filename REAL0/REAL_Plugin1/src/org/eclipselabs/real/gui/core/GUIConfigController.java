package org.eclipselabs.real.gui.core;

public enum GUIConfigController {
    INSTANCE;
    
    protected IGUIConfigRepository guiRep = new GUIConfigReposioryImpl();

    public IGUIConfigRepository getGUIObjectRepository() {
        return guiRep;
    }
}
