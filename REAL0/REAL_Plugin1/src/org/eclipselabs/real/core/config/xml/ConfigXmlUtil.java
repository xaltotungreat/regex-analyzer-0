package org.eclipselabs.real.core.config.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.regex.constructor.IComplexRegexViewConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IDTIntervalAcceptanceConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexAcceptanceConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.regex.constructor.ISOConstructor;
import org.eclipselabs.real.core.config.regex.xml.constructor.LinkedRealRegexXmlConstructorImpl;
import org.eclipselabs.real.core.config.regex.xml.constructor.RealRegexGroupXmlConstructorImpl;
import org.eclipselabs.real.core.config.regex.xml.constructor.RealRegexXmlConstructorImpl;
import org.eclipselabs.real.core.config.regex.xml.constructor.RegexXmlConstructorFactoryImpl;
import org.eclipselabs.real.core.config.regex.xml.constructor.ReplaceParamXmlConstructorImpl;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexGroup;
import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.regex.RealRegexParamBoolean;
import org.eclipselabs.real.core.regex.RealRegexParamIRealRegex;
import org.eclipselabs.real.core.regex.RealRegexParamInteger;
import org.eclipselabs.real.core.regex.RealRegexParamString;
import org.eclipselabs.real.core.regex.RealRegexParamType;
import org.eclipselabs.real.core.regex.RegexFactory;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISearchObjectDateInfo;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.crit.IDTIntervalCriterion;
import org.eclipselabs.real.core.searchobject.crit.IRegexAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.ref.RefAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.ref.RefDateInfo;
import org.eclipselabs.real.core.searchobject.ref.RefInternalSortRequest;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchobject.ref.RefParamInteger;
import org.eclipselabs.real.core.searchobject.ref.RefRealRegex;
import org.eclipselabs.real.core.searchobject.ref.RefRealRegexParam;
import org.eclipselabs.real.core.searchobject.ref.RefReplaceParam;
import org.eclipselabs.real.core.searchobject.ref.RefType;
import org.eclipselabs.real.core.searchobject.ref.RefView;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains many static methods to work with the XML config files.
 * They are necessary to simplify construction of search objects
 *
 * @author Vadim Korkin
 *
 */
public class ConfigXmlUtil implements IConfigXmlConstants {
    private static final Logger log  = LogManager.getLogger(ConfigXmlUtil.class);

    private ConfigXmlUtil() {}

    public static List<IReplaceParam<?>> collectReplaceParams(Node elem) {
        List<IReplaceParam<?>> result = new ArrayList<IReplaceParam<?>>();
        List<Node> regNodeList = collectChildNodes(elem, XmlConfigNodeType.REPLACE_PARAM);
        if ((regNodeList != null) && (!regNodeList.isEmpty())) {
            for (Node currNode : regNodeList) {
                if (XmlConfigNodeType.REPLACE_PARAM.equalsNode(currNode) && currNode.getParentNode().equals(elem)) {
                    IReplaceParam<?> constructedParam = (new ReplaceParamXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                    if (constructedParam != null) {
                        result.add(constructedParam);
                    }
                }
            }
        }
        return result;
    }

    public static List<RefReplaceParam> collectRefReplaceParams(Node elem) {
        List<RefReplaceParam> allRefRPList = new ArrayList<>();
        List<Node> refRPNodeList = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_REPLACE_PARAM);
        for (Node refRPNode : refRPNodeList) {
            String refRPName = getRefParamName(refRPNode);
            RefType rpRefType = getRefParamType(refRPNode);
            if (rpRefType == null) {
                log.error("No param type available. Cannot process this ref replace param " + refRPName);
                continue;
            }

            RefReplaceParam newRefRP = new RefReplaceParam(rpRefType, refRPName);
            List<IReplaceParam<?>> params = ConfigXmlUtil.collectReplaceParams(refRPNode);
            if ((params != null) && (!params.isEmpty())) {
                if (newRefRP.getValue() == null) {
                    newRefRP.setValue(new ArrayList<IReplaceParam<?>>());
                }
                for (IReplaceParam<?> rp : params) {
                    newRefRP.addReplaceParam(rp);
                }
                allRefRPList.add(newRefRP);
            } else {
                log.warn("Empty replace param list for ref replace param " + newRefRP
                        + " this ref replace param is not processed");
            }
        }
        return allRefRPList;
    }

