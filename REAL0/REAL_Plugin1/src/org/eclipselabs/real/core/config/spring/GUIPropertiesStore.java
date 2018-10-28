package org.eclipselabs.real.core.config.spring;

import java.util.List;

import org.eclipselabs.real.gui.core.GUIProperty;

public class GUIPropertiesStore {

    private List<GUIProperty> guiProperties;

    public GUIPropertiesStore(List<GUIProperty> gps) {
        guiProperties = gps;
    }

    public List<GUIProperty> getGuiProperties() {
        return guiProperties;
    }

    public void setGuiProperties(List<GUIProperty> guiProperties) {
        this.guiProperties = guiProperties;
    }

}
