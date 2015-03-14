package org.eclipselabs.real.core.config.gui.xml.constructor;

import org.eclipselabs.real.core.config.gui.constructor.IDisplaySOSelectorConstructor;
import org.eclipselabs.real.core.config.gui.constructor.IGUIConstructorFactory;
import org.eclipselabs.real.core.config.gui.constructor.IPropertyConstructor;
import org.eclipselabs.real.core.config.gui.constructor.ISOTreeConstructor;
import org.eclipselabs.real.core.config.gui.constructor.ISortRequestKeyConstructor;
import org.w3c.dom.Node;

public class GUIXmlConstructorFactory implements
        IGUIConstructorFactory<Node> {

    private GUIXmlConstructorFactory() {}
    
    public static GUIXmlConstructorFactory getInstance() {
        return new GUIXmlConstructorFactory();
    }
    
    @Override
    public ISOTreeConstructor<Node> getSOTReeConstructor() {
        return new SOTreeXmlConstructorImpl();
    }

    @Override
    public IPropertyConstructor<Node> getPropertyConstructor() {
        return new PropertyXmlConstructor();
    }

    @Override
    public IDisplaySOSelectorConstructor<Node> getSelectorConstructor() {
        return new DisplaySOSelectorXmlConstructor();
    }

    @Override
    public ISortRequestKeyConstructor<Node> getSortRequestKeyConstructor() {
        return new SortRequestKeyXmlConstructor();
    }

}
