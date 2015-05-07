package org.eclipselabs.real.core.searchobject.ref;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class contains the basic implementation of a reference to
 * an object. It is not necessarily a search object, it can be a ref
 * to a parameter of the search object for example the date info.
 *
 * The most important methods are: resolve and defaultMatch
 *
 * resolve has to return a reference to the resolved object i.e.
 * the original object is passed to the method, this object is cloned
 * then the following methods (abstract in this class) are executed (in this order):
 *  - addParameters
 *  - replaceAddParameters
 *  - removeParameters
 *  - cpAdd
 *  - cpReplace
 *
 * Every method is supposed to handle child refs of the corresponding type
 * (cpAdd handles CP_ADD refs, replaceAddParameters REPLACE_ADD)
 *
 * CP_ADD stands for "Change Parameters then Add"
 * CP_REPLACE stands for "Change Parameters then Replace"
 *
 * defaultMatch uses the default match predicate (must be implemented by subclasses)
 * to return true if the passed object "matches" this match i.e. if this object
 * should be used to resolve this ref. The refs may use or not use this default matching
 * (may use some other methods for matching refs to objects). This method is provided
 * for convenience.
 *
 * @author Vadim Korkin
 *
 * @param <T> the type this ref is for
 */
public abstract class RefImpl<T> implements IRef<T> {

    private static final Logger log = LogManager.getLogger(RefImpl.class);
    protected RefType refType;
    protected String name;
    protected T refValue;
    protected Integer position;

    public RefImpl(RefType aType, String aName) {
        refType = aType;
        name = aName;
    }
    public RefImpl(RefType aType, String aName, Integer pos) {
        refType = aType;
        name = aName;
        position = pos;
    }

    @Override
    public boolean defaultMatch(T obj) {
        boolean result = false;
        if (getDefaultMatchPredicate() != null) {
            result = getDefaultMatchPredicate().test(obj);
        } else {
            log.warn("defaultMatch null default match predicate return null");
        }
        return result;
    }

    @Override
    public T resolve(T originalObj) {
        T resolvedObj = null;
        if (originalObj != null) {
            resolvedObj = getClone(originalObj);
            if (resolvedObj != null) {
                addParameters(resolvedObj);
                replaceAddParameters(resolvedObj);
                replaceParameters(resolvedObj);
                removeParameters(resolvedObj);
                cpAdd(resolvedObj);
                cpReplace(resolvedObj);
            } else {
                log.error("resolve couldn't get a clone cannot resolve");
            }
        } else {
            log.error("resolve null original object cannot resolve");
        }
        return resolvedObj;
    }
    protected abstract T getClone(T obj);

    protected abstract Integer addParameters(T obj);
    protected abstract Integer replaceAddParameters(T obj);
    protected abstract Integer replaceParameters(T obj);
    protected abstract Integer removeParameters(T obj);
    protected abstract Integer cpAdd(T obj);
    protected abstract Integer cpReplace(T obj);

    @Override
    public RefType getType() {
        return refType;
    }

    @Override
    public void setType(RefType newType) {
        refType = newType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public T getValue() {
        return refValue;
    }

    @Override
    public void setValue(T newValue) {
        refValue = newValue;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        RefImpl<?> other = (RefImpl<?>) obj;
        if (refType != other.refType) {
            return false;
        }
        return true;
    }

}
