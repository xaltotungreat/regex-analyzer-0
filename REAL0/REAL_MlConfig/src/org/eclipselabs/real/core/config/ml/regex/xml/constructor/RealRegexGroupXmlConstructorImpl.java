package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRealRegexGroupConstructor;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexGroup;
import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.regex.RegexFactory;
import org.w3c.dom.Node;

public class RealRegexGroupXmlConstructorImpl implements IRealRegexGroupConstructor<Node> {

    private static final Logger log = LogManager.getLogger(RealRegexGroupXmlConstructorImpl.class);
    
    @Override
    public IRealRegexGroup constructCO(IConstructionSource<Node> cSource) {
        IRealRegexGroup regGroup = null;
        try {
            if (XmlConfigNodeType.REGEX_GROUP.equalsNode(cSource.getSource())) {
             // a default value is provided for the cases where a name is required
                String groupName = IConfigXmlConstants.DEFAULT_REGEX_NAME;
                String parentGroup = null;
                if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
                    groupName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
                }
                if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_GROUP) != null) {
                    parentGroup = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_GROUP).getNodeValue();
                }
                regGroup = RegexFactory.INSTANCE.getRegexGroup(groupName);
                regGroup.setRegexGroup(parentGroup);
                List<IRealRegex> childRegexes = ConfigXmlUtil.collectAllRegex(cSource.getSource());
                if ((childRegexes != null) && (!childRegexes.isEmpty())) {
                    regGroup.getRegexList().addAll(childRegexes);
                    regGroup.setSelected(0);
                }
                
                List<Node> flagsNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REGEX_FLAGS);
                if ((flagsNodeList != null) && (!flagsNodeList.isEmpty())) {
                    regGroup.setRegexFlags(ConfigXmlUtil.getRegexFlags(flagsNodeList.get(0)));
                }
                List<IRealRegexParam<?>> paramLst = ConfigXmlUtil.collectRegexParams(cSource.getSource());
                if ((paramLst != null) && (!paramLst.isEmpty())) {
                    regGroup.putParameters(paramLst);
                }
            }
        } catch (Throwable t) {
            log.error("Error creating IRealRegex", t);
        }
        return regGroup;
    }

}
