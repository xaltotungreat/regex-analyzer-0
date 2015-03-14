package org.eclipselabs.real.core.searchobject;

import org.eclipselabs.real.core.searchobject.param.IReplaceParamRepository;

public interface ISearchObjectController {
    public ISearchObjectRepository getSearchObjectRepository();
    public IReplaceParamRepository getReplaceParamRepository();
}
