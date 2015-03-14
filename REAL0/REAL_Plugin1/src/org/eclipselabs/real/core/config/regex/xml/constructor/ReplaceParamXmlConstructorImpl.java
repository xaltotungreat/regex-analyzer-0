package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                        Calendar cal = Calendar.getInstance();
                        // do not use milliseconds as they are not available in the UI time picker
                        cal.set(Calendar.MILLISECOND, 0);
                        SimpleDateFormat ftm = new SimpleDateFormat(IReplaceParam.DEFAULT_FORMAT_STRING_LONG, IRealCoreConstants.MAIN_DATE_LOCALE);
                        try {
                            cal.setTime(ftm.parse(rpValueStr));
                        } catch (ParseException e) {
                            // check if the value is actually a hint
                            if (rpValueStr.equalsIgnoreCase(REPLACE_PARAM_VALUE_HINT_TODAY_START)) {
                                cal.set(Calendar.HOUR_OF_DAY, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.SECOND, 0);
                            } else if (rpValueStr.equalsIgnoreCase(REPLACE_PARAM_VALUE_HINT_TODAY_END)) {
                                cal.set(Calendar.HOUR_OF_DAY, 23);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.SECOND, 59);
                            }
                        }
                        rpResult = new ReplaceParamImpl<Calendar>(valType, new ReplaceParamKey(rpName), rpDescr, replaceNameList, cal);
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
