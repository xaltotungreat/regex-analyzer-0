package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRefConstructor;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRefSOSearchScriptConstructor;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.ml.regex.constructor.ISearchScriptConstructor;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.ml.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOSearchScript;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchobject.ref.RefParamString;
import org.eclipselabs.real.core.searchobject.ref.RefSOComplexRegex;
import org.eclipselabs.real.core.searchobject.ref.RefSOSearchScript;
import org.eclipselabs.real.core.searchobject.ref.RefType;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RefSOSearchScriptXmlConstructorImpl implements IRefSOSearchScriptConstructor<Node>, IConfigXmlConstants {

    private static final Logger log = LogManager.getLogger(RefSOSearchScriptXmlConstructorImpl.class);

    @Override
    public RefSOSearchScript constructCO(IConstructionSource<Node> cSource) {
        RefSOSearchScript refSSResult = null;
        IRegexConstructorFactory<Node> constructFactory = RegexXmlConstructorFactoryImpl.getInstance();
        if (XmlConfigNodeType.REF_SEARCH_SCRIPT.equalsNode(cSource.getSource())) {
            String refName = ConfigXmlUtil.getRefParamName(cSource.getSource());
            RefType refType = ConfigXmlUtil.getRefParamType(cSource.getSource());
            Integer position = ConfigXmlUtil.getRefParamPosition(cSource.getSource());
            if (refType != null) {
                refSSResult = new RefSOSearchScript(refType, SearchObjectType.COMPLEX_REGEX, refName);
            } else {
                refSSResult = new RefSOSearchScript(SearchObjectType.COMPLEX_REGEX, refName);
            }
            refSSResult.setPosition(position);
            ConfigXmlUtil.constructRefKeyedSO((RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>)refSSResult, (Element)cSource.getSource());

            List<Node> refStringNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REF_STRING);
            if ((refStringNodes != null) && (!refStringNodes.isEmpty())) {
                Iterator<Node> refStrIter = refStringNodes.iterator();
                while (refStrIter.hasNext()) {
                    Node currNode = refStringNodes.get(0);
                    String refTextName = null;
                    if (currNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_NAME) != null) {
                        refTextName = currNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_NAME).getNodeValue();
                    }

                    RefType refTextType = null;
                    if ((currNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE) != null)
                            && (currNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue() != null)) {
                        try {
                            refTextType = RefType.valueOf(currNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue().toUpperCase());
                        } catch (IllegalArgumentException iae) {
                            log.error("No such element in RefType " + currNode.getAttributes().getNamedItem(ATTRIBUTE_NAME_TYPE).getNodeValue()
                                    + " Cannot process this ref replace param " + refTextName, iae);
                            continue;
                        }
                    } else {
                        log.error("No param type available. Cannot process this ref replace param " + refTextName);
                    }
                    if ((currNode.getTextContent() != null) && (!"".equals(currNode.getTextContent()))) {
                        RefParamString textParam = new RefParamString(refTextType, refTextName, currNode.getTextContent());
                        refSSResult.setRefScriptText(textParam);
                        break;
                    }

                }
            }

            // collect refs
            List<Node> refsNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REF_COMPLEX_REGEX);
            for (Node crNode : refsNodes) {
                IRefConstructor<Node, ? extends RefKeyedSO<
                        ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> currConstr = constructFactory.getRefConstructor(crNode);
                if (currConstr != null) {
                    RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> constructedSO
                                = currConstr.constructCO(new XmlDomConstructionSource(crNode));
                    if (constructedSO != null) {
                        refSSResult.addMainRegex((RefSOComplexRegex)constructedSO);
                    }
                }
            }

            IRegexConstructorFactory<Node> factory = RegexXmlConstructorFactoryImpl.getInstance();
            List<Node> refValuesNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.SEARCH_SCRIPT);
            if ((refValuesNodes != null) && (!refValuesNodes.isEmpty())) {
                ISearchScriptConstructor<Node> constr = factory.getSearchScriptConstructor();
                for (Node isoNode : refValuesNodes) {
                    ISOSearchScript refValue = constr.constructCO(new XmlDomConstructionSource(isoNode));
                    if (refValue != null) {
                        refSSResult.setValue(refValue);
                    }
                }
            }
        }
        return refSSResult;
    }

}
