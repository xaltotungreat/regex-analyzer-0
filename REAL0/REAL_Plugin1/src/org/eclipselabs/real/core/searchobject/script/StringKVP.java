package org.eclipselabs.real.core.searchobject.script;

public class StringKVP {

    protected String name;
    protected String value;
    
    public StringKVP(String aName, String aValue) {
        name = aName;
        value = aValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
