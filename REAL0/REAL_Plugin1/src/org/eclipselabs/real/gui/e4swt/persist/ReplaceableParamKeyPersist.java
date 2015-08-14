package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;

@XmlType(propOrder={"name"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplaceableParamKeyPersist {

    String name;

    public ReplaceableParamKeyPersist() {
    }

    public ReplaceableParamKeyPersist(ReplaceableParamKey aName) {
        name = aName.getRPName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
