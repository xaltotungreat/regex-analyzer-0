package org.eclipselabs.real.core.searchobject.ref;

import java.util.function.Predicate;

import org.eclipselabs.real.core.util.ITypedObject;

/**
 * This is the basic ref interface. Refs can be simple or compound
 * A simple ref contains a value and the instructions what to do with the value.
 * The instruction include the action "ADD", "REMOVE" may include
 * the position (where to insert for example).
 * The compound ref includes not the value but other refs that are used to
 * resolve the ref.
 * The concept of "resoluion" is very important for both ref types
 * In the usual sense to resolve a ref means to find the value that is referenced.
 * In REAL it means not only to find this value but (for a compound ref)
 * apply all child refs to get the end value. Simple refs are resolved to the value.
 *
 * That means in order to resolve a ref first the original value has to be found.
 * Usually it is achieved by comparing some parameters from the value in the ref
 * with the same parameters of existing objects. Usually even if type is REMOVE
 * have to be present because it is needed to find the original object to remove.
 *
 * @author Vadim Korkin
 *
 * @param <T> the type of the object that is referenced
 */
public interface IRef<T> extends ITypedObject<RefType> {

    /**
     * The type is the action that has to be taken with this ref.
     * For example
     * ADD - this means the value has to be added to the list of original objects
     * (maybe inserted to a position)
     * REMOVE - the first matching value has to be found and removed
     * @param newType
     */
    public void setType(RefType newType);

    /**
     * This is the value of this ref.
     * Usually it has to be present to find the first match
     * @return the value of this ref
     */
    public T getValue();
    /**
     *
     * @param newValue the new value for this ref
     */
    public void setValue(T newValue);

    /**
     * For some objects that can be referenced the position is important
     * Therefore a basic ref may include position. If it is null
     * then this position is not used
     * @return
     */
    public Integer getPosition();
    /**
     * For some objects that can be referenced the position is important
     * Therefore a basic ref may include position. If it is null
     * then this position is not used
     * @param position the new position for this ref
     */
    public void setPosition(Integer position);

    /**
     * This predicate is the default way of understanding if some object
     * of the correct type <T> should be used to resolve this reference.
     * The algorithm of matching doesn't have to be unique or find only
     * one match every time. Usually it is not unique and the first matching object
     * is used to resolve the reference. It is the responsibility of the ref creator
     * to make the ref unique to use the correct object for resolving.
     *
     * Also the classes extending RefImpl or IRef may use their own ways of matching objects
     * to refs (other than getDefaultMatchPredicate and defaultMatch).
     * It is only a convenience implementation.
     *
     * @return true if this object may be used to resolve this ref false otherwise
     */
    public Predicate<? super T> getDefaultMatchPredicate();

    /**
     * defaultMatch uses the default match predicate (must be implemented by subclasses)
     * to return true if the passed object "matches" this match i.e. if this object
     * should be used to resolve this ref. The refs may use or not use this default matching
     * (may use some other methods for matching refs to objects). This method is provided
     * for convenience.
     * @param obj the object to test if it "matches" this ref
     * @return
     */
    public boolean defaultMatch(T obj);

    /**
     * Some parameters may have the type MATCH. This means
     * instead of changing the object they serve to find the correct matching object
     * to resolve that reference. Simple parameters return true for this method,
     * compound parameters have to override this method.
     * @param obj
     * @return true if all child refs match the values of obj false otherwise
     */
    public boolean matchByParameters(T obj);

    /**
     * This method should do the following:
     *
     * 1. Get an independent reference to a copy of originalObj
     * This can be done by means of cloning this object or any other way scan be chosen
     * It is very important that the clone shares no references with originalObj because
     * the parameters of the clone may be changed
     *
     * 2. Handle all ref parameters. Ref parameters contain information
     * what parameter of originalObj has to be changed and how.
     * For example a ref parameter for a String field can have the type REPLACE
     * and contain the new value. In that case in the method resolve the field
     * will be replaced with the new value
     *
     * @param originalObj the object that will be changed by this ref
     * @return a clone of originalObj with some parameters changed
     */
    public T resolve(T originalObj);
}
