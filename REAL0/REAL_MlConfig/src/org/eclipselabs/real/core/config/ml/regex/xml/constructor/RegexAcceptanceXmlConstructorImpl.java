package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRegexAcceptanceConstructor;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionFactory;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionType;
import org.eclipselabs.real.core.searchobject.crit.IRegexAcceptanceCriterion;
import org.w3c.dom.Node;

public class RegexAcceptanceXmlConstructorImpl implements IRegexAcceptanceConstructor<Node> {

    private static final Logger log  = LogManager.getLogger(RegexAcceptanceXmlConstructorImpl.class);
    @Override
    public IRegexAcceptanceCriterion constructCO(IConstructionSource<Node> cSource) {
        String critName = null;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
            critName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
        }
        AcceptanceCriterionType acceptType = null;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE) != null) {
            String sortApplStr = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE).getNodeValue();
            if ((sortApplStr != null) && (!"".equals(sortApplStr))) {
                try {
                    acceptType = AcceptanceCriterionType.valueOf(sortApplStr);
                } catch (IllegalArgumentException iae) {
                    log.error("NO such element in AcceptanceCriterionType " + sortApplStr + " unable to process this criterion", iae);
                }
            }
        }
        IRegexAcceptanceCriterion crit = null;
        if (acceptType != null) {
            crit = AcceptanceCriterionFactory.getInstance().getRegexAcceptanceCriterion(acceptType);
            crit.setName(critName);
            List<Node> regexesNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REGEXES);
            List<IRealRegex> tmpList = new ArrayList<IRealRegex>();
            for (Node node : regexesNodeList) {
                tmpList.addAll(ConfigXmlUtil.collectAllRegex(node));
            }
            crit.getAcceptanceRegexList().addAll(tmpList);
        }
        return crit;
    }

}
