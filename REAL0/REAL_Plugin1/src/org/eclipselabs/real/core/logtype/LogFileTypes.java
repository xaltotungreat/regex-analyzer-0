package org.eclipselabs.real.core.logtype;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.logfile.LogFileTypeRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public enum LogFileTypes {
    INSTANCE;

    private static final Logger log = LogManager.getLogger(LogFileTypes.class);

    LogFileTypeRepository logFileTypeRep = new LogFileTypeRepository();
    //private String xmlFileName;

    public void initXml(InputStream aIS, InputStream activationIS) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        final Document doc;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(aIS);
            parseDomStructure(doc.getDocumentElement());
            if (activationIS != null) {
                loadActiveStates(activationIS);
            } else {
                log.info("startup No active configuration found - loading default (all active)");
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            log.error("Parsing log types Exception", e);
        }
    }

    public void initXml(String aXmlFileName) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        final Document doc;
        //xmlFileName = aXmlFileName;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(aXmlFileName);
            parseDomStructure(doc.getDocumentElement());
        } catch (IOException | SAXException | ParserConfigurationException e) {
            log.error("Parsing log types Exception", e);
        }
    }

    protected void parseDomStructure(Node elem) {
        if (XmlConfigNodeType.LOG_TYPES.equalsNode(elem)) {
            NodeList types = ((Element)elem).getElementsByTagName(XmlConfigNodeType.LOG_TYPE.getNodeName());
            for (int i = 0; i < types.getLength(); i++) {
                Element currLogType = (Element)types.item(i);
                addLogTypeFromXml(currLogType);
            }
        }
    }

    protected void addLogTypeFromXml(Element xmlLogType) {
        String logTypeName = xmlLogType.getAttribute(IConfigXmlConstants.ATTRIBUTE_NAME_NAME);
        log.info("Adding log type name=" + logTypeName);
        LogFileTypeKey newKey = new LogFileTypeKey(logTypeName);
        LogFileType newType = new LogFileType(logTypeName, new LogFileTypeState(true, false));
        // active if this is the first start or set active in the active config
        NodeList patternsList = xmlLogType.getElementsByTagName(XmlConfigNodeType.FILENAME_PATTERNS.getNodeName());
        for (int i = 0; i < patternsList.getLength(); i++) {
            Element patternsNode = (Element)patternsList.item(i);
            NodeList patternList = patternsNode.getElementsByTagName(XmlConfigNodeType.FILENAME_PATTERN.getNodeName());
            for (int j = 0; j < patternList.getLength(); j++) {
                newType.addPattern(patternList.item(j).getTextContent());
                log.info("Adding log type name=" + logTypeName + " pattern=" + patternList.item(j).getTextContent());
            }
        }
        logFileTypeRep.add(newKey, newType);
    }

    protected void loadActiveStates(InputStream activationIS) {
        // load the active states
        try {
            JAXBContext mrshContext;
            //Schema wsSchema = PersistUtil.getSchema(IEclipse4Constants.PATH_WORKSPACE_PERSIST_SCHEMA);
            mrshContext = JAXBContext.newInstance(LogTypeActivation.class);
            Unmarshaller mrsh = mrshContext.createUnmarshaller();
            //mrsh.setSchema(wsSchema);
            LogTypeActivation activation = (LogTypeActivation)mrsh.unmarshal(activationIS);
            LogFileTypes.INSTANCE.setLogTypeEnableStates(activation.getStates());
        } catch (JAXBException e) {
            log.error("dispose Marshalling exception",e);
        }
    }

    public Map<String, Boolean> getEnableMap() {
        List<LogFileType> allTypes = getAllTypes();
        /*Map<String, Boolean> returnMap = new TreeMap<String, Boolean>();
        List<LogFileType> allTypes = getAllTypes();
        for (LogFileType currType : allTypes) {
            returnMap.put(currType.getLogTypeName(), currType.isEnabled());
        }
        allTypes.forEach((a) -> returnMap.put(a.getLogTypeName(), a.isEnabled()));*/
        return new TreeMap<String, Boolean>(allTypes.stream().collect(Collectors.toMap(LogFileType::getLogTypeName, LogFileType::isEnabled)));
    }

    public List<LogFileType> getAllTypes() {
        return logFileTypeRep.getAllValues();
    }

    public Set<LogFileTypeKey> getAllTypeKeys() {
        return logFileTypeRep.getAllKeys();
    }

    public Set<LogFileTypeKey> getAllTypeKeys(Predicate<LogFileType> typePredicate) {
        return logFileTypeRep.getKeys(typePredicate);
    }

    public LogFileType getLogFileType(String typeName) {
        LogFileType returnlfType = null;
        if (typeName != null) {
            returnlfType = logFileTypeRep.get(new LogFileTypeKey(typeName));
        }
        return returnlfType;
    }

    public Set<String> getPatterns(String name) {
        return getPatterns(new LogFileTypeKey(name));
    }

    public Set<String> getPatterns(LogFileTypeKey typeKey) {
        LogFileType typeObj = logFileTypeRep.get(typeKey);
        if (typeObj != null) {
            return typeObj.getFilePatterns();
        }
        return null;
    }

    public void setLogTypeEnableState(String name, boolean newState) {
        LogFileType typeObj = logFileTypeRep.get(new LogFileTypeKey(name));
        if (typeObj != null) {
            typeObj.setEnabled(newState);
        }
    }

    public void setLogTypeEnableStates(Map<String, Boolean> states) {
        for (Map.Entry<String, Boolean> oneStateEntry : states.entrySet()) {
            setLogTypeEnableState(oneStateEntry.getKey(), oneStateEntry.getValue());
        }
    }
}

