package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchobject.param.IReplaceableParamRepository;

public interface ISearchObjectController {
    public ISearchObjectRepository getSearchObjectRepository();
    public IReplaceableParamRepository getReplaceParamRepository();
}
