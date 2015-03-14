package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;

@XmlType(propOrder={"name"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplaceParamKeyPersist {

    String name;

    public ReplaceParamKeyPersist() {
    }

    public ReplaceParamKeyPersist(ReplaceParamKey aName) {
        name = aName.getRPName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
