package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchobject.param.IReplaceableParamRepository;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamRepositoryImpl;

public enum SearchObjectController implements ISearchObjectController {
    INSTANCE;
    protected ISearchObjectRepository soRep = new SearchObjectRepositoryImpl();
    protected IReplaceableParamRepository rpRep = new ReplaceableParamRepositoryImpl();
    
    @Override
    public ISearchObjectRepository getSearchObjectRepository() {
        return soRep;
    }

    @Override
    public IReplaceableParamRepository getReplaceableParamRepository() {
        return rpRep;
    }
    
}
