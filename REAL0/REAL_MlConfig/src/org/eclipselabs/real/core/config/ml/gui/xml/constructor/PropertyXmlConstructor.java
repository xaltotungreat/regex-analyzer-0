package org.eclipselabs.real.core.config.ml.gui.xml.constructor;

import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.gui.constructor.IPropertyConstructor;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.gui.core.GUIProperty;
import org.w3c.dom.Node;

public class PropertyXmlConstructor implements IPropertyConstructor<Node> {

    @Override
    public GUIProperty constructCO(IConstructionSource<Node> cSource) {
        GUIProperty property = null;
        if (XmlConfigNodeType.GUI_PROPERTY.equalsNode(cSource.getSource())) {
            property = new GUIProperty();
            property.setName(cSource.getSource().getAttributes().getNamedItem("name").getNodeValue());
            property.setValue(cSource.getSource().getTextContent());
        }
        return property;
    }

}
