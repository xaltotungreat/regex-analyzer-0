package org.eclipselabs.real.core.distrib;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DistribTaskResult<R> implements IDistribTaskResultWrapper<R> {

    private R taskResult;

    private Map<String, Object> internalNVPs = new HashMap<>();


    public DistribTaskResult() {
        // empty constructor
    }

    public DistribTaskResult(R res) {
        taskResult = res;
    }

    public DistribTaskResult(R res, Map<String, Object> nvps) {
        taskResult = res;
        if (nvps != null) {
            internalNVPs.putAll(nvps);
        }
    }

    @Override
    public R getActualResult() {
        return taskResult;
    }

    @Override
    public void setActualResult(R res) {
        taskResult = res;
    }

    @Override
    public Map<String, Object> getNVPs() {
        return internalNVPs;
    }

    @Override
    public Optional<Object> getNVPValue(String key) {
        Optional<Object> res = Optional.empty();
        if (key != null) {
            Object val = internalNVPs.get(key);
            res = Optional.ofNullable(val);
        }
        return res;
    }

    @Override
    public void putNVPs(Map<String, Object> newNVPs) {
        if (newNVPs != null) {
            internalNVPs.putAll(newNVPs);
        }
    }

    @Override
    public void putNVP(String key, Object value) {
        if ((key != null) && (value != null)) {
            internalNVPs.put(key, value);
        }
    }



}
