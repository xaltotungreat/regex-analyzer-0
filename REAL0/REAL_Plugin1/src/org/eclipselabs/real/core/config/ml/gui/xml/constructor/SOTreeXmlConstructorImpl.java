package org.eclipselabs.real.core.config.ml.gui.xml.constructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.gui.constructor.ISOTreeConstructor;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.ml.xml.XmlDomConstructionSource;
import org.eclipselabs.real.gui.core.sotree.DisplaySOFolderImpl;
import org.eclipselabs.real.gui.core.sotree.DisplaySORoot;
import org.eclipselabs.real.gui.core.sotree.DisplaySOTemplateImpl;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOConstants;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOFolder;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOSelector;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTemplate;
import org.eclipselabs.real.gui.core.sotree.IDisplaySOTreeItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOTreeXmlConstructorImpl implements ISOTreeConstructor<Node>, IConfigXmlConstants {
    private static final Logger log = LogManager.getLogger(SOTreeXmlConstructorImpl.class);
    
    
    @Override
    public DefaultMutableTreeNode constructCO(IConstructionSource<Node> cSource) {
        DefaultMutableTreeNode soTreeRootNode = null;
        if (XmlConfigNodeType.SEARCH_OBJECT_TREE.equalsNode(cSource.getSource())) {
            soTreeRootNode = new DefaultMutableTreeNode(new DisplaySORoot("Search Object Tree", true));
            NodeList children = cSource.getSource().getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    parseSOTreeObject(children.item(i), soTreeRootNode);
                }
            }
        }
        return soTreeRootNode;
    }
    
    protected IDisplaySOTreeItem parseSOTreeObject(Node xmlNode, DefaultMutableTreeNode treeNode) {
        IDisplaySOTreeItem result = null;
        if (XmlConfigNodeType.FOLDER.equalsNode(xmlNode)) {
            String fldName = xmlNode.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
            Boolean expanded = false;
            if ((xmlNode.getAttributes().getNamedItem("expanded") != null)) {
                String expandedState = xmlNode.getAttributes().getNamedItem("expanded").getNodeValue();
                if ("true".equalsIgnoreCase(expandedState)) {
                    expanded = true;
                }
            }
            log.debug("Parsing GUI Config Folder " + fldName);
            IDisplaySOFolder fld = new DisplaySOFolderImpl(fldName, expanded);
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(fld);
            treeNode.add(newNode);
            NodeList children = xmlNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    parseSOTreeObject(children.item(i), newNode);
                }
            }
            result = fld;
        } else if (XmlConfigNodeType.TEMPLATE.equalsNode(xmlNode)) {
            String refName = xmlNode.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
            log.debug("Parsing GUI Config Template " + refName);
            Boolean expanded = false;
            if ((xmlNode.getAttributes().getNamedItem("expanded") != null)) {
                String expandedState = xmlNode.getAttributes().getNamedItem("expanded").getNodeValue();
                if ("true".equalsIgnoreCase(expandedState)) {
                    expanded = true;
                }
            }
            IDisplaySOTemplate refObject = new DisplaySOTemplateImpl(refName, expanded);
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(refObject);
            treeNode.add(newNode);
            List<Node> selectorNodes = ConfigXmlUtil.collectChildNodes(xmlNode, XmlConfigNodeType.SELECTOR, XmlConfigNodeType.TEMPLATE);
            for (Node selectorNode : selectorNodes) {
                if (XmlConfigNodeType.SELECTOR.equalsNode(selectorNode)) {
                    IDisplaySOSelector newSelector = GUIXmlConstructorFactory.getInstance().getSelectorConstructor().constructCO(new XmlDomConstructionSource(selectorNode));
                    if (newSelector != null) {
                        refObject.addSelector(newSelector);
                    }
                } else if (XmlConfigNodeType.TEMPLATE.equalsNode(selectorNode)) {
                    refObject.addSelector((IDisplaySOTemplate)parseSOTreeObject(selectorNode, newNode));
                    //parseSOTreeObject(selectorNode, newNode);
                }
            }
            List<Node> iconSetsAllNodes = ConfigXmlUtil.collectChildNodes(xmlNode, XmlConfigNodeType.ICON_SETS);
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
                    if ("default".equalsIgnoreCase(setName)) {
                        refObject.getGuiProperties().put(IDisplaySOConstants.GUI_PROPERTY_DEFAULT_ICON_SET, iconPaths);
                    } else {
                        refObject.getGuiProperties().put(setName, iconPaths);
                    }
                }
            }
            result = refObject;
        }
        return result;
    }

}
