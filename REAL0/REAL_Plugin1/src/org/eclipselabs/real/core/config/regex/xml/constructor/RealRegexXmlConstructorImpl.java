package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.regex.constructor.IRealRegexConstructor;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.regex.IMultiLineRegex;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.regex.ISimpleRegex;
import org.eclipselabs.real.core.regex.RegexFactory;
import org.w3c.dom.Node;

public class RealRegexXmlConstructorImpl implements IRealRegexConstructor<Node> {

    private static final Logger log = LogManager.getLogger(RealRegexXmlConstructorImpl.class);
    @Override
    public IRealRegex constructCO(IConstructionSource<Node> csNode) {
        IRealRegex rRegex = null;
        try {
            if (XmlConfigNodeType.REGEX.equalsNode(csNode.getSource())) {
                List<Node> regexLinesNodes = ConfigXmlUtil.collectChildNodes(csNode.getSource(), XmlConfigNodeType.REGEX_LINE);
                if ((regexLinesNodes != null) && (!regexLinesNodes.isEmpty())) {
                    int regexLineCount = regexLinesNodes.size();
                    // a default value is provided for the cases where a name is required
                    String regexName = IConfigXmlConstants.DEFAULT_REGEX_NAME;
                    String regexGroup = null;
                    if (csNode.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
                        regexName = csNode.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
                    }
                    if (csNode.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_GROUP) != null) {
                        regexGroup = csNode.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_GROUP).getNodeValue();
                    }
                    if (regexLineCount == 1) {
                        ISimpleRegex simpleRegex = RegexFactory.INSTANCE.getSimpleRegex(regexName);
                        simpleRegex.setRegexStr(regexLinesNodes.get(0).getTextContent());
                        rRegex = simpleRegex;
                        rRegex.setRegexGroup(regexGroup);
                    } else if (regexLineCount > 1) {
                        List<String> regLines = new ArrayList<String>();
                        for (Node node : regexLinesNodes) {
                            regLines.add(node.getTextContent());
                        }
                        IMultiLineRegex mlineRegex = RegexFactory.INSTANCE.getMultiLineRegex(regexName);
                        mlineRegex.setRegexStrings(regLines);
                        rRegex = mlineRegex;
                        rRegex.setRegexGroup(regexGroup);
                    }
                } else {
                    log.error("constructCO No regex lines returning null");
                    return rRegex;
                }
                
                List<Node> flagsNodeList = ConfigXmlUtil.collectChildNodes(csNode.getSource(), XmlConfigNodeType.REGEX_FLAGS);
                if ((flagsNodeList != null) && (!flagsNodeList.isEmpty())) {
                    rRegex.setRegexFlags(ConfigXmlUtil.getRegexFlags(flagsNodeList.get(0)));
                }
                
                List<IRealRegexParam<?>> paramLst = ConfigXmlUtil.collectRegexParams(csNode.getSource());
                if ((paramLst != null) && (!paramLst.isEmpty())) {
                    rRegex.putParameters(paramLst);
                }
            }
        } catch (Throwable t) {
            log.error("Error creating IRealRegex", t);
        }
        return rRegex;
    }

}

