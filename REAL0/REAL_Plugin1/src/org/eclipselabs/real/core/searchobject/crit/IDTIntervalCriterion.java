package org.eclipselabs.real.core.searchobject.crit;

import java.util.Calendar;

import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;

public interface IDTIntervalCriterion extends IAcceptanceCriterion {

    public ReplaceParamKey getLowBoundKey();
    public void setLowBoundKey(ReplaceParamKey lowBoundKey);
    public ReplaceParamKey getHighBoundKey();
    public void setHighBoundKey(ReplaceParamKey highBoundKey);
    
    public Calendar getLowBound();
    public void setLowBound(Calendar lowBound);
    public Calendar getHighBound();
    public void setHighBound(Calendar highBound);
}
