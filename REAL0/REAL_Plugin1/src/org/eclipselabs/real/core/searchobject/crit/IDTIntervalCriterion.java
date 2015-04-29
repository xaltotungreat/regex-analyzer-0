package org.eclipselabs.real.core.searchobject.crit;

import java.time.LocalDateTime;

import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;

public interface IDTIntervalCriterion extends IAcceptanceCriterion {

    public ReplaceParamKey getLowBoundKey();
    public void setLowBoundKey(ReplaceParamKey lowBoundKey);
    public ReplaceParamKey getHighBoundKey();
    public void setHighBoundKey(ReplaceParamKey highBoundKey);

    public LocalDateTime getLowBound();
    public void setLowBound(LocalDateTime lowBound);
    public LocalDateTime getHighBound();
    public void setHighBound(LocalDateTime highBound);
}
