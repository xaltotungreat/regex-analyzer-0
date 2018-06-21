package org.eclipselabs.real.core.config.spring;

import java.util.List;

import org.eclipselabs.real.gui.core.GUIProperty;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTemplate;

public class GUITemplateStore {

    List<IDisplaySOTemplate> templates;
    List<GUIProperty> guiProperties;

    public GUITemplateStore(List<IDisplaySOTemplate> a, List<GUIProperty> gps) {
        templates = a;
        guiProperties = gps;
    }

    public List<IDisplaySOTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<IDisplaySOTemplate> templates) {
        this.templates = templates;
    }

    public List<GUIProperty> getGuiProperties() {
        return guiProperties;
    }

    public void setGuiProperties(List<GUIProperty> guiProperties) {
        this.guiProperties = guiProperties;
    }

}
