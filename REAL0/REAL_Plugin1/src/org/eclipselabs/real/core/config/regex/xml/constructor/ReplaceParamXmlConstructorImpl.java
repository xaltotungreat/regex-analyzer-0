package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.regex.constructor.IReplaceParamConstructor;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamImpl;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamValueType;
import org.eclipselabs.real.core.util.IRealCoreConstants;
import org.w3c.dom.Node;

public class ReplaceParamXmlConstructorImpl implements IReplaceParamConstructor<Node>, IConfigXmlConstants {

    private static final Logger log = LogManager.getLogger(ReplaceParamXmlConstructorImpl.class);

    @Override
    public IReplaceParam<?> constructCO(IConstructionSource<Node> cSource) {
        IReplaceParam<?> rpResult = null;
        if (XmlConfigNodeType.REPLACE_PARAM.equalsNode(cSource.getSource())) {
            String rpName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
            String rpValueStr = cSource.getSource().getTextContent();
            List<Node> replaceNameNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REPLACE_NAME);
            Set<String> replaceNameList = null;
            String rpDescr = null;
            if ((replaceNameNodes != null) && (!replaceNameNodes.isEmpty())) {
                replaceNameList = new HashSet<>();
                for (Node node : replaceNameNodes) {
                    if ((node.getTextContent() != null) && (!"".equals(node.getTextContent()))) {
                        replaceNameList.add(node.getTextContent());
                    }
                }

                List<Node> replaceDescrNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.DESCRIPTION);
                if ((replaceDescrNodes != null) && (!replaceDescrNodes.isEmpty())) {
                    rpDescr = replaceDescrNodes.get(0).getTextContent();
                }
                rpValueStr = "";
                List<Node> replaceValueNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REPLACE_VALUE);
                if ((replaceValueNodes != null) && (!replaceValueNodes.isEmpty())) {
                    rpValueStr = replaceValueNodes.get(0).getTextContent();
                }
            }
            ReplaceParamValueType valType = null;
            if ((cSource.getSource().getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE) != null)
                    && (cSource.getSource().getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue() != null)) {
                try {
                    valType = ReplaceParamValueType.valueOf(cSource.getSource().getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue().toUpperCase());
                } catch (IllegalArgumentException iae) {
                    log.error("getReplaceParamValueType No such element in ReplaceParamValueType "
                            + cSource.getSource().getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue()
                            + " Cannot process this ref node " + cSource.getSource(), iae);
                }
            }
            if ((rpName != null) && (rpValueStr != null)) {
                if (valType != null) {

                    switch (valType) {
                    case BOOLEAN:
                        Boolean boolVal = Boolean.parseBoolean(rpValueStr);
                        rpResult = new ReplaceParamImpl<Boolean>(valType, new ReplaceParamKey(rpName), rpDescr, replaceNameList, boolVal);
                        break;
                    case INTEGER:
                        try {
                            Integer intVal = Integer.parseInt(rpValueStr);
                            rpResult = new ReplaceParamImpl<Integer>(valType, new ReplaceParamKey(rpName), rpDescr, replaceNameList, intVal);
                        } catch (NumberFormatException nfe) {
                            log.error("Incorrect number format " + rpValueStr + ". Omitting this replace param", nfe);
                        }
                        break;
                    case DATE:
                        LocalDateTime cal = LocalDateTime.now().withNano(0);
                        // do not use milliseconds as they are not available in the UI time picker
                        DateTimeFormatter ftm = DateTimeFormatter.ofPattern(IReplaceParam.DEFAULT_FORMAT_STRING_LONG, IRealCoreConstants.MAIN_DATE_LOCALE);
                        try {
                            cal = LocalDateTime.parse(rpValueStr, ftm);
                        } catch (DateTimeParseException e) {
                            // check if the value is actually a hint
                            if (rpValueStr.equalsIgnoreCase(REPLACE_PARAM_VALUE_HINT_TODAY_START)) {
                                cal = cal.withHour(0).withMinute(0).withSecond(0);
                            } else if (rpValueStr.equalsIgnoreCase(REPLACE_PARAM_VALUE_HINT_TODAY_END)) {
                                cal = cal.withHour(23).withMinute(59).withSecond(59);
                            }
                        }
                        rpResult = new ReplaceParamImpl<LocalDateTime>(valType, new ReplaceParamKey(rpName), rpDescr, replaceNameList, cal);
                        break;
                    case STRING:
                    default:
                        rpResult = new ReplaceParamImpl<String>(new ReplaceParamKey(rpName), (rpDescr != null)?rpDescr:rpValueStr, replaceNameList, rpValueStr);
                        break;
                    }
                } else {
                    rpResult = new ReplaceParamImpl<String>(new ReplaceParamKey(rpName), rpValueStr, replaceNameList, rpValueStr);
                }
            } else {
                log.warn("constructCO Unable to construct param rpName=" + rpName + " rpValueStr=" + rpValueStr);
            }
            log.debug("Constructed ReplaceParam " + rpResult);
        }
        return rpResult;
    }

}
