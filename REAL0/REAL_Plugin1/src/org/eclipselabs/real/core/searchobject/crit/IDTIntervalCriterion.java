package org.eclipselabs.real.core.searchobject.crit;

import java.time.LocalDateTime;

import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;

/**
 * The interface of the datetime interval criterion.
 * It selects only the records with datetime within the interval
 * (inclusively or exclusively is defined by the type)
 * Usually the values for the low and high bounds are entered
 * by the user (as replaceable parameters) and the criterion retrieves them
 * from the search result.
 *
 * @author Vadim Korkin
 *
 */
public interface IDTIntervalCriterion extends IAcceptanceCriterion {

    public ReplaceableParamKey getLowBoundKey();
    public void setLowBoundKey(ReplaceableParamKey lowBoundKey);
    public ReplaceableParamKey getHighBoundKey();
    public void setHighBoundKey(ReplaceableParamKey highBoundKey);

    public LocalDateTime getLowBound();
    public void setLowBound(LocalDateTime lowBound);
    public LocalDateTime getHighBound();
    public void setHighBound(LocalDateTime highBound);
}
