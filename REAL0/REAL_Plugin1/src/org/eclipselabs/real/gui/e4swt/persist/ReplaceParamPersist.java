package org.eclipselabs.real.gui.e4swt.persist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.util.IRealCoreConstants;

@XmlType(propOrder={"valueType","name","replaceNames","value"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplaceParamPersist<T> {

    protected ReplaceParamValueTypePersist valueType;
    protected String name;
    protected Set<String> replaceNames;
    protected T value;

    public ReplaceParamPersist() {
    }
    
    public ReplaceParamPersist(String aName, T aValue) {
        name = aName;
        value = aValue;
    }
    
    public ReplaceParamPersist(IReplaceParam<?> param) {
        name = param.getName();
        replaceNames = param.getReplaceNames();
        valueType = ReplaceParamValueTypePersist.valueOf(param.getType().name());
        
        /*
         * If store the Calendar Type the loaded type is XMLGregorianCalndar
         * It doesn't contain milliseconds. Therefore a String with milliseconds
         * is stored instead. 
         */
        switch(param.getType()) {
        case DATE:
            SimpleDateFormat fmt = new SimpleDateFormat(IReplaceParam.DEFAULT_FORMAT_STRING_LONG, IRealCoreConstants.MAIN_DATE_LOCALE);
            value = (T)fmt.format(((IReplaceParam<Calendar>)param).getValue().getTime());
            break;
        default:
            value = (T)param.getValue();
            break;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getReplaceNames() {
        return replaceNames;
    }

    public void setReplaceNames(Set<String> replaceNames) {
        this.replaceNames = replaceNames;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public ReplaceParamValueTypePersist getValueType() {
        return valueType;
    }

    public void setValueType(ReplaceParamValueTypePersist valueType) {
        this.valueType = valueType;
    }

}
