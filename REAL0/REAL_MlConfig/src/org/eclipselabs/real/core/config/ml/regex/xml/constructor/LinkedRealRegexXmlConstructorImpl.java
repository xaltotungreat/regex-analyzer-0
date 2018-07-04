package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.ILinkedRealRegexConstructor;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.regex.ILinkedRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.regex.RegexFactory;
import org.w3c.dom.Node;

public class LinkedRealRegexXmlConstructorImpl implements
        ILinkedRealRegexConstructor<Node> {

    private static final Logger log = LogManager.getLogger(LinkedRealRegexXmlConstructorImpl.class);
    
    @Override
    public ILinkedRealRegex constructCO(IConstructionSource<Node> cSource) {
        ILinkedRealRegex lnkRegex = null;
        try {
            if (XmlConfigNodeType.LINKED_REGEX.equalsNode(cSource.getSource())) {
                // a default value is provided for the cases where a name is required
                String regexName = IConfigXmlConstants.DEFAULT_REGEX_NAME;
                String regexGroup = null;
                if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
                    regexName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
                }
                if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_GROUP) != null) {
                    regexGroup = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_GROUP).getNodeValue();
                }
                lnkRegex = RegexFactory.INSTANCE.getLinkedRegex(regexName);
                lnkRegex.setRegexList(ConfigXmlUtil.collectRegex(cSource.getSource()));
                lnkRegex.setRegexGroup(regexGroup);
                List<Node> flagsNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REGEX_FLAGS);
                if ((flagsNodeList != null) && (!flagsNodeList.isEmpty())) {
                    lnkRegex.setRegexFlags(ConfigXmlUtil.getRegexFlags(flagsNodeList.get(0)));
                }
                List<IRealRegexParam<?>> paramLst = ConfigXmlUtil.collectRegexParams(cSource.getSource());
                if ((paramLst != null) && (!paramLst.isEmpty())) {
                    lnkRegex.putParameters(paramLst);
                }
            }
        } catch (Throwable t) {
            log.error("Error creating IRealRegex", t);
        }
        return lnkRegex;
    }

}
