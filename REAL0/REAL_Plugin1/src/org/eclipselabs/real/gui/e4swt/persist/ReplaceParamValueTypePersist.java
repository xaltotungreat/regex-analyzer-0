package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum ReplaceParamValueTypePersist {

    STRING,
    INTEGER,
    BOOLEAN,
    DATE;
}
