package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.regex.constructor.IRegexSortRequestConstructor;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.sort.IRegexSortRequest;
import org.eclipselabs.real.core.searchresult.sort.SortRequestFactory;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.w3c.dom.Node;

public class RegexSortRequestXmlConstructor implements IRegexSortRequestConstructor<Node> {

    private static final Logger log = LogManager.getLogger(RegexSortRequestXmlConstructor.class);
    @Override
    public IRegexSortRequest constructCO(IConstructionSource<Node> cSource) {
        String reqName = null;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
            reqName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
        }
        SortingApplicability sortAppl = SortingApplicability.NONE;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_APPLICABILITY) != null) {
            String sortApplStr = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_APPLICABILITY).getNodeValue();
            if ((sortApplStr != null) && (!"".equals(sortApplStr))) {
                try {
                    sortAppl = SortingApplicability.valueOf(sortApplStr);
                } catch (IllegalArgumentException iae) {
                    log.error("No such element in SortingApplicability " + sortApplStr + " Setting to NONE", iae);
                    sortAppl = SortingApplicability.NONE;
                }
            }
        }
        
        IRegexSortRequest resultReq = SortRequestFactory.getInstance().getRegexSortRequest(reqName);
        resultReq.setSortApplicability(sortAppl);
        List<Node> allRegexesNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REGEXES);
        List<IRealRegex> allRealReg = new ArrayList<>();
        for (Node regexesNode : allRegexesNodes) {
            allRealReg.addAll(ConfigXmlUtil.collectAllRegex(regexesNode));
        }
        resultReq.setSortRegexList(allRealReg);
        int resRegexFlags = 0;
        List<Node> allregexFlNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REGEX_FLAGS);
        for (Node flagsNode : allregexFlNodes) {
            resRegexFlags = resRegexFlags | ConfigXmlUtil.getRegexFlags(flagsNode);
        }
        if (resRegexFlags != 0) {
            resultReq.setRegexFlags(resRegexFlags);
        }
        return resultReq;
    }

}