    public static Integer getRegexFlags(Node flagsNode) {
        int resultFlags = 0;
        List<Node> regNodeList = collectChildNodes(flagsNode, XmlConfigNodeType.REGEX_FLAG);
        for (Node flagNode : regNodeList) {
            if (flagNode.getTextContent() != null) {
                if (flagNode.getTextContent().equals(REGEX_FLAG_UNIX_LINES)) {
                    resultFlags = resultFlags | Pattern.UNIX_LINES;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_CASE_INSENSITIVE)) {
                    resultFlags = resultFlags | Pattern.CASE_INSENSITIVE;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_COMMENTS)) {
                    resultFlags = resultFlags | Pattern.COMMENTS;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_MULTILINE)) {
                    resultFlags = resultFlags | Pattern.MULTILINE;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_UNICODE_CASE)) {
                    resultFlags = resultFlags | Pattern.UNICODE_CASE;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_LITERAL)) {
                    resultFlags = resultFlags | Pattern.LITERAL;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_DOTALL)) {
                    resultFlags = resultFlags | Pattern.DOTALL;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_CANON_EQ)) {
                    resultFlags = resultFlags | Pattern.CANON_EQ;
                } else if (flagNode.getTextContent().equals(REGEX_FLAG_UNICODE_CHARACTER_CLASS)) {
                    resultFlags = resultFlags | Pattern.UNICODE_CHARACTER_CLASS;
                }
            }
        }
        return resultFlags;
    }

    public static String getRefParamName(Node refNode) {
        String refRPName = null;
        if (refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_NAME) != null) {
            refRPName = refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue();
        }
        return refRPName;
    }

    public static RefType getRefParamType(Node refNode) {
        RefType rpRefType = null;
        if ((refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE) != null)
                && (refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue() != null)) {
            try {
                rpRefType = RefType.valueOf(refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue().toUpperCase());
            } catch (IllegalArgumentException iae) {
                log.error("getRefParamType No such element in RefType "
                        + refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue()
                        + " Cannot process this ref node " + refNode, iae);
            }
        } else {
            log.error("getRefParamType No param type available " + refNode);
        }
        return rpRefType;
    }

    public static Integer getRefParamPosition(Node refNode) {
        Integer position = null;
        if ((refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_POSITION) != null)
                && (refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_POSITION).getNodeValue() != null)) {
            try {
                position = Integer.parseInt(refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_POSITION).getNodeValue());
            } catch (NumberFormatException iae) {
                log.error("Incorrect number " + refNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_POSITION).getNodeValue()
                        + " No position assigned ", iae);
            }
        }
        return position;
    }

    public static List<IRealRegex> collectRegex(Node elem) {
        List<IRealRegex> result = new ArrayList<IRealRegex>();
        if (elem.hasChildNodes()) {
            Node currNode = elem.getFirstChild();
            IRealRegex newRegex = null;
            Map<String,IRealRegexGroup> groupMap = new HashMap<>();
            do {
                newRegex = null;
                if (XmlConfigNodeType.REGEX.equalsNode(currNode)) {
                    newRegex = (new RealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                } else if (XmlConfigNodeType.REGEX_GROUP.equalsNode(currNode)) {
                    newRegex = (new RealRegexGroupXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                }
                if (newRegex != null) {
                    if (newRegex.getRegexGroup() != null) {
                        if (groupMap.containsKey(newRegex.getRegexGroup())) {
                            groupMap.get(newRegex.getRegexGroup()).addRegex(newRegex);
                        } else {
                            IRealRegexGroup newGroup = RegexFactory.INSTANCE.getRegexGroup(newRegex.getRegexGroup());
                            newGroup.addRegex(newRegex);
                            groupMap.put(newRegex.getRegexGroup(), newGroup);
                            result.add(newGroup);
                        }
                    } else {
                        result.add(newRegex);
                    }
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return result;
    }

    public static Map<String,IRealRegex> collectNamedRegex(Node elem) {
        Map<String,IRealRegex> result = new HashMap<String,IRealRegex>();
        if (elem.hasChildNodes()) {
            Node currNode = elem.getFirstChild();
            IRealRegex newRegex = null;
            Map<String,IRealRegexGroup> groupMap = new HashMap<>();
            do {
                newRegex = null;
                if (XmlConfigNodeType.REGEX.equalsNode(currNode)) {
                    newRegex = (new RealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                } else if (XmlConfigNodeType.REGEX_GROUP.equalsNode(currNode)) {
                    newRegex = (new RealRegexGroupXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                }
                if (newRegex != null) {
                    if (newRegex.getRegexGroup() != null) {
                        if (groupMap.containsKey(newRegex.getRegexGroup())) {
                            groupMap.get(newRegex.getRegexGroup()).addRegex(newRegex);
                        } else {
                            IRealRegexGroup newGroup = RegexFactory.INSTANCE.getRegexGroup(newRegex.getRegexGroup());
                            newGroup.addRegex(newRegex);
                            groupMap.put(newRegex.getRegexGroup(), newGroup);
                            result.put(newRegex.getRegexGroup(), newGroup);
                        }
                    } else {
                        result.put(newRegex.getRegexName(), newRegex);
                    }
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return result;
    }

    /**
     * The method collect both the linked regex and regex elements
     * @param elem the parent node for the regex nodes
     * @return a List of IRealRegex
     */
    public static List<IRealRegex> collectAllRegex(Node elem) {
        List<IRealRegex> result = new ArrayList<IRealRegex>();
        if (elem.hasChildNodes()) {
            Node currNode = elem.getFirstChild();
            IRealRegex newRegex = null;
            Map<String,IRealRegexGroup> groupMap = new HashMap<>();
            do {
                newRegex = null;
                if (XmlConfigNodeType.REGEX.equalsNode(currNode)) {
                    newRegex = (new RealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                } else if (XmlConfigNodeType.LINKED_REGEX.equalsNode(currNode)) {
                    newRegex = (new LinkedRealRegexXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                } else if (XmlConfigNodeType.REGEX_GROUP.equalsNode(currNode)) {
                    newRegex = (new RealRegexGroupXmlConstructorImpl()).constructCO(new XmlDomConstructionSource(currNode));
                }
                if (newRegex != null) {
                    // add to the group not to the main list if it has a non-null group
                    if (newRegex.getRegexGroup() != null) {
                        if (groupMap.containsKey(newRegex.getRegexGroup())) {
                            groupMap.get(newRegex.getRegexGroup()).addRegex(newRegex);
                        } else {
                            IRealRegexGroup newGroup = RegexFactory.INSTANCE.getRegexGroup(newRegex.getRegexGroup());
                            newGroup.addRegex(newRegex);
                            groupMap.put(newRegex.getRegexGroup(), newGroup);
                            result.add(newGroup);
                        }
                    } else {
                        result.add(newRegex);
                    }
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return result;
    }

    public static List<IRealRegexParam<?>> collectRegexParams(Node regexElem) {
        List<IRealRegexParam<?>> resultList = new ArrayList<>();
        List<Node> regexParamNodes = ConfigXmlUtil.collectChildNodes(regexElem, XmlConfigNodeType.REGEX_PARAM);
        if ((regexParamNodes != null) && (!regexParamNodes.isEmpty())) {
            for (Node node : regexParamNodes) {
                RealRegexParamType paramType = null;
                if ((node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE) != null)
                        && (node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE).getNodeValue() != null)) {
                    try {
                        paramType = RealRegexParamType.valueOf(node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE).getNodeValue().toUpperCase());
                        String paramName = null;
                        if (node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
                            paramName = node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
                        } else {
                            log.error("Incorrect name attribute for this real regex param");
                        }
                        if ((paramType != null) && (paramName != null) && (node.getTextContent() != null)) {
                            switch(paramType) {
                            case BOOLEAN:
                                RealRegexParamBoolean booleanParam = RegexFactory.INSTANCE.getRealRegexBooleanParam();
                                booleanParam.setName(paramName);
                                booleanParam.setValue(Boolean.parseBoolean(node.getTextContent().toUpperCase()));
                                resultList.add(booleanParam);
                                break;
                            case INTEGER:
                                try {
                                    Integer intVal = Integer.parseInt(node.getTextContent());
                                    RealRegexParamInteger integerParam = RegexFactory.INSTANCE.getRealRegexIntegerParam();
                                    integerParam.setName(paramName);
                                    integerParam.setValue(intVal);
                                    resultList.add(integerParam);
                                } catch (NumberFormatException nfe) {
                                    log.error("Incorrect number format " + node.getTextContent() + ". Omitting this param", nfe);
                                }
                                break;
                            case STRING:
                                RealRegexParamString stringParam = RegexFactory.INSTANCE.getRealRegexStringParam();
                                stringParam.setName(paramName);
                                stringParam.setValue(node.getTextContent());
                                resultList.add(stringParam);
                                break;
                            case REGEX:
                                List<IRealRegex> allRegexes = ConfigXmlUtil.collectAllRegex(node);
                                if ((allRegexes != null) && (!allRegexes.isEmpty())) {
                                    RealRegexParamIRealRegex realRegParam = RegexFactory.INSTANCE.getRealRegexIRealRegexParam();
                                    realRegParam.setName(paramName);
                                    realRegParam.setValue(allRegexes.get(0));
                                    resultList.add(realRegParam);
                                } else {
                                    log.error("No regexes collected for param " + paramName);
                                }
                                break;
                            case REGEX_LIST:
                                log.error("The type " + RealRegexParamType.REGEX_LIST + " is not yet supported");
                                break;
                            default:
                                log.error("Incorrect param type " + paramType + " name=" + paramName + " text=" + node.getTextContent());
                                break;
                            }
                        } else {
                            log.error("Null values for param type=" + paramType + " name=" + paramName + " text=" + node.getTextContent());
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Incorrect real regex param type for " + node.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE).getNodeValue(), e);
                    }
                } else {
                    log.error("Incorrect type attribute. Omitting this param");
                }
            }
        }
        return resultList;
    }

    public static List<RefRealRegex> collectRefRegex(Node elem) {
        List<RefRealRegex> allRefRegex = new ArrayList<>();
        if (elem.hasChildNodes()) {
            Node currNode = elem.getFirstChild();
            do {
                if (XmlConfigNodeType.REF_REGEX.equalsNode(currNode)) {
                    String refName = getRefParamName(currNode);
                    RefType refType = getRefParamType(currNode);
                    if (refType == null) {
                        log.error("No param type available. Cannot process this ref regex " + refName);
                        continue;
                    }
                    Integer position = getRefParamPosition(currNode);
                    RefRealRegex newRefRegex = new RefRealRegex(refType, refName, position);
                    List<IRealRegex> refRegexList = ConfigXmlUtil.collectAllRegex(currNode);
                    List<RefRealRegexParam> refParams = collectRefRegexParams(currNode);
                    List<RefParamInteger> refFlags = collectRefRegexFlags(currNode);
                    if ((refRegexList != null) && (!refRegexList.isEmpty())) {
                        newRefRegex.setValue(refRegexList.get(0));
                        // add to the list only if the IRealRegex if found
                        allRefRegex.add(newRefRegex);
                    } else if ((!refParams.isEmpty()) || (!refFlags.isEmpty())) {
                        newRefRegex.setRefParamList(refParams);
                        newRefRegex.setRefRegexFlags(refFlags);
                        allRefRegex.add(newRefRegex);
                    } else {
                        log.warn("No IRealRegex found for the ref real regex and no internal ref params" + refName
                                + " cannot process this ref real regex");
                    }
                }
                // processing complete moving to the next node
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return allRefRegex;
    }

    public static List<RefParamInteger> collectRefRegexFlags(Node refNode) {
        List<RefParamInteger> collectedRefFlags = new ArrayList<>();
        List<Node> allRegexFlags = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_REGEX_FLAGS);
        if ((allRegexFlags != null) && (!allRegexFlags.isEmpty())) {
            Iterator<Node> regexFlagIter = allRegexFlags.iterator();
            while (regexFlagIter.hasNext()) {
                Node refFlagsNode = regexFlagIter.next();

                String refRPName = getRefParamName(refFlagsNode);
                RefType rpRefType = getRefParamType(refFlagsNode);
                if (rpRefType == null) {
                    log.error("No param type available. Cannot process this ref replace param " + refRPName);
                    continue;
                }
                RefParamInteger newRefRegexFlags = new RefParamInteger(rpRefType, refRPName);
                List<Node> flagsNodeList = ConfigXmlUtil.collectChildNodes(refFlagsNode, XmlConfigNodeType.REGEX_FLAGS);
                Integer endFlags = 0;
                for (Node nodeFlags : flagsNodeList) {
                    endFlags |= getRegexFlags(nodeFlags);
                }
                newRefRegexFlags.setValue(endFlags);
                collectedRefFlags.add(newRefRegexFlags);
            }
        }
        return collectedRefFlags;
    }

    public static List<RefRealRegexParam> collectRefRegexParams(Node refNode) {
        List<RefRealRegexParam> collectedRefParams = new ArrayList<>();
        List<Node> allRegexFlags = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_REGEX_PARAM);
        if ((allRegexFlags != null) && (!allRegexFlags.isEmpty())) {
            Iterator<Node> regexFlagIter = allRegexFlags.iterator();
            while (regexFlagIter.hasNext()) {
                Node refFlagsNode = regexFlagIter.next();

                String refRPName = getRefParamName(refFlagsNode);
                RefType rpRefType = getRefParamType(refFlagsNode);
                if (rpRefType == null) {
                    log.error("No param type available. Cannot process this ref replace param " + refRPName);
                    continue;
                }
                RefRealRegexParam newRefRegexFlags = new RefRealRegexParam(rpRefType, refRPName);
                List<IRealRegexParam<?>> allRegParams = ConfigXmlUtil.collectRegexParams(refFlagsNode);
                newRefRegexFlags.setValue(allRegParams);
                collectedRefParams.add(newRefRegexFlags);
            }
        }
        return collectedRefParams;
    }

    public static List<Node> collectChildNodes(Node parentNode, XmlConfigNodeType nodeType) {
        List<Node> childNodes = new ArrayList<Node>();
        NodeList regNodeList = ((Element)parentNode).getElementsByTagName(nodeType.getNodeName());
        if ((regNodeList != null) && (regNodeList.getLength() > 0)) {
            for (int i = 0; i < regNodeList.getLength(); i++) {
                Node currNode = regNodeList.item(i);
                if (currNode.getParentNode().equals(parentNode)) {
                    childNodes.add(currNode);
                }
            }
        }
        return childNodes;
    }

    public static List<Node> collectChildNodes(Node parentNode, XmlConfigNodeType... nodeTypes) {
        List<Node> childNodes = new ArrayList<Node>();
        if (parentNode.hasChildNodes()) {
            Node currNode = parentNode.getFirstChild();
            do {
                for (XmlConfigNodeType currType : nodeTypes) {
                    if (currType.equalsNode(currNode)) {
                        childNodes.add(currNode);
                    }
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return childNodes;
    }

    public static List<Node> collectChildNodes(Node parentNode, String nodeName) {
        List<Node> childNodes = new ArrayList<Node>();
        NodeList regNodeList = ((Element)parentNode).getElementsByTagName(nodeName);
        if ((regNodeList != null) && (regNodeList.getLength() > 0)) {
            for (int i = 0; i < regNodeList.getLength(); i++) {
                Node currNode = regNodeList.item(i);
                if (currNode.getParentNode().equals(parentNode)) {
                    childNodes.add(currNode);
                }
            }
        }
        return childNodes;
    }

    public static List<Node> collectChildNodes(Node parentNode, String... nodeNames) {
        List<Node> childNodes = new ArrayList<Node>();
        if (parentNode.hasChildNodes()) {
            Node currNode = parentNode.getFirstChild();
            do {
                for (String currType : nodeNames) {
                    if (currType.equals(currNode.getNodeName())) {
                        childNodes.add(currNode);
                    }
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return childNodes;
    }

    public static Map<String,String> collectTags(Node elem) {
        Map<String,String> result = new HashMap<String,String>();
        NodeList regNodeList = ((Element)elem).getElementsByTagName(XmlConfigNodeType.TAG.getNodeName());
        if ((regNodeList != null) && (regNodeList.getLength() > 0)) {
            for (int i = 0; i < regNodeList.getLength(); i++) {
                Node currNode = regNodeList.item(i);
                if (XmlConfigNodeType.TAG.equalsNode(currNode) && currNode.getParentNode().equals(elem)) {
                    String tagName = currNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue();
                    result.put(tagName, currNode.getTextContent());
                }
            }
        }
        return result;
    }

    public static List<ISearchObjectDateInfo> collectDateInfos(Node elem) {
        List<ISearchObjectDateInfo> resultList = new ArrayList<>();
        List<Node> dateInfos = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.DATE_INFO);
        for (Node dateInfoNode : dateInfos) {
            String dateFormat = null;
            List<Node> dateFormats = ConfigXmlUtil.collectChildNodes(dateInfoNode, XmlConfigNodeType.DATE_FORMAT);
            for (Node dateFormatNode : dateFormats) {
                if ((dateFormatNode.getTextContent() != null) && (!"".equals(dateFormatNode.getTextContent()))) {
                    dateFormat = dateFormatNode.getTextContent();
                    break;
                }
            }

            List<IRealRegex> allRegex = new ArrayList<>();
            List<Node> allRegexesNodes = ConfigXmlUtil.collectChildNodes(dateInfoNode, XmlConfigNodeType.REGEXES);
            for (Node allRegexesNode : allRegexesNodes) {
                List<IRealRegex> currNodeRegex = collectAllRegex(allRegexesNode);
                if ((currNodeRegex != null) && (!currNodeRegex.isEmpty())) {
                    allRegex.addAll(currNodeRegex);
                }
            }
            if ((dateFormat != null) && (!allRegex.isEmpty())) {
                ISearchObjectDateInfo newDateInfo = SearchObjectFactory.getInstance().getDateInfo();
                newDateInfo.setDateFormat(dateFormat);
                newDateInfo.setRegexList(allRegex);
                //keyedSO.setDateInfo(newDateInfo);
                resultList.add(newDateInfo);
            }
        }
        return resultList;
    }

    public static List<IInternalSortRequest> collectSortRequests(Node requestsNode) {
        List<IInternalSortRequest> resultList = new ArrayList<>();
        IRegexConstructorFactory<Node> fact = RegexXmlConstructorFactoryImpl.getInstance();
        if (requestsNode.hasChildNodes()) {
            Node currNode = requestsNode.getFirstChild();
            do {
                if (XmlConfigNodeType.SORT_REQUEST_DATE_TIME.equalsNode(currNode)) {
                    resultList.add(fact.getDateTimeSortRequestConstructor().constructCO(new XmlDomConstructionSource(currNode)));
                } else if (XmlConfigNodeType.SORT_REQUEST_REGEX.equalsNode(currNode)) {
                    resultList.add(fact.getRegexSortRequestConstructor().constructCO(new XmlDomConstructionSource(currNode)));
                } else if (XmlConfigNodeType.SORT_REQUEST_REGEX_COMPLEX.equalsNode(currNode)) {
                    resultList.add(fact.getRegexComplexSortRequestConstructor().constructCO(new XmlDomConstructionSource(currNode)));
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return resultList;
    }

    public static List<RefInternalSortRequest> collectRefSortRequests(Node refsNode) {
        List<RefInternalSortRequest> allRefSortRequestList = new ArrayList<>();
        List<Node> sortRequestNodes = ConfigXmlUtil.collectChildNodes(refsNode, XmlConfigNodeType.REF_SORT_REQUEST);
        for (Node sortRequestNode : sortRequestNodes) {
            String refSortReqName = getRefParamName(sortRequestNode);
            RefType sortReqRefType = getRefParamType(sortRequestNode);
            if (sortReqRefType == null) {
                log.error("No param type available. Cannot process this ref sort request " + refSortReqName);
                continue;
            }
            Integer position = getRefParamPosition(sortRequestNode);

            RefInternalSortRequest refReq = new RefInternalSortRequest(sortReqRefType, refSortReqName, position);
            List<IInternalSortRequest> refReqList = ConfigXmlUtil.collectSortRequests(sortRequestNode);
            if ((refReqList != null) && (!refReqList.isEmpty())) {
                refReq.setValue(refReqList.get(0));
                allRefSortRequestList.add(refReq);
            } else {
                log.warn("Empty ref sort request list for " + refReq + " this ref sort request is not processed");
            }
        }
        return allRefSortRequestList;
    }

    public static List<RefAcceptanceCriterion> collectRefAcceptanceCriteria(Node refsNode) {
        List<RefAcceptanceCriterion> refCriteriaList = new ArrayList<>();
        List<Node> refAcceptanceNodes = ConfigXmlUtil.collectChildNodes(refsNode, XmlConfigNodeType.REF_ACCEPTANCE);
        for (Node currNode : refAcceptanceNodes) {
            String refRPName = getRefParamName(currNode);
            RefType rpRefType = getRefParamType(currNode);
            if (rpRefType == null) {
                log.error("No param type available. Cannot process this ref acceptance criterion " + refRPName);
                continue;
            }
            Integer position = getRefParamPosition(currNode);

            RefAcceptanceCriterion newRefCriterion = new RefAcceptanceCriterion(rpRefType, refRPName, position);
            List<IAcceptanceCriterion> criteriaList = ConfigXmlUtil.collectAcceptanceCriteria(currNode);
            if ((criteriaList != null) && (!criteriaList.isEmpty())) {
                newRefCriterion.setValue(criteriaList.get(0));
                refCriteriaList.add(newRefCriterion);
            } else {
                log.warn("Empty criteria list for " + newRefCriterion + " this ref acceptance criteria is not processed");
            }
        }
        return refCriteriaList;
    }

    public static List<RefDateInfo> collectRefDateInfos(Node elem) {
        List<RefDateInfo> resultList = new ArrayList<>();
        List<Node> allRefDateInfoNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_DATE_INFO);
        if ((allRefDateInfoNodes != null) && (!allRefDateInfoNodes.isEmpty())) {
            for (Node refDateInfoNode : allRefDateInfoNodes) {
                String refRPName = getRefParamName(refDateInfoNode);
                RefType rpRefType = getRefParamType(refDateInfoNode);
                if (rpRefType == null) {
                    log.error("No param type available. Cannot process this ref date info " + refRPName);
                    continue;
                }

                RefDateInfo newRefDateInfo = new RefDateInfo(rpRefType, refRPName);
                List<ISearchObjectDateInfo> allDateInfos = collectDateInfos(refDateInfoNode);
                if ((allDateInfos != null) && (!allDateInfos.isEmpty())) {
                    newRefDateInfo.setValue(allDateInfos.get(0));
                    resultList.add(newRefDateInfo);
                }
            }
        }
        return resultList;
    }

    public static List<IAcceptanceCriterion> collectAcceptanceCriteria(Node criteriaNode) {
        List<IAcceptanceCriterion> criteriaList = new ArrayList<>();
        IRegexConstructorFactory<Node> fact = RegexXmlConstructorFactoryImpl.getInstance();
        if (criteriaNode.hasChildNodes()) {
            Node currNode = criteriaNode.getFirstChild();
            do {
                if (XmlConfigNodeType.REGEX_ACCEPTANCE.equalsNode(currNode)) {
                    IRegexAcceptanceConstructor<Node> acConstr = fact.getRegexAcceptanceConstructor();
                    IRegexAcceptanceCriterion newCriterion = acConstr.constructCO(new XmlDomConstructionSource(currNode));
                    if (newCriterion != null) {
                        criteriaList.add(newCriterion);
                    }
                } else if (XmlConfigNodeType.DT_INTERVAL_ACCEPTANCE.equalsNode(currNode)) {
                    IDTIntervalAcceptanceConstructor<Node> acConstr = fact.getDTIntervalAcceptanceConstructor();
                    IDTIntervalCriterion newCriterion = acConstr.constructCO(new XmlDomConstructionSource(currNode));
                    if (newCriterion != null) {
                        criteriaList.add(newCriterion);
                    }
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return criteriaList;
    }

    public static <T extends IKeyedSearchObject<?,?>> List<T> collectSearchObjects(Node parentNode, ISOConstructor<Node, T> constr, XmlConfigNodeType nodeType) {
        List<T> resultList = new ArrayList<>();
        List<Node> childNodesType = ConfigXmlUtil.collectChildNodes(parentNode, nodeType);
        if ((childNodesType != null) && (!childNodesType.isEmpty())) {
            for (Node currNode : childNodesType) {
                T currObj = constr.constructCO(new XmlDomConstructionSource(currNode));
                if (currObj != null) {
                    resultList.add(currObj);
                }
            }
        }
        return resultList;
    }

    public static <T extends IKeyedSearchObject<?,?>> T getFirstSearchObject(Node parentNode, ISOConstructor<Node, T> constr, XmlConfigNodeType nodeType) {
        T result = null;
        List<Node> childNodesType = ConfigXmlUtil.collectChildNodes(parentNode, nodeType);
        if ((childNodesType != null) && (!childNodesType.isEmpty())) {
            for (Node currNode : childNodesType) {
                T currObj = constr.constructCO(new XmlDomConstructionSource(currNode));
                if (currObj != null) {
                    result = currObj;
                    break;
                }
            }
        }
        return result;
    }

    public static <T extends IKeyedComplexSearchObject<R, O,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView,String>,
            R extends IKeyedComplexSearchResult<O,ISRComplexRegexView, ISROComplexRegexView,String>,
            O extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView,String>> void collectViews(T complexSO, Node soNode) {
        IRegexConstructorFactory<Node> constructFactory = RegexXmlConstructorFactoryImpl.getInstance();
        List<Node> viewsNodeList = ConfigXmlUtil.collectChildNodes(soNode, XmlConfigNodeType.VIEWS);
        for (Node node1 : viewsNodeList) {
            List<Node> viewNodeList = ConfigXmlUtil.collectChildNodes(node1, XmlConfigNodeType.VIEW);
            IComplexRegexViewConstructor<Node> viewConstructor = constructFactory.getComplexRegexViewConstructor();
            for (Node node2 : viewNodeList) {
                ISOComplexRegexView newView = viewConstructor.constructCO(new XmlDomConstructionSource(node2));
                complexSO.addView(newView.getSearchObjectName(), newView);
            }
        }
    }

    public static List<RefView<ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> collectRefViews(Node refViewsParent) {
        IRegexConstructorFactory<Node> constructFactory = RegexXmlConstructorFactoryImpl.getInstance();
        List<Node> allRefViewsNodes = ConfigXmlUtil.collectChildNodes(refViewsParent, XmlConfigNodeType.REF_VIEWS);
        List<RefView<ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> allViews = new ArrayList<>();
        for (Node refViewsNode : allRefViewsNodes) {
            List<Node> refViewNodes = ConfigXmlUtil.collectChildNodes(refViewsNode, XmlConfigNodeType.REF_VIEW);
            for (Node refViewNode : refViewNodes) {
                String refRPName = getRefParamName(refViewNode);
                RefType rpRefType = getRefParamType(refViewNode);
                if (rpRefType == null) {
                    log.error("No param type available. Cannot process this ref view " + refRPName);
                    continue;
                }
                Integer position = getRefParamPosition(refViewNode);

                RefView<ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> newRefView
                        = new RefView<>(rpRefType, refRPName,position);

                IComplexRegexViewConstructor<Node> viewConstructor = constructFactory.getComplexRegexViewConstructor();
                List<Node> viewNodes = ConfigXmlUtil.collectChildNodes(refViewNode, XmlConfigNodeType.VIEW);
                if ((viewNodes != null) && (!viewNodes.isEmpty())) {
                    ISOComplexRegexView viewObj = viewConstructor.constructCO(new XmlDomConstructionSource(viewNodes.get(0)));
                    newRefView.setViewKey(viewObj.getSearchObjectName());
                    newRefView.setViewSearchObject(viewObj);
                    allViews.add(newRefView);
                }
            }
        }
        return allViews;
    }

    public static void constructKeyedSO(IKeyedSearchObject<?, ?> keyedSO, Element soNode) {
        // tags
        Map<String,String> tagsMap = new HashMap<String,String>();
        List<Node> allTagsNodeList = collectChildNodes(soNode, XmlConfigNodeType.TAGS);
        for (Node tagsListNode : allTagsNodeList) {
            tagsMap.putAll(ConfigXmlUtil.collectTags(tagsListNode));
        }
        keyedSO.setSearchObjectTags(tagsMap);

        List<Node> allDescriptionNodes = ConfigXmlUtil.collectChildNodes(soNode, XmlConfigNodeType.DESCRIPTION);
        for (Node descrNode : allDescriptionNodes) {
            if ((descrNode.getTextContent() != null) && (!"".equals(descrNode.getTextContent()))) {
                keyedSO.setSearchObjectDescription(descrNode.getTextContent());
            }
        }

        // replace params
        List<Node> rpAllNodeList = collectChildNodes(soNode, XmlConfigNodeType.REPLACE_PARAMS);
        for (Node rpListNode : rpAllNodeList) {
            List<IReplaceParam<?>> params = ConfigXmlUtil.collectReplaceParams(rpListNode);
            for (IReplaceParam<?> rp : params) {
                keyedSO.addParam(rp);
            }
        }

        // required log types
        List<LogFileTypeKey> logTypes = new ArrayList<>();
        List<Node> allReqLogTypes = collectChildNodes(soNode, XmlConfigNodeType.REQUIRED_LOG_TYPES);
        for (Node rqLogTypeList : allReqLogTypes) {
            List<Node> ltList = collectChildNodes(rqLogTypeList, XmlConfigNodeType.LOG_TYPE);
            for (Node logTypeNode : ltList) {
                if ((logTypeNode.getTextContent() != null) && (!"".equals(logTypeNode.getTextContent()))) {
                    LogFileTypeKey newKey = new LogFileTypeKey(logTypeNode.getTextContent());
                    logTypes.add(newKey);
                }
            }
        }
        if (!logTypes.isEmpty()) {
            keyedSO.setRequiredLogTypes(logTypes);
        }

        // regex flags
        List<Node> flagsNodeList = ConfigXmlUtil.collectChildNodes(soNode, XmlConfigNodeType.REGEX_FLAGS);
        for (Node nodeFlags : flagsNodeList) {
            if (nodeFlags.getParentNode().equals(soNode)) {
                keyedSO.setRegexFlags(getRegexFlags(nodeFlags));
            }
        }

        // sort requests
        List<IInternalSortRequest> intSortReqList = new ArrayList<>();
        List<Node> sortReqsAllNodes = ConfigXmlUtil.collectChildNodes(soNode, XmlConfigNodeType.SORT_REQUESTS);
        for (Node sortReqsNode : sortReqsAllNodes) {
            intSortReqList.addAll(ConfigXmlUtil.collectSortRequests(sortReqsNode));
        }
        //keyedSO.setSortRequestList(intSortReqList);
        keyedSO.getSortRequestList().addAll(intSortReqList);

        // acceptance criteria
        List<IAcceptanceCriterion> acceptCritList = new ArrayList<>();
        List<Node> acceptCritAllNodes = ConfigXmlUtil.collectChildNodes(soNode, XmlConfigNodeType.ACCEPTANCES);
        for (Node acceptNodes : acceptCritAllNodes) {
            acceptCritList.addAll(ConfigXmlUtil.collectAcceptanceCriteria(acceptNodes));
        }
        keyedSO.getAcceptanceList().addAll(acceptCritList);

        // date info
        List<ISearchObjectDateInfo> allDateInfos = collectDateInfos(soNode);
        if ((allDateInfos != null) && (!allDateInfos.isEmpty())) {
            keyedSO.setDateInfo(allDateInfos.get(0));
        } else {
            log.warn("DateInfo not available for " + keyedSO.getSearchObjectName() + " Removing all date internal search requests");
        }
    }

    public static void constructRefKeyedSO(
            RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>> refKeyedSO, Element refNode) {
        // tags
        Map<String,String> soTagsMap = new HashMap<String,String>();
        List<Node> allTagsNodeList = collectChildNodes(refNode, XmlConfigNodeType.TAGS);
        for (Node tagsListNode : allTagsNodeList) {
            soTagsMap.putAll(ConfigXmlUtil.collectTags(tagsListNode));
        }
        refKeyedSO.setTags(soTagsMap);

        //ref description
        List<Node> allDescriptionNodes = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.DESCRIPTION);
        for (Node descrNode : allDescriptionNodes) {
            if ((descrNode.getTextContent() != null) && (!"".equals(descrNode.getTextContent()))) {
                refKeyedSO.setDescription(descrNode.getTextContent());
            }
        }
        // ref name
        List<Node> refNames = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_NAME);
        if ((refNames != null) && (!refNames.isEmpty())) {
            Iterator<Node> refNameIter = refNames.iterator();
            while (refNameIter.hasNext()) {
                Node currRefName = refNameIter.next();
                if ((currRefName.getTextContent() != null) && (!"".equals(currRefName.getTextContent()))) {
                    refKeyedSO.setRefName(currRefName.getTextContent());
                    break;
                }
            }
        }
        // ref group
        List<Node> refGroups = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_GROUP);
        if ((refGroups != null) && (!refGroups.isEmpty())) {
            Iterator<Node> refGroupIter = refGroups.iterator();
            while (refGroupIter.hasNext()) {
                Node currRefGroup = refGroupIter.next();
                if ((currRefGroup.getTextContent() != null) && (!"".equals(currRefGroup.getTextContent()))) {
                    //refKeyedSO.setRefGroup(currRefGroup.getTextContent());
                    refKeyedSO.setRefGroup(SearchObjectFactory.getInstance().getSearchObjectGroup(currRefGroup.getTextContent()));
                    break;
                }
            }
        }
        // ref tags
        Map<String,String> refTagsMap = new HashMap<String,String>();
        List<Node> refTags = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_TAGS);
        for (Node tagsListNode : refTags) {
            refTagsMap.putAll(ConfigXmlUtil.collectTags(tagsListNode));
        }
        refKeyedSO.setRefTags(refTagsMap);

        // ref replace params
        List<Node> refRPAllNodeList = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_REPLACE_PARAMS);
        List<RefReplaceParam> allRefRPList = new ArrayList<>();
        for (Node rpListNode : refRPAllNodeList) {
            allRefRPList.addAll(ConfigXmlUtil.collectRefReplaceParams(rpListNode));
        }
        if (!allRefRPList.isEmpty()) {
            refKeyedSO.setRefReplaceParams(allRefRPList);
        }

        // ref sort requests
        List<Node> refAllSortRequests = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_SORT_REQUESTS);
        List<RefInternalSortRequest> allRefSortRequestList = new ArrayList<>();
        for (Node refSortRequestsNode : refAllSortRequests) {
            allRefSortRequestList.addAll(ConfigXmlUtil.collectRefSortRequests(refSortRequestsNode));
        }
        if (!allRefSortRequestList.isEmpty()) {
            refKeyedSO.setRefSortRequests(allRefSortRequestList);
        }

        // acceptance criteria
        List<RefAcceptanceCriterion> allAcceptanceCriteria = new ArrayList<>();
        List<Node> allRefACsNodes = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_ACCEPTANCES);
        log.debug("Ref name=" + refKeyedSO.getName() + " Acceptances number=" + allRefACsNodes.size());
        for (Node refACNode : allRefACsNodes) {
            List<RefAcceptanceCriterion> refAcceptanceCriteria = ConfigXmlUtil.collectRefAcceptanceCriteria(refACNode);
            if (refAcceptanceCriteria != null) {
                allAcceptanceCriteria.addAll(refAcceptanceCriteria);
            }
        }
        refKeyedSO.setRefAcceptanceCriteria(allAcceptanceCriteria);

        // regex flags
        List<Node> allRegexFlags = ConfigXmlUtil.collectChildNodes(refNode, XmlConfigNodeType.REF_REGEX_FLAGS);
        if ((allRegexFlags != null) && (!allRegexFlags.isEmpty())) {

            Iterator<Node> regexFlagIter = allRegexFlags.iterator();
            RefParamInteger newRefRegexFlags = null;
            while (regexFlagIter.hasNext()) {
                Node refFlagsNode = regexFlagIter.next();
                String refRPName = getRefParamName(refFlagsNode);
                RefType rpRefType = getRefParamType(refFlagsNode);
                if (rpRefType == null) {
                    log.error("No param type available. Cannot process this ref replace param " + refRPName);
                    continue;
                }

                newRefRegexFlags = new RefParamInteger(rpRefType, refRPName);
                List<Node> flagsNodeList = ConfigXmlUtil.collectChildNodes(refFlagsNode, XmlConfigNodeType.REGEX_FLAGS);
                Integer endFlags = 0;
                for (Node nodeFlags : flagsNodeList) {
                    endFlags |= getRegexFlags(nodeFlags);
                }
                newRefRegexFlags.setValue(endFlags);
                break;
            }
            if (newRefRegexFlags != null) {
                refKeyedSO.setRefRegexFlags(newRefRegexFlags);
            }
        }

        // ref date info
        List<RefDateInfo> allRefDateInfos = collectRefDateInfos(refNode);
        if ((allRefDateInfos != null) && (!allRefDateInfos.isEmpty())) {
            refKeyedSO.setRefDateInfo(allRefDateInfos.get(0));
        }
    }

}
