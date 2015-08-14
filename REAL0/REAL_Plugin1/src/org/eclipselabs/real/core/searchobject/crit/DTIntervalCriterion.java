package org.eclipselabs.real.core.searchobject.crit;

import java.time.LocalDateTime;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchobject.ISearchObjectConstants;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamValueType;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public class DTIntervalCriterion extends AcceptanceCriterionImpl implements IDTIntervalCriterion {

    private static final Logger log = LogManager.getLogger(DTIntervalCriterion.class);
    protected ReplaceableParamKey lowBoundKey;
    protected ReplaceableParamKey highBoundKey;
    protected LocalDateTime lowBound;
    protected LocalDateTime highBound;

    public DTIntervalCriterion(AcceptanceCriterionType aType) {
        this(aType, null);
    }

    public DTIntervalCriterion(AcceptanceCriterionType aType, String newName) {
        super(aType, newName, AcceptanceCriterionStage.SEARCH);
    }

    @Override
    public boolean test(ISearchResultObject sro, ISearchResult<? extends ISearchResultObject> sr) {
        boolean result = true;
        if (sro.getDate() == null) {
            result = false;
        }
        init(sr);
        if (result && (lowBound != null) && (highBound != null)) {
            LocalDateTime useDate = sro.getDate();
            /*
             * Trying to guess the correct year (if no year then 1970) from the bounds
             */
            if (useDate.getYear() == ISearchObjectConstants.DEFAULT_NOT_FOUND_YEAR) {
                if (useDate.getMonth() == lowBound.getMonth()) {
                    useDate = useDate.withYear(lowBound.getYear());
                } else if (useDate.getMonth() == highBound.getMonth()) {
                    useDate = useDate.withYear(highBound.getYear());
                } else {
                    useDate = useDate.withYear(lowBound.getYear());
                }
            }
            switch (type) {
            case INTERVAL:
                result = (useDate.compareTo(lowBound) >= 0) && (useDate.compareTo(highBound) <= 0);
                break;
            case INTERVAL_EXCLUSIVE:
                result = (useDate.compareTo(lowBound) > 0) && (useDate.compareTo(highBound) < 0);
                break;
            default:
                log.error("test criterion type " + type + " is not supported for dt interval");
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    public void init(ISearchResult<? extends ISearchResultObject> sr) {
        Map<ReplaceableParamKey, IReplaceableParam<?>> allReplaceParams = sr.getAllCachedReplaceParams();
        if (allReplaceParams == null) {
            log.warn("loadParameters all replace params is null");
            return;
        }
        if (lowBoundKey != null) {
            IReplaceableParam<?> lowBoundParam = allReplaceParams.get(lowBoundKey);
            if (lowBoundParam != null) {
                if ((ReplaceableParamValueType.DATE.equals(lowBoundParam.getType()))) {
                    IReplaceableParam<LocalDateTime> lowBoundParamType = (IReplaceableParam<LocalDateTime>)lowBoundParam;
                    lowBound = lowBoundParamType.getValue();
                } else {
                    log.error("init incorrect param type expected " + ReplaceableParamValueType.DATE +
                            " received " + lowBoundParam.getType());
                }
            } else {
                log.error("init param " + lowBoundKey + " not found criterion " + getName());
            }
        } else {
            log.error("init lowBoundKey is null");
        }
        if (highBoundKey != null) {
            IReplaceableParam<?> highBoundParam = allReplaceParams.get(highBoundKey);
            if (highBoundParam != null) {
                if ((ReplaceableParamValueType.DATE.equals(highBoundParam.getType()))) {
                    IReplaceableParam<LocalDateTime> highBoundParamType = (IReplaceableParam<LocalDateTime>)highBoundParam;
                    highBound = highBoundParamType.getValue();
                } else {
                    log.error("init incorrect param type expected " + ReplaceableParamValueType.DATE +
                            " received " + highBoundParam.getType());
                }
            } else {
                log.error("init param " + highBoundKey + " not found criterion " + getName());
            }
        } else {
            log.error("init highBoundKey is null");
        }
    }

    @Override
    public IAcceptanceCriterion clone() throws CloneNotSupportedException {
        DTIntervalCriterion cloneObj = (DTIntervalCriterion)super.clone();
        if (lowBoundKey != null) {
            cloneObj.setLowBoundKey(lowBoundKey.clone());
        }
        if (highBoundKey != null) {
            cloneObj.setHighBoundKey(highBoundKey.clone());
        }
        if (lowBound != null) {
            cloneObj.setLowBound(lowBound);
        }
        if (highBound != null) {
            cloneObj.setHighBound(highBound);
        }
        return cloneObj;
    }

    @Override
    public ReplaceableParamKey getLowBoundKey() {
        return lowBoundKey;
    }

    @Override
    public void setLowBoundKey(ReplaceableParamKey lowBoundKey) {
        this.lowBoundKey = lowBoundKey;
    }

    @Override
    public ReplaceableParamKey getHighBoundKey() {
        return highBoundKey;
    }

    @Override
    public void setHighBoundKey(ReplaceableParamKey highBoundKey) {
        this.highBoundKey = highBoundKey;
    }

    @Override
    public LocalDateTime getLowBound() {
        return lowBound;
    }

    @Override
    public void setLowBound(LocalDateTime lowBound) {
        this.lowBound = lowBound;
    }

    @Override
    public LocalDateTime getHighBound() {
        return highBound;
    }

    @Override
    public void setHighBound(LocalDateTime highBound) {
        this.highBound = highBound;
    }

    @Override
    public String toString() {
        return "DTIntervalCriterion [type=" + type + ", name=" + name + ", accumulating=" + accumulating +
                "lowBoundKey=" + lowBoundKey + ", highBoundKey=" + highBoundKey + "]";
    }

}
