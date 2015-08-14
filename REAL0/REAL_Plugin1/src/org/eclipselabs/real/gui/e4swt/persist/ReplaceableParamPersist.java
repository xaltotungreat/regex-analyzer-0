package org.eclipselabs.real.gui.e4swt.persist;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.util.IRealCoreConstants;

@XmlType(propOrder={"valueType","name","replaceNames","value"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplaceableParamPersist<T> {

    protected ReplaceableParamValueTypePersist valueType;
    protected String name;
    protected Set<String> replaceNames;
    protected T value;

    public ReplaceableParamPersist() {
    }

    public ReplaceableParamPersist(String aName, T aValue) {
        name = aName;
        value = aValue;
    }

    public ReplaceableParamPersist(IReplaceableParam<?> param) {
        name = param.getName();
        replaceNames = param.getReplaceNames();
        valueType = ReplaceableParamValueTypePersist.valueOf(param.getType().name());

        /*
         * JAXB as of 2015-04-29 doesn't work with java.time classes
         * Therefore the datetime is converted to String and stored as String.
         */
        switch(param.getType()) {
        case DATE:
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(IRealCoreConstants.DEFAULT_FORMAT_DATE_LONG, IRealCoreConstants.DEFAULT_DATE_LOCALE);
            value = (T)fmt.format(((IReplaceableParam<LocalDateTime>)param).getValue());
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

    public ReplaceableParamValueTypePersist getValueType() {
        return valueType;
    }

    public void setValueType(ReplaceableParamValueTypePersist valueType) {
        this.valueType = valueType;
    }

}
