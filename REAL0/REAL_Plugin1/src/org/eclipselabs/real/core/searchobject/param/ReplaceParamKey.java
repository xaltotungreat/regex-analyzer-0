package org.eclipselabs.real.core.searchobject.param;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.util.RealPredicate;

public class ReplaceParamKey implements Cloneable {
    protected String rpName;
    protected ISearchObjectGroup<String> rpGroup;
    
    public static class RPKNamePredicate implements RealPredicate<ReplaceParamKey> {
        String rpKeyName;
        
        public RPKNamePredicate(String aRPName) {
            rpKeyName = aRPName;
        }

        @Override
        public boolean test(ReplaceParamKey t) {
            boolean returnVal = false;
            if ((rpKeyName != null) && (t != null) && (t.getRPName() != null)) {
                returnVal = rpKeyName.equals(t.getRPName());
            } else if ((rpKeyName == null) && (t != null) && (t.getRPName() == null)) {
                returnVal = true;
            }
            return returnVal;
        }
    }
    
    public static class RPKGroupStartsWithPredicate implements RealPredicate<ReplaceParamKey> {
        ISearchObjectGroup<String> rpKeyGroup;
        
        public RPKGroupStartsWithPredicate(ISearchObjectGroup<String> aRPName) {
            rpKeyGroup = aRPName;
        }

        @Override
        public boolean test(ReplaceParamKey t) {
            boolean returnVal = false;
            if ((rpKeyGroup != null) && (t != null) && (t.getRPGroup() != null)) {
                //returnVal = rpKeyGroup.equals(t.getRPGroup());
                returnVal = t.getRPGroup().startsWith(rpKeyGroup);
            } else if ((rpKeyGroup == null) && (t != null) && (t.getRPGroup() == null)) {
                returnVal = true;
            }
            return returnVal;
        }
    }
    
    public static class RPKGroupEqualsPredicate implements RealPredicate<ReplaceParamKey> {
        ISearchObjectGroup<String> rpKeyGroup;
        
        public RPKGroupEqualsPredicate(ISearchObjectGroup<String> aRPName) {
            rpKeyGroup = aRPName;
        }

        @Override
        public boolean test(ReplaceParamKey t) {
            boolean returnVal = false;
            if ((rpKeyGroup != null) && (t != null) && (t.getRPGroup() != null)) {
                //returnVal = rpKeyGroup.equals(t.getRPGroup());
                returnVal = t.getRPGroup().startsWith(rpKeyGroup);
            } else if ((rpKeyGroup == null) && (t != null) && (t.getRPGroup() == null)) {
                returnVal = true;
            }
            return returnVal;
        }
    }
    
    public ReplaceParamKey(ReplaceParamKey otherKey) {
        rpName = otherKey.getRPName();
        rpGroup = otherKey.getRPGroup();
    }
    
    public ReplaceParamKey(String name) {
        rpName = name;
    }
    
    public ReplaceParamKey(String name, ISearchObjectGroup<String> path) {
        rpName = name;
        rpGroup = path;
    }
    
    public String getRPName() {
        return rpName;
    }
    public void setRPName(String soName) {
        this.rpName = soName;
    }

    public ISearchObjectGroup<String> getRPGroup() {
        return rpGroup;
    }
    public void setRPGroup(ISearchObjectGroup<String> soGroup) {
        this.rpGroup = soGroup;
    }

    @Override
    public ReplaceParamKey clone() throws CloneNotSupportedException {
        ReplaceParamKey clonedObj = (ReplaceParamKey)super.clone();
        if (rpGroup != null) {
            clonedObj.setRPGroup(rpGroup.clone());
        }
        return clonedObj;
    }

    @Override
    public String toString() {
        return "ReplaceParamKey [rpName=" + rpName + ", rpGroup=" + rpGroup + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rpName == null) ? 0 : rpName.hashCode());
        result = prime * result + ((rpGroup == null) ? 0 : rpGroup.hashCode());
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
        ReplaceParamKey other = (ReplaceParamKey) obj;
        if (rpName == null) {
            if (other.rpName != null)
                return false;
        } else if (!rpName.equals(other.rpName))
            return false;
        if (rpGroup == null) {
            if (other.rpGroup != null)
                return false;
        } else if (!rpGroup.equals(other.rpGroup))
            return false;
        return true;
    }
}
