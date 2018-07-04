package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRefSORegexConstructor;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRegexConstructor;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.ml.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISORegex;
import org.eclipselabs.real.core.searchobject.SearchObjectType;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchobject.ref.RefRealRegex;
import org.eclipselabs.real.core.searchobject.ref.RefSORegex;
import org.eclipselabs.real.core.searchobject.ref.RefType;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RefSORegexXmlConstructorImpl implements IRefSORegexConstructor<Node>, IConfigXmlConstants {

    private static final Logger log = LogManager.getLogger(RefSORegexXmlConstructorImpl.class);

    @Override
    public RefSORegex constructCO(IConstructionSource<Node> cSource) {
        RefSORegex refRxResult = null;
        if (XmlConfigNodeType.REF_KEYED_REGEX.equalsNode(cSource.getSource())) {
            String refName = ConfigXmlUtil.getRefParamName(cSource.getSource());
            RefType refType = ConfigXmlUtil.getRefParamType(cSource.getSource());
            Integer position = ConfigXmlUtil.getRefParamPosition(cSource.getSource());
            if (refType != null) {
                refRxResult = new RefSORegex(refType, SearchObjectType.COMPLEX_REGEX, refName);
            } else {
                refRxResult = new RefSORegex(SearchObjectType.COMPLEX_REGEX, refName);
            }
            refRxResult.setPosition(position);
            ConfigXmlUtil.constructRefKeyedSO((RefKeyedSO<? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject>>)refRxResult, (Element)cSource.getSource());

            List<RefRealRegex> mainRefRegexList = new ArrayList<RefRealRegex>();
            mainRefRegexList.addAll(ConfigXmlUtil.collectRefRegex(cSource.getSource()));
            if (!mainRefRegexList.isEmpty()) {
                refRxResult.setRefRegex(mainRefRegexList.get(0));
            } else {
                log.warn("No ref real regex found for RefSORegex "
                        + refRxResult.getName()
                        + " Cannot process this object");
                refRxResult = null;
                return refRxResult;
            }

            IRegexConstructorFactory<Node> factory = RegexXmlConstructorFactoryImpl.getInstance();
            List<Node> refValuesNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), XmlConfigNodeType.KEYED_REGEX);
            if ((refValuesNodes != null) && (!refValuesNodes.isEmpty())) {
                IRegexConstructor<Node> constr = factory.getRegexConstructor();
                for (Node isoNode : refValuesNodes) {
                    ISORegex refValue = constr.constructCO(new XmlDomConstructionSource(isoNode));
                    if (refValue != null) {
                        refRxResult.setValue(refValue);
                    }
                }
            }
        }
        return refRxResult;
    }

}
