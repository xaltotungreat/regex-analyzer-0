package org.eclipselabs.real.core.searchobject.ref;

/**
 * This enum specifies the types of refs.
 * The type of the ref tell the framework what to do with it.
 * For example if the type of some ref is ADD and it has a value and a position
 * then the value of this ref will be added to he specified position
 * @author Vadim Korkin
 *
 */
public enum RefType {

    /**
     * The ref with this type must be matched
     */
    MATCH,
    /**
     * The value of a ref with this type must be added either to a specified position
     * or "simply" added (implementation is provided by the ref).
     * Usually it is assumed that this ref cannot be added if another value
     * matching this ref already exists (details are implementation-specific)
     */
    ADD,
    /**
     * The value of the ref with this type must be added either to a specified position
     * or "simply" added/replaced (implementation is provided by the ref).
     * Usually it is assumed that this ref must replace the first matching value 
     * (details are implementation-specific)
     */
    REPLACE_ADD,
    /**
     * The value of this ref must replace an already existing value
     * Usually it is assumed that the value to replace is specified
     * by the position or some other matching strategy
     */
    REPLACE,
    /**
     * The first value matching this ref must be removed
     */
    REMOVE,
    /**
     * For compound refs (ref containing other refs as children) this type
     * specifies that 
     * 1. The first value matching this ref must be found
     * 2. It is cloned
     * 3. The parameters of the cloned object are changed in accordance with
     * the refs to parameters of this ref
     * 4. The resulting value is added (to he position of simply added)
     */
    CP_ADD,
    /**
     * For compound refs (ref containing other refs as children) this type
     * specifies that 
     * 1. The first value matching this ref must be found
     * 2. It is cloned
     * 3. The parameters of the cloned object are changed in accordance with
     * the refs to parameters of this ref
     * 4. The resulting value replaces either the value in the specified position
     * or the first matching value (if no position is specified) 
     */
    CP_REPLACE;
}
