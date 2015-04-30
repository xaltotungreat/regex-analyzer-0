package org.eclipselabs.real.core.searchobject.param;

import java.util.Set;
import java.util.function.Predicate;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.util.IKeyedObject;
import org.eclipselabs.real.core.util.ITypedObject;

public interface IReplaceParam<T> extends IKeyedObject<ReplaceParamKey>, ITypedObject<ReplaceParamValueType>, Cloneable {

    public static class IRPTypePredicate implements Predicate<IReplaceParam<?>>{
        ReplaceParamValueType tp;

        public IRPTypePredicate(ReplaceParamValueType t) {
            tp = t;
        }

        @Override
        public boolean test(IReplaceParam<?> t) {
            if (t.getType().equals(tp)) {
                return true;
            }
            return false;
        }

        public ReplaceParamValueType getTp() {
            return tp;
        }

        public void setTp(ReplaceParamValueType tp) {
            this.tp = tp;
        }
    }

    public String getName();
    public void setName(String aName);
    public Set<String> getReplaceNames();
    public void setReplaceNames(Set<String> newRN);
    public ISearchObjectGroup<String> getGroup();
    public void setGroup(ISearchObjectGroup<String> aName);
    public String getDescription();
    public void setDescription(String aDescr);
    public T getValue();
    public void setValue(T aValue);
    public IReplaceParam<T> clone() throws CloneNotSupportedException;
}
