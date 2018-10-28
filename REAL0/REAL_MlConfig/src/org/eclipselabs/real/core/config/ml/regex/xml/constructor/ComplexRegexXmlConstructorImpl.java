package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.IComplexRegexConstructor;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ComplexRegexXmlConstructorImpl implements
        IComplexRegexConstructor<Node> {
    //private static final Logger log = LogManager.getLogger(ComplexRegexXmlConstructorImpl.class);
    
    @Override
    public ISOComplexRegex constructCO(IConstructionSource<Node> cSource) {
        ISOComplexRegex crResult = null;
        if (XmlConfigNodeType.COMPLEX_REGEX.equalsNode(cSource.getSource())) {
            crResult = SearchObjectFactory.getInstance().getSOComplexRegex(cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue());
            ConfigXmlUtil.constructKeyedSO(crResult, (Element)cSource.getSource());
            
            List<Node> regexesNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REGEXES);
            List<IRealRegex> tmpList = new ArrayList<IRealRegex>();
            for (Node node : regexesNodeList) {
                tmpList.addAll(ConfigXmlUtil.collectAllRegex(node));
            }
            crResult.getMainRegexList().addAll(tmpList);
            ConfigXmlUtil.collectViews(crResult, cSource.getSource());
        }
        return crResult;
    }

}
