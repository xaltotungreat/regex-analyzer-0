package org.eclipselabs.real.core.config.regex.xml.constructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.config.regex.constructor.IDTIntervalAcceptanceConstructor;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionFactory;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionType;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceGuess;
import org.eclipselabs.real.core.searchobject.crit.IDTIntervalCriterion;
import org.eclipselabs.real.core.searchobject.crit.IDTIntervalGuess;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.w3c.dom.Node;

public class DTIntervalAcceptanceXmlConstructorImpl implements IDTIntervalAcceptanceConstructor<Node> {

    private static final Logger log  = LogManager.getLogger(DTIntervalAcceptanceXmlConstructorImpl.class);
    
    public static final String NODE_LOW_BOUND = "lowBound";
    public static final String NODE_HIGH_BOUND = "highBound";
    
    public static final String GUESS_REGEX_FIRST_RECORD = "FirstRecord";
    public static final String GUESS_REGEX_LAST_RECORD = "LastRecord";
    
    @Override
    public IDTIntervalCriterion constructCO(IConstructionSource<Node> cSource) {
        String critName = null;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
            critName = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
        }
        AcceptanceCriterionType acceptType = null;
        if (cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE) != null) {
            String sortApplStr = cSource.getSource().getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_TYPE).getNodeValue();
            if ((sortApplStr != null) && (!"".equals(sortApplStr))) {
                try {
                    acceptType = AcceptanceCriterionType.valueOf(sortApplStr);
                } catch (IllegalArgumentException iae) {
                    log.error("NO such element in AcceptanceCriterionType " + sortApplStr + " unable to process this criterion", iae);
                }
            }
        }
        IDTIntervalCriterion crit = null;
        if (acceptType != null) {
            crit = AcceptanceCriterionFactory.getInstance().getDTIntervalCriterion(acceptType);
            crit.setName(critName);
            List<Node> lowBoundNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), NODE_LOW_BOUND);
            if ((lowBoundNodes != null) && (!lowBoundNodes.isEmpty())) {
                Iterator<Node> lowIter = lowBoundNodes.iterator();
                while (lowIter.hasNext()) {
                    Node lowNode = lowIter.next();
                    if ((lowNode.getTextContent() != null) && (!"".equals(lowNode.getTextContent()))) {
                        crit.setLowBoundKey(new ReplaceParamKey(lowNode.getTextContent()));
                    }
                }
            }
            List<Node> highBoundNodes = ConfigXmlUtil.collectChildNodes(cSource.getSource(), NODE_HIGH_BOUND);
            if ((highBoundNodes != null) && (!highBoundNodes.isEmpty())) {
                Iterator<Node> highIter = highBoundNodes.iterator();
                while (highIter.hasNext()) {
                    Node highNode = highIter.next();
                    if ((highNode.getTextContent() != null) && (!"".equals(highNode.getTextContent()))) {
                        crit.setHighBoundKey(new ReplaceParamKey(highNode.getTextContent()));
                    }
                }
            }
            crit.addGuesses(collectGuess(cSource.getSource()));
        }
        return crit;
    }
    
    protected List<IAcceptanceGuess> collectGuess(Node parentNode) {
        List<IAcceptanceGuess> result = new ArrayList<>();
        if ((parentNode != null) && (parentNode.hasChildNodes())) {
            Node currNode = parentNode.getFirstChild();
            do {
                if (XmlConfigNodeType.DT_INTERVAL_GUESS.equalsNode(currNode)) {
                    IDTIntervalGuess dtIntGuess = AcceptanceCriterionFactory.getInstance().getDTIntervalGuess();
                    String guessName = null;
                    if (currNode.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
                        guessName = currNode.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME).getNodeValue();
                    }
                    dtIntGuess.setName(guessName);
                    List<IRealRegex> allGuessRegex = ConfigXmlUtil.collectAllRegex(currNode);
                    if ((allGuessRegex != null) && (!allGuessRegex.isEmpty())) {
                        for (IRealRegex reg : allGuessRegex) {
                            if ((dtIntGuess.getFirstRecord() == null) && (GUESS_REGEX_FIRST_RECORD.equals(reg.getRegexName()))) {
                                dtIntGuess.setFirstRecord(reg);
                            } else if ((dtIntGuess.getLastRecord() == null) && (GUESS_REGEX_LAST_RECORD.equals(reg.getRegexName()))) {
                                dtIntGuess.setLastRecord(reg);
                            }
                        }
                    } else {
                        log.error("collectGuess Regex array is empty");
                    }
                    result.add(dtIntGuess);
                }
                currNode = currNode.getNextSibling();
            } while (currNode != null);
        }
        return result;
    }

}
