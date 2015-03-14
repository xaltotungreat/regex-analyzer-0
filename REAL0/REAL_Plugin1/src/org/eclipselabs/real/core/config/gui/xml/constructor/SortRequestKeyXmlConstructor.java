package org.eclipselabs.real.core.config.gui.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.gui.constructor.ISortRequestKeyConstructor;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.eclipselabs.real.core.searchresult.sort.SortingType;
import org.eclipselabs.real.gui.core.sort.SortRequestKey;
import org.eclipselabs.real.gui.core.sort.SortRequestKeyParam;
import org.eclipselabs.real.gui.core.sort.SortRequestKeyParamType;
import org.eclipselabs.real.gui.core.sort.SortRequestKeyParamUseType;
import org.w3c.dom.Node;

public class SortRequestKeyXmlConstructor implements ISortRequestKeyConstructor<Node> {

    private static final Logger log = LogManager.getLogger(SortRequestKeyXmlConstructor.class);
    @Override
    public SortRequestKey constructCO(IConstructionSource<Node> cSource) {
        SortRequestKey resultKey = null;
        if ((XmlConfigNodeType.SORT_REQUEST_KEY.equalsNode(cSource.getSource())) 
                    && (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE) != null)) { 
            String typeStr = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE).getNodeValue();
            SortingType type = null;
            try {
                type = SortingType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Incorrect type for a sort request key " + typeStr + ". Omitting this sort request key", e);
            }
            if (type != null) {
                // if the type is not null load the params
                List<Node> allParamsNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.SORT_REQUEST_KEY_PARAM);
                List<SortRequestKeyParam<?>> paramLst = new ArrayList<>();
                for (Node node : allParamsNodes) {
                    if ((node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE) != null) 
                            && (node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_USE_TYPE) != null)) {
                        String paramTypeStr = node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE).getNodeValue();
                        String paramUseTypeStr = node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_USE_TYPE).getNodeValue();
                        SortRequestKeyParamType paramType = null;
                        SortRequestKeyParamUseType paramUseType = null;
                        try {
                            paramType = SortRequestKeyParamType.valueOf(paramTypeStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.error("Incorrect type for a sort request param type " + paramTypeStr + ". Omitting this param", e);
                        }
                        
                        try {
                            paramUseType = SortRequestKeyParamUseType.valueOf(paramUseTypeStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.error("Incorrect type for a sort request param useType " + paramUseTypeStr + ". Omitting this param", e);
                        }
                        if ((paramType != null) && (paramUseType != null)) {
                            String paramValueStr = node.getTextContent();
                            if ((paramValueStr != null) && (!"".equals(paramValueStr))) {
                                if (paramType.equals(SortRequestKeyParamType.SORT_APPLICABILITY)) {
                                    SortingApplicability appl = null;
                                    try {
                                        appl = SortingApplicability.valueOf(paramValueStr.toUpperCase());
                                        SortRequestKeyParam<SortingApplicability> newParam = new SortRequestKeyParam<SortingApplicability>(paramType, paramUseType, appl);
                                        paramLst.add(newParam);
                                    } catch (IllegalArgumentException e) {
                                        log.error("Incorrect type for a sort request param value " + paramValueStr + ". Omitting this param", e);
                                    }
                                    //SortRequestKeyParam<SortingApplicability> newParam = new 
                                } else if (paramType.equals(SortRequestKeyParamType.NAME)) {
                                    SortRequestKeyParam<String> newParam = new SortRequestKeyParam<String>(paramType, paramUseType, paramValueStr);
                                    paramLst.add(newParam);
                                }
                            }
                        }
                    }
                }
                if (paramLst.isEmpty()) {
                    paramLst = null;
                }
                resultKey = new SortRequestKey(type, paramLst);
            }
        }
        return resultKey;
    }

}
