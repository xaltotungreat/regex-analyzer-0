package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchobject.param.IReplaceParamRepository;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamRepositoryImpl;

public enum SearchObjectController implements ISearchObjectController {
    INSTANCE;
    protected ISearchObjectRepository soRep = new SearchObjectRepositoryImpl();
    protected IReplaceParamRepository rpRep = new ReplaceParamRepositoryImpl();
    
    @Override
    public ISearchObjectRepository getSearchObjectRepository() {
        return soRep;
    }

    @Override
    public IReplaceParamRepository getReplaceParamRepository() {
        return rpRep;
    }
    
}
