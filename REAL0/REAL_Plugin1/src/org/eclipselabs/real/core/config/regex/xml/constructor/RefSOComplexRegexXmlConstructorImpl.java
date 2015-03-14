package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.regex.constructor.IComplexRegexConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRefSOComplexRegexConstructor;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchobject.ref.RefType;
import org.eclipselabs.real.core.searchobject.ref.RefRealRegex;
import org.eclipselabs.real.core.searchobject.ref.RefSOComplexRegex;
import org.eclipselabs.real.core.searchobject.ref.RefView;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RefSOComplexRegexXmlConstructorImpl implements IRefSOComplexRegexConstructor<Node>, IConfigXmlConstants {

    //private static final Logger log = LogManager.getLogger(getClass());
    
    @Override
    public RefSOComplexRegex constructCO(IConstructionSource<Node> cSource) {
        RefSOComplexRegex refCRResult = null;
        if (XmlConfigNodeType.REF_COMPLEX_REGEX.equalsNode(cSource.getSource())) {
            String refName = ConfigXmlUtil.getRefParamName(cSource.getSource());
            RefType refType = ConfigXmlUtil.getRefParamType(cSource.getSource());
            Integer position = ConfigXmlUtil.getRefParamPosition(cSource.getSource());
            if (refType != null) {
                refCRResult = new RefSOComplexRegex(refType, SearchObjectType.COMPLEX_REGEX, refName);
            } else {
                refCRResult = new RefSOComplexRegex(SearchObjectType.COMPLEX_REGEX, refName);
            }
            refCRResult.setPosition(position);
            ConfigXmlUtil.constructRefKeyedSO((RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>)refCRResult, (Element)cSource.getSource());
            List<RefView<ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> allViews = ConfigXmlUtil.collectRefViews(cSource.getSource());
            if ((allViews != null) && (!allViews.isEmpty())) {
                refCRResult.setRefViewList(allViews);
            }
            
            List<Node> regexesNodeList = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.REF_REGEXES);
            List<RefRealRegex> tmpList = new ArrayList<RefRealRegex>();
            for (Node node : regexesNodeList) {
                tmpList.addAll(ConfigXmlUtil.collectRefRegex(node));
            }
            refCRResult.setRefMainRegexes(tmpList);
            
            IRegexConstructorFactory<Node> factory = RegexXmlConstructorFactoryImpl.getInstance();
            List<Node> refValuesNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.COMPLEX_REGEX);
            if ((refValuesNodes != null) && (!refValuesNodes.isEmpty())) {
                IComplexRegexConstructor<Node> constr = factory.getComplexRegexConstructor();
                for (Node isoNode : refValuesNodes) {
                    ISOComplexRegex refValue = constr.constructCO(new XmlDomConstructionSource(isoNode));
                    if (refValue != null) {
                        refCRResult.setValue(refValue);
                    }
                }
            }
        }
        return refCRResult;
    }

}
