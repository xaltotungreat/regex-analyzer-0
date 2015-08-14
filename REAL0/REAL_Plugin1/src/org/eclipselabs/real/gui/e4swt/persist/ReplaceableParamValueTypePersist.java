package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum ReplaceableParamValueTypePersist {

    STRING,
    INTEGER,
    BOOLEAN,
    DATE;
}
