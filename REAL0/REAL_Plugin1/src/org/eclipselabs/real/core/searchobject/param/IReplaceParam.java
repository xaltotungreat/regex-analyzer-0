package org.eclipselabs.real.core.searchobject.param;

import java.util.Set;
import java.util.function.Predicate;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.util.IKeyedObject;
import org.eclipselabs.real.core.util.ITypedObject;

/**
 * The basic interface for a replace parameter.
 * A replace parameter may consist of
 * 1. One or more names
 * 2. The value that is not necessarily is a String
 *
 * The replace parameters are used mostly in regular expressions.
 * The regex template in the configuration contains the name of the parameter and this name
 * is replaced with value every time this regex is searched for. The value
 * is defined by the user. This makes regular expressions reusable.
 *
 * For example the regex in the configuration is
 * (?<=(aaa))REPLACE_PARAM\d{10}
 * The user wants to search for aaaXXX1234567890
 * Then this regular expression is executed with REPLACE_PARAM = XXX
 *
 * The user sets the value via the GUI.
 *
 * @author Vadim Korkin
 *
 * P.S. The correct name would be Replaceable Parameter this the current name
 * is shorter and easier to use in code and comments
 *
 * @param <T> the type of the value of this replace parameter
 */
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
