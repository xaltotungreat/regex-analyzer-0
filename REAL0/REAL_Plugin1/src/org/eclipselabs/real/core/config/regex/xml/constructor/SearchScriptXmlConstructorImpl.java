package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.regex.constructor.IRefConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.regex.constructor.ISOConstructor;
import org.eclipselabs.real.core.config.regex.constructor.ISearchScriptConstructor;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISOSearchScript;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SearchScriptXmlConstructorImpl implements ISearchScriptConstructor<Node> {

    @Override
    public ISOSearchScript constructCO(IConstructionSource<Node> cSource) {
        IRegexConstructorFactory<Node> constructFactory = RegexXmlConstructorFactoryImpl.getInstance();
        ISOSearchScript ssResult = null;
        if (XmlConfigNodeType.SEARCH_SCRIPT.equalsNode(cSource.getSource())) {
            ssResult = SearchObjectFactory.getInstance().getSOSearchScript(cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue());
            ConfigXmlUtil.constructKeyedSO(ssResult, (Element)cSource.getSource());

            // main script text
            List<Node> textNodesList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.SCRIPT_TEXT);
            if ((textNodesList != null) && (!textNodesList.isEmpty())) {
                for (Node textNode : textNodesList) {
                    if ((textNode.getTextContent() != null) && (!"".equals(textNode.getTextContent()))) {
                        ssResult.setScriptText(textNode.getTextContent());
                    }
                }
            }

            // collect complex regexes
            List<Node> crNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.COMPLEX_REGEX);
            for (Node crNode : crNodeList) {
                ISOConstructor<Node, IKeyedSearchObject<?,?>> currConstr = constructFactory.getSOConstructor(crNode);
                if (currConstr != null) {
                    IKeyedSearchObject<?,?> constructedSO = currConstr.constructCO(new XmlDomConstructionSource(crNode));
                    if (constructedSO != null) {
                        constructedSO.setParent(ssResult);
                        ssResult.getMainRegexList().add((IKeyedComplexSearchObject<
                                ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                                        ISRComplexRegexView, ISROComplexRegexView, String>,
                                    ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                                    ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>)constructedSO);
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
                        ssResult.addRef(constructedSO);
                    }
                }
            }

            // collect the views before setting the view order
            ConfigXmlUtil.collectViews(ssResult, cSource.getSource());
            // collect view names
            List<String> viewNamesOrder = new ArrayList<>();
            List<Node> viewNamesNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.VIEW_NAMES);
            for (Node vnsNode : viewNamesNodes) {
                List<Node> viewNameNodes = ConfigXmlUtil.collectChildNodes(vnsNode, XmlConfigNodeType.VIEW_NAME);
                for (Node vnNode : viewNameNodes) {
                    if ((vnNode.getTextContent() != null) && (!"".equals(vnNode.getTextContent()))) {
                        viewNamesOrder.add(vnNode.getTextContent());
                    }
                }
            }
            ssResult.setViewOrder(viewNamesOrder);

            // views for this search script
            /*List<Node> viewsNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.VIEWS);
            for (Node node1 : viewsNodeList) {
                List<Node> viewNodeList = ConfigXmlUtil.collectChildNodes(node1, XmlConfigNodeType.VIEW);
                IComplexRegexViewConstructor<Node> viewConstructor = constructFactory.getComplexRegexViewConstructor();
                for (Node node2 : viewNodeList) {
                    ISOComplexRegexView newView = viewConstructor.constructCO(new XmlDomConstructionSource(node2));
                    ssResult.addView(newView.getSearchObjectName(), newView);
                }
            }*/
        }
        return ssResult;
    }

}
