package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import java.util.HashMap;
import java.util.Map;

import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.IComplexRegexViewConstructor;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.ml.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexGroup;
import org.eclipselabs.real.core.regex.RegexFactory;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.SOComplexRegexViewImpl;
import org.w3c.dom.Node;

public class ComplexRegexViewXmlConstructorImpl implements IComplexRegexViewConstructor<Node> {
    //private static final Logger log = LogManager.getLogger(ComplexRegexViewXmlConstructorImpl.class);
    @Override
    public ISOComplexRegexView constructCO(IConstructionSource<Node> cSource) {
        ISOComplexRegexView crView = null;
        if (XmlConfigNodeType.VIEW.equalsNode(cSource.getSource())) {
            crView = new SOComplexRegexViewImpl(cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue());
            //log.info("Complex Regex View name = " + cSource.getSource().getAttributes().getNamedItem(ConfigXmlUtil.ATTRIBUTE_NAME_NAME).getNodeValue());
            Node currNode = cSource.getSource().getFirstChild();
            IRealRegex newRegex = null;
            Map<String,IRealRegexGroup> groupMap = new HashMap<>();
            do {
                newRegex = null;
                if (XmlConfigNodeType.VIEW_STRING.equalsNode(currNode)) {
                    crView.add(currNode.getTextContent().replace("\\n", System.getProperty("line.separator")));
                } else if (XmlConfigNodeType.REGEX.equalsNode(currNode)) {
                    newRegex = (new RealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                    //crView.add((new RealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode)));
                } else if (XmlConfigNodeType.LINKED_REGEX.equalsNode(currNode)) {
                    newRegex = (new LinkedRealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                    //crView.add((new LinkedRealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode)));
                }
                if (newRegex != null) {
                    if (newRegex.getRegexGroup() != null) {
                        if (groupMap.containsKey(newRegex.getRegexGroup())) {
                            groupMap.get(newRegex.getRegexGroup()).addRegex(newRegex);
                        } else {
                            IRealRegexGroup newGroup = RegexFactory.INSTANCE.getRegexGroup(newRegex.getRegexGroup());
                            newGroup.addRegex(newRegex);
                            groupMap.put(newRegex.getRegexGroup(), newGroup);
                            crView.add(newGroup);
                        }
                    } else {
                        crView.add(newRegex);
                    }
                }
                currNode = currNode.getNextSibling();
            } while(currNode != null);
        }
        return crView;
    }
    
}
