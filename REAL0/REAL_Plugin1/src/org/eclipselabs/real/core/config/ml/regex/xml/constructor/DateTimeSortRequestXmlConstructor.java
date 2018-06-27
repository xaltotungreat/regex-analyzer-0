package org.eclipselabs.real.core.config.ml.regex.xml.constructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.IConstructionSource;
import org.eclipselabs.real.core.config.ml.regex.constructor.IDateTimeSortRequestConstructor;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.searchresult.sort.IDateTimeSortRequest;
import org.eclipselabs.real.core.searchresult.sort.SortRequestFactory;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.w3c.dom.Node;

public class DateTimeSortRequestXmlConstructor implements IDateTimeSortRequestConstructor<Node> {
    
    private static final Logger log = LogManager.getLogger(DateTimeSortRequestXmlConstructor.class);
    @Override
    public IDateTimeSortRequest constructCO(IConstructionSource<Node> cSource) {
        String reqName = null;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
            reqName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
        }
        SortingApplicability sortAppl = SortingApplicability.NONE;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_APPLICABILITY) != null) {
            String sortApplStr = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_APPLICABILITY).getNodeValue();
            if ((sortApplStr != null) && (!"".equals(sortApplStr))) {
                try {
                    sortAppl = SortingApplicability.valueOf(sortApplStr);
                } catch (IllegalArgumentException iae) {
                    log.error("No such element in SortingApplicability " + sortApplStr + " Setting to NONE", iae);
                    sortAppl = SortingApplicability.NONE;
                }
            }
        } else {
            log.warn("No sort applicability element for date time sort request " + reqName);
        }
        IDateTimeSortRequest res = SortRequestFactory.getInstance().getDateTimeSortRequest(reqName);
        res.setSortApplicability(sortAppl);
        return res;
    }

}
