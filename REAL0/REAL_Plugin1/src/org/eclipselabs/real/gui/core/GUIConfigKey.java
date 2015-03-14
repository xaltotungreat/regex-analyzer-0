package org.eclipselabs.real.gui.core;

import org.eclipselabs.real.core.util.ITypedObject;

public class GUIConfigKey implements ITypedObject<GUIConfigObjectType>{
    

    protected GUIConfigObjectType type;
    private String name;
    
    public GUIConfigKey(GUIConfigObjectType aType) {
        type = aType;
    }
    
    public GUIConfigKey(GUIConfigObjectType aType, String aName) {
        type = aType;
        setName(aName);
    }

    public String getTypeName() {
        return type.getTypeName();
    }

    @Override
    public GUIConfigObjectType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GUIConfigKey other = (GUIConfigKey) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "GUIConfigKey [type=" + type + ", name=" + name + "]";
    }

}
