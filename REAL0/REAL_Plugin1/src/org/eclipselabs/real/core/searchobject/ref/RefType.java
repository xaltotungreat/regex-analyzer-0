package org.eclipselabs.real.core.searchobject.ref;

/**
 * This enum specifies the types of refs.
 * The type of the ref tell the framework what to do with it.
 * For example if the type of some ref is ADD and it has a value and a position
 * then the value of this ref will be added to the specified position
 *
 * @author Vadim Korkin
 *
 */
public enum RefType {

    /**
     * The ref with this type must be matched. This type is used when the default match for
     * a compound ref is not enough. Then some parameters may be marked as MATCH.
     * Then at the resolve stage among the matching objects the first object
     * which parameters (acceptances, replace params) match the ones marked as MATCH in the ref is found.
     * This found object is considered a match for the ref.
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
     * (details are implementation-specific). This type simplifies configuration.
     *
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
     * 4. The resulting value is added (to the specified position or added to the end of the list)
     */
    /*
     * There is no type like CP_REPLACE_ADD for the following reasons.
     * For example we have 2 parameters as part of a search object
     *   Parameter1
     *     Key11=Value11
     *     Key12=Value12
     *     Key13=Value13
     *   Parameter2
     *     Key21=Value21
     *     Key22=Value22
     *     Key23=Value23
     *
     *   And we have a ref for Parameter3 of the hypothetical type CP_REPLACE_ADD
     *   This ref must be resolved to either Parameter1 or Parameter2. Then
     *   after all the child refs have been handled it can either replace one of
     *   the existing parameters or add the resolved value to the list. It is not possible
     *   to add a completely new value to the list because CP_ADD is resolved within the list.
     *   It means the key values of the ref (that define equality) cannot be changed
     *   or "accidentally" be equal to an existing value in the list.
     *   REPLACE_ADD allows one to replace if the value is equal (to avoid checking the whole hierarchy)
     *   and add if it is not equal.
     *   For the hypothetical CP_REPLACE_ADD the value cannot be accidentally equal.
     *   It must be either designed to be equal or designed to be different.
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
