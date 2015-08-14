package org.eclipselabs.real.core.searchobject.param;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.util.IRealCoreConstants;


public class ReplaceableParamImpl<T> implements IReplaceableParam<T> {

    protected ReplaceableParamValueType type;
    protected ReplaceableParamKey theKey;
    protected String description;
    protected Set<String> replaceNames;
    protected T paramValue;

    public ReplaceableParamImpl(ReplaceableParamValueType aType, ReplaceableParamKey aKey, String aDescr, Set<String> rns, T aValue) {
        type = aType;
        theKey = aKey;
        description = aDescr;
        replaceNames = rns;
        paramValue = aValue;
    }

    public ReplaceableParamImpl(ReplaceableParamValueType aType, ReplaceableParamKey aKey, Set<String> rns, T aValue) {
        this(aType, aKey, null, rns, aValue);
    }

    public ReplaceableParamImpl(ReplaceableParamKey aKey, Set<String> rns) {
        this(ReplaceableParamValueType.STRING, aKey, rns, null);
    }

    public ReplaceableParamImpl(ReplaceableParamKey aKey, T aValue) {
        this(ReplaceableParamValueType.STRING, aKey, null, aValue);
    }

    public ReplaceableParamImpl(ReplaceableParamKey aKey, Set<String> rns, T aValue) {
        this(ReplaceableParamValueType.STRING, aKey, rns, aValue);
    }

    public ReplaceableParamImpl(ReplaceableParamKey aKey, String aDescr, Set<String> rns, T aValue) {
        this(ReplaceableParamValueType.STRING, aKey, aDescr, rns, aValue);
    }

    @Override
    public ReplaceableParamValueType getType() {
        return type;
    }

    @Override
    public ReplaceableParamKey getKey() {
        return new ReplaceableParamKey(theKey);
    }

    @Override
    public void setKey(ReplaceableParamKey aNewName) {
        theKey = aNewName;
    }

    @Override
    public String getName() {
        return theKey.getRPName();
    }

    @Override
    public void setName(String aName) {
        theKey.setRPName(aName);
    }

    @Override
    public Set<String> getReplaceNames() {
        return replaceNames;
    }

    @Override
    public void setReplaceNames(Set<String> newRN) {
        replaceNames = newRN;
    }

    @Override
    public ISearchObjectGroup<String> getGroup() {
        return theKey.getRPGroup();
    }

    @Override
    public void setGroup(ISearchObjectGroup<String> paramGroup) {
        theKey.setRPGroup(paramGroup);
    }

    @Override
    public T getValue() {
        return paramValue;
    }

    @Override
    public void setValue(T aValue) {
        paramValue = aValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((theKey == null) ? 0 : theKey.hashCode());
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
        ReplaceableParamImpl<?> other = (ReplaceableParamImpl<?>) obj;
        if (theKey == null) {
            if (other.theKey != null)
                return false;
        } else if (!theKey.equals(other.theKey))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public IReplaceableParam<T> clone() throws CloneNotSupportedException {
        ReplaceableParamImpl<T> cloneObj = (ReplaceableParamImpl<T>)super.clone();
        if (theKey != null) {
            cloneObj.theKey = theKey.clone();
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ReplaceParamImpl [type=" + type + ", theKey=" + theKey + ", replaceNames="
                + replaceNames + ", paramValue=");
        // to avoid a NPE
        String paramValStr = "" + paramValue;
        if ((ReplaceableParamValueType.DATE.equals(type)) && (paramValue != null)) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(IRealCoreConstants.DEFAULT_FORMAT_DATE_LONG, IRealCoreConstants.DEFAULT_DATE_LOCALE);
            paramValStr = fmt.format((LocalDateTime)paramValue);
        }
        sb.append(paramValStr + "]");
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String aDescr) {
        description = aDescr;
    }


}
