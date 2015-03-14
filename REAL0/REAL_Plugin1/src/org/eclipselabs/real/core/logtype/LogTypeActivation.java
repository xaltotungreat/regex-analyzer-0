package org.eclipselabs.real.core.logtype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"states"})
public class LogTypeActivation {

    private Map<String, Boolean> states;

    public Map<String, Boolean> getStates() {
        return states;
    }

    public void setStates(Map<String, Boolean> states) {
        this.states = states;
    }

    public static LogTypeActivation loadFromList(List<LogFileType> allTypes) {
        LogTypeActivation inst = new LogTypeActivation();
        Map<String, Boolean> loadedStates = new HashMap<String, Boolean>();
        for (LogFileType lft : allTypes) {
            loadedStates.put(lft.getLogTypeName(), lft.isEnabled());
        }
        inst.setStates(loadedStates);
        return inst;
    }

}
