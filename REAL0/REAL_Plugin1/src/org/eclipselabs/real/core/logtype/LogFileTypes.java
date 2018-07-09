package org.eclipselabs.real.core.logtype;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.logfile.LogFileTypeRepository;
import org.springframework.context.ApplicationContext;

public enum LogFileTypes {
    INSTANCE;

    private static final Logger log = LogManager.getLogger(LogFileTypes.class);

    LogFileTypeRepository logFileTypeRep = new LogFileTypeRepository();
    public void initFromApplicationContext(ApplicationContext context, InputStream activationIS) {
        Map<String,LogFileType> allLogTypes = context.getBeansOfType(LogFileType.class);
        allLogTypes.entrySet().stream().map(ent -> (LogFileType)ent.getValue())
                .forEach(lft -> logFileTypeRep.add(new LogFileTypeKey(lft.getLogTypeName()), lft));
        if (activationIS != null) {
            loadActiveStates(activationIS);
        } else {
            log.info("startup No active configuration found - loading default (all active)");
        }
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

