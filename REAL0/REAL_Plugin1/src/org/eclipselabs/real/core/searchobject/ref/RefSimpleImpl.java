package org.eclipselabs.real.core.searchobject.ref;

/**
 * This class represents a simple ref i.e. it contains a value and
 * the instruction what to do with the value (ADD, REPLACE etc)
 * It shouldn't contain any other refs as children
 * Refs that contain other refs as children should extend RefImpl
 *
 * @author Vadim Korkin
 *
 * @param <T> the type of the object that this param is to modify
 */
public abstract class RefSimpleImpl<T> extends RefImpl<T> implements IRefSimple<T> {

    public RefSimpleImpl(RefType aType, String aName) {
        super(aType, aName);
    }

    public RefSimpleImpl(RefType aType, String aName, Integer pos) {
        super(aType, aName, pos);
    }

    /**
     * The ref is resolved to its own value by default
     * it is true for simple parameters - they have no child parameters
     * to modify the value. Compound parameters should override this method
     * Ref params that contain a list of params to perform the same action on
     * for example add all params have List<T> as the value of the ref
     *
     * @return the value of the parameter unchanged
     */
    @Override
    public T resolve(T originalObj) {
        return refValue;
    }

    /**
     * Ref params return null by default.
     * Compound params have to override this method
     * @return null for simple params (and by default)
     */
    @Override
    protected T getClone(T obj) {
        return null;
    }

    /**
     * For a simple parameter (default) this method returns true
     * Compound parameters have to override this method
     */
    @Override
    public boolean matchByParameters(T obj) {
        return true;
    }

    /**
     * For simple parameters (that contain no child parameters)
     * this method returns 0 that means 0 parameters have been added
     */
    @Override
    protected Integer addParameters(T obj) {
        return 0;
    }

    /**
     * For simple parameters (that contain no child parameters)
     * this method returns 0 that means 0 parameters have been replaced/added
     */
    @Override
    protected Integer replaceAddParameters(T obj) {
        return 0;
    }

    /**
     * For simple parameters (that contain no child parameters)
     * this method returns 0 that means 0 parameters have been replaced
     */
    @Override
    protected Integer replaceParameters(T obj) {
        return 0;
    }

    /**
     * For simple parameters (that contain no child parameters)
     * this method returns 0 that means 0 parameters have been removed
     */
    @Override
    protected Integer removeParameters(T obj) {
        return 0;
    }

    /**
     * For simple parameters (that contain no child parameters)
     * this method returns 0 that means 0 parameters have been added
     */
    @Override
    protected Integer cpAdd(T obj) {
        return 0;
    }

    /**
     * For simple parameters (that contain no child parameters)
     * this method returns 0 that means 0 parameters have been added
     */
    @Override
    protected Integer cpReplace(T obj) {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((refType == null) ? 0 : refType.hashCode());
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
        RefSimpleImpl<?> other = (RefSimpleImpl<?>) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (refType != other.refType) {
            return false;
        }
        return true;
    }

}
