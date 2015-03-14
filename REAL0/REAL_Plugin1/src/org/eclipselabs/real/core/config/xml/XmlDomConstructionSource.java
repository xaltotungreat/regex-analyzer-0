package org.eclipselabs.real.core.config.xml;

import org.eclipselabs.real.core.config.IConstructionSource;
import org.w3c.dom.Node;

public class XmlDomConstructionSource implements IConstructionSource<Node> {

	private Node theSource;
	
	public XmlDomConstructionSource(Node aSource) {
		theSource = aSource;
	}
	@Override
	public Node getSource() {
		return theSource;
	}

	@Override
	public void setSource(Node newSource) {
		theSource = newSource;
	}

}
