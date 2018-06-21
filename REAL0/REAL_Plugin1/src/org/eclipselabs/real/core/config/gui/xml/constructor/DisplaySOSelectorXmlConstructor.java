package org.eclipselabs.real.core.config.gui.xml.constructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.gui.constructor.IDisplaySOSelectorConstructor;
import org.eclipselabs.real.core.config.gui.constructor.ISortRequestKeyConstructor;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.util.TagRef;
import org.eclipselabs.real.core.util.TagRefType;
import org.eclipselabs.real.gui.core.sort.SortRequestKey;
import org.eclipselabs.real.gui.core.sotree.DisplaySOSelector;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOConstants;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOSelector;
import org.w3c.dom.Node;

public class DisplaySOSelectorXmlConstructor implements IDisplaySOSelectorConstructor<Node>, IConfigXmlConstants {

    private static final Logger log = LogManager.getLogger(DisplaySOSelectorXmlConstructor.class);
    
    @Override
    public IDisplaySOSelector constructCO(IConstructionSource<Node> cSource) {
        String selectorName = "No name";
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
            selectorName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
        }
        IDisplaySOSelector newSelector = new DisplaySOSelector(selectorName);
        List<Node> nameReg = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.NAME_REGEXES);
        List<IRealRegex> nameRegAll = new ArrayList<IRealRegex>();
        for (Node node : nameReg) {
            List<IRealRegex> nameRegList = ConfigXmlUtil.collectAllRegex(node);
            nameRegAll.addAll(nameRegList);
        }
        newSelector.setNameRegexes(nameRegAll);

        List<Node> groupReg = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.GROUP_REGEXES);
        List<IRealRegex> groupRegAll = new ArrayList<IRealRegex>();
        for (Node node : groupReg) {
            List<IRealRegex> groupRegList = ConfigXmlUtil.collectAllRegex(node);
            groupRegAll.addAll(groupRegList);
        }
        newSelector.setGroupRegexes(groupRegAll);

        List<Node> tagReg = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.TAG_REGEXES);
        List<TagRef> tagRefList = new ArrayList<TagRef>();
        for (Node node : tagReg) {
            List<Node> tagRegList = ConfigXmlUtil.collectChildNodes(node, XmlConfigNodeType.TAG_REGEX);
            for (Node node2 : tagRegList) {
                Map<String, IRealRegex> tagNameReg = ConfigXmlUtil.collectNamedRegex(node2);
                TagRefType refType = TagRefType.MATCH;
                if (node2.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE) != null) {
                    String typeStr = node2.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue();
                    if (typeStr != null) {
                        try {
                            refType = TagRefType.valueOf(typeStr);
                        } catch (IllegalArgumentException iae) {
                            log.warn("constructCO Incorrect TagRefType " + typeStr + " assign default " + refType, iae);
                        }
                    }
                }
                tagRefList.add(new TagRef(refType, tagNameReg.get(ATTRIBUTE_VALUE_TAG_REF_NAME_REGEX), tagNameReg.get(ATTRIBUTE_VALUE_TAG_REF_VALUE_REGEX)));
            }
        }
        newSelector.setTagsRegexes(tagRefList);

        List<Node> textView = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.TEXT_VIEW);
        if ((textView != null) && (!textView.isEmpty())) {
            newSelector.setTextViewName(textView.get(0).getTextContent());
        }

        List<Node> shortViews = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.SHORT_VIEWS);
        for (Node node : shortViews) {
            List<Node> shortViewItems = ConfigXmlUtil.collectChildNodes(node, XmlConfigNodeType.SHORT_VIEW);
            for (Node node2 : shortViewItems) {
                newSelector.addViewNamePattern(node2.getTextContent());
            }
        }
        
        List<SortRequestKey> allKeys = new ArrayList<>();
        List<Node> sortRequestAllNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.SORT_REQUEST_KEYS);
        for (Node node : sortRequestAllNodes) {
            List<Node> sortRequestKeyNodes = ConfigXmlUtil.collectChildNodes(node, XmlConfigNodeType.SORT_REQUEST_KEY);
            ISortRequestKeyConstructor<Node> srqConstructor = GUIXmlConstructorFactory.getInstance().getSortRequestKeyConstructor();
            for (Node node2 : sortRequestKeyNodes) {
                SortRequestKey newKey = srqConstructor.constructCO(new XmlDomConstructionSource(node2));
                if (newKey != null) {
                    allKeys.add(newKey);
                }
            }
        }
        if (!allKeys.isEmpty()) {
            newSelector.setSortRequestKeys(allKeys);
        }
        List<Node> iconSetsAllNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.ICON_SETS);
        for (Node iconSetsNode : iconSetsAllNodes) {
            List<Node> iconSetNodes = ConfigXmlUtil.collectChildNodes(iconSetsNode, XmlConfigNodeType.ICON_SET);
            for (Node iconSetNode : iconSetNodes) {
                String setName = null;
                Node setNameAttrNode = iconSetNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_NAME);
                if ((setNameAttrNode != null) && (setNameAttrNode.getNodeValue() != null) && (!"".equals(setNameAttrNode.getNodeValue()))) {
                    setName = setNameAttrNode.getNodeValue();
                }
                if (setName == null) {
                    log.warn("constructCO incorrect icon set name");
                    continue;
                }
                List<Node> iconPathNodes = ConfigXmlUtil.collectChildNodes(iconSetNode, XmlConfigNodeType.ICON_PATH);
                Map<String,String> iconPaths = new HashMap<String, String>();
                for (Node iconPathNode : iconPathNodes) {
                    Node nameNode = iconPathNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_NAME);
                    if ((nameNode != null) && (nameNode.getNodeValue() != null) && (!"".equals(nameNode.getNodeValue()))
                            && (iconPathNode.getTextContent() != null) && (!"".equals(iconPathNode.getTextContent()))) {
                        iconPaths.put(nameNode.getNodeValue(), iconPathNode.getTextContent());
                    }
                }
                if ("default".equals(setName)) {
                    newSelector.getGuiProperties().put(IDisplaySOConstants.GUI_PROPERTY_DEFAULT_ICON_SET, iconPaths);
                } else {
                    newSelector.getGuiProperties().put(setName, iconPaths);
                }
            }
        }
        return newSelector;
    }

}
