package org.eclipselabs.real.core.regex;

public abstract class RealRegexParamImpl<T> implements IRealRegexParam<T> {

    protected RealRegexParamType type;
    protected String name;
    protected T value;
    
    RealRegexParamImpl(RealRegexParamType aType) {
        type = aType;
    }
    
    RealRegexParamImpl(RealRegexParamType aType, String aName, T aValue) {
        name = aName;
        value = aValue;
    }
    
    protected void init(String aName, T aValue) {
        name = aName;
        value = aValue;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T newValue) {
        value = newValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public RealRegexParamType getType() {
        return type;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RealRegexParamImpl<?> other = (RealRegexParamImpl<?>) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RealRegexParamImpl [type=" + type + ", name=" + name + ", value=" + value + "]";
    }

    

}
