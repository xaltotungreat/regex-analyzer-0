package org.eclipselabs.real.gui.e4swt.persist;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
         * JAXB as of 2015-04-29 doesn't work with java.time classes
         * Therefore the datetime is converted to String and stored as String.
         */
        switch(param.getType()) {
        case DATE:
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(IReplaceParam.DEFAULT_FORMAT_STRING_LONG, IRealCoreConstants.MAIN_DATE_LOCALE);
            value = (T)fmt.format(((IReplaceParam<LocalDateTime>)param).getValue());
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
