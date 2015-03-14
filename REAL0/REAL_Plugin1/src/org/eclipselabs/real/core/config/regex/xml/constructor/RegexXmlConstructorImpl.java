package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.util.List;

import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.searchobject.ISORegex;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RegexXmlConstructorImpl implements IRegexConstructor<Node> {

    @Override
    public ISORegex constructCO(IConstructionSource<Node> cSource) {
        IRegexConstructorFactory<Node> factory = RegexXmlConstructorFactoryImpl.getInstance();
        ISORegex result = SearchObjectFactory.getInstance().getSORegex(cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue());
        ConfigXmlUtil.constructKeyedSO(result, (Element)cSource.getSource());
        List<Node> regexesNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REGEX);
        if ((regexesNodeList != null) && (!regexesNodeList.isEmpty())) {
            result.setRegex(factory.getRealRegexConstructor().constructCO(new XmlDomConstructionSource(regexesNodeList.get(0))));
        }
        return result;
    }

}
