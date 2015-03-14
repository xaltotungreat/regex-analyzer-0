package org.eclipselabs.real.core.searchobject.ref;

import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.RealPredicate;

public class RefView<V extends ISearchObject<W,X>,W extends ISearchResult<X>, X extends ISearchResultObject,Q> 
            extends RefSimpleImpl<V> {

    protected Q viewKey;
    protected V viewSearchObject;

    public RefView(RefType aType, String aName) {
        super(aType, aName);
    }
    
    public RefView(RefType aType, String aName, Integer pos) {
        super(aType, aName, pos);
    }

    public Q getViewKey() {
        return viewKey;
    }

    public void setViewKey(Q viewKey) {
        this.viewKey = viewKey;
    }

    public V getViewSearchObject() {
        return viewSearchObject;
    }

    public void setViewSearchObject(V viewSearchObject) {
        this.viewSearchObject = viewSearchObject;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "RefView [name=" + name + ", refType=" + refType + ", viewKey=" + viewKey + ", viewSearchObject=" + viewSearchObject + ", position=" + position + "]";
    }

    @Override
    public V resolve(V originalObj) {
        return viewSearchObject;
    }

    @Override
    public boolean matchByParameters(V obj) {
        // it is simple parameter return true
        return true;
    }

    @Override
    public RealPredicate<V> getDefaultMatchPredicate() {
        // The RefView has two parameters the key and the value
        // a simple default predicate will not work here therefore return 
        // an always false predicate
        return RefUtil.getAlwaysFalsePredicate();
    }
    

}
