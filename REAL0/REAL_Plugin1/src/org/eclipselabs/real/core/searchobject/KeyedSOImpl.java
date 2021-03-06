package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamValueType;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public class KeyedSOImpl implements IKeyedSO, Cloneable {

    private static final Logger log = LogManager.getLogger(KeyedSOImpl.class);
    protected IKeyedSearchObject<?, ?> keyedSO;

    protected ISearchObjectGroup<String> soGroup;
    protected Map<String,String> soTags = new ConcurrentHashMap<>();
    protected Set<LogFileTypeKey> requiredLogFileTypes;
    protected List<ISearchObjectDateInfo> dateInfos;
    protected IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> parent;

    public KeyedSOImpl(IKeyedSearchObject<?, ?> aKeyedSO) {
        keyedSO = aKeyedSO;
    }

    @Override
    public LogFileTypeKey getLogFileType() {
        LogFileTypeKey returnKey = null;
        if ((requiredLogFileTypes != null) && (!requiredLogFileTypes.isEmpty())) {
            returnKey = requiredLogFileTypes.iterator().next();
        }
        return returnKey;
    }

    public Map<String, String> getOneLevelReplaceTable(List<IReplaceableParam<String>> replaceParamList, Map<String, String> customReplaceTable) {
        Map<String, String> finalReplaceTable = new HashMap<>();
        if (replaceParamList != null) {
            for (IReplaceableParam<String> rParam : replaceParamList) {
                if (((rParam.getReplaceNames() == null) || (rParam.getReplaceNames().isEmpty()))) {
                    if ((customReplaceTable == null) || (!customReplaceTable.containsKey(rParam.getName()))) {
                        finalReplaceTable.put(rParam.getName(), rParam.getValue());
                    }
                } else {
                    String existingValue = rParam.getValue();
                    if (customReplaceTable != null) {
                        for (String replaceName : rParam.getReplaceNames()) {
                            if (customReplaceTable.containsKey(replaceName)) {
                                existingValue = customReplaceTable.get(replaceName);
                                break;
                            }
                        }
                    }
                    for (String replaceName : rParam.getReplaceNames()) {
                        finalReplaceTable.put(replaceName, existingValue);
                    }
                }
            }
        }
        return finalReplaceTable;
    }

    protected Map<String, String> getReplaceTableUpstream(Map<String, String> customReplaceTable) {
        List<IReplaceableParam<String>> tmpParamList = new ArrayList<>();
        Map<String,String> finalMap = new HashMap<>();
        // first get group params the lower group params replace the higher group params
        for (int i = 0; i < soGroup.getElementCount(); i++) {
            tmpParamList.clear();
            ISearchObjectGroup<String> currPath = soGroup.getSubGroup(i);
            List<IReplaceableParam<?>> rpListGroup = SearchObjectController.INSTANCE.getReplaceableParamRepository().getValues(
                    new ReplaceableParamKey.RPKGroupStartsWithPredicate(currPath), (new IReplaceableParam.IRPTypePredicate(ReplaceableParamValueType.STRING)));
            for (IReplaceableParam<?> rp : rpListGroup) {
                tmpParamList.add((IReplaceableParam<String>)rp);
            }
            if (!tmpParamList.isEmpty()) {
                finalMap.putAll(getOneLevelReplaceTable(tmpParamList, customReplaceTable));
            }
        }

        /* next get parent SO params the lower SO params (in the hierarchy)
         * replace the higher SO params (in the hierarchy). For example if
         * SO1 is the parent of SO2, SO2 is the parent of SO3 and they all have
         * param AAA with the values:
         * SO1 QQQ
         * SO2 WWW
         * SO3 EEE
         * In the end the value in the final map will be EEE
         */
        Map<String,String> soParams = getParentReplaceTable(customReplaceTable);
        if ((soParams != null) && (!soParams.isEmpty())) {
            finalMap.putAll(soParams);
        }
        if ((customReplaceTable != null) && (!customReplaceTable.isEmpty())) {
            finalMap.putAll(customReplaceTable);
        }
        log.debug("getReplaceTableUpstream " + finalMap);
        return finalMap;
    }

    @Override
    public Map<String,String> getParentReplaceTable(Map<String, String> customReplaceTable) {
        Map<String,String> soParams = new HashMap<>();
        if (parent != null) {
            Map<String,String> parentParams = parent.getParentReplaceTable(customReplaceTable);
            if ((parentParams != null) && (!parentParams.isEmpty())) {
                soParams.putAll(parentParams);
            }
        }
        List<IReplaceableParam<?>> thisSOParams = keyedSO.getCloneParamList()
                .stream()
                .filter(a -> ReplaceableParamValueType.STRING.equals(a.getType()))
                .collect(Collectors.toList());
        List<IReplaceableParam<String>> tmpTable = new ArrayList<>();
        if ((thisSOParams != null) && (!thisSOParams.isEmpty())) {
            tmpTable.clear();
            for (IReplaceableParam<?> rp : thisSOParams) {
                tmpTable.add((IReplaceableParam<String>)rp);
            }
            if (!tmpTable.isEmpty()) {
                soParams.putAll(getOneLevelReplaceTable(tmpTable, customReplaceTable));
            }
        }
        return soParams;
    }

    @Override
    public Map<String, String> getFinalReplaceTable(Map<ReplaceableParamKey, IReplaceableParam<?>> staticReplaceParams, Map<String,String> dynamicReplaceParams) {
        Map<String,String> stringParams = new HashMap<>();
        if (staticReplaceParams != null) {
            for (Map.Entry<ReplaceableParamKey, IReplaceableParam<?>> currParam : staticReplaceParams.entrySet()) {
                if (ReplaceableParamValueType.STRING.equals(currParam.getValue().getType())) {
                    String paramValue = ((IReplaceableParam<String>)currParam.getValue()).getValue();
                    if ((currParam.getValue().getReplaceNames() != null) && (!currParam.getValue().getReplaceNames().isEmpty())) {
                        String existingValue = paramValue;
                        // dynamic params are used by search scripts (maybe others)
                        // they have higher priority than normal (static) params
                        if (dynamicReplaceParams != null) {
                            for (String replaceName : currParam.getValue().getReplaceNames()) {
                                if (dynamicReplaceParams.containsKey(replaceName)) {
                                    existingValue = dynamicReplaceParams.get(replaceName);
                                    break;
                                }
                            }
                        }
                        for (String rpName : currParam.getValue().getReplaceNames()) {
                            stringParams.put(rpName, existingValue);
                        }
                    } else {
                        if ((dynamicReplaceParams == null) || (!dynamicReplaceParams.containsKey(currParam.getKey().getRPName()))) {
                            stringParams.put(currParam.getKey().getRPName(), paramValue);
                        }
                    }
                }
            }
        }
        if (dynamicReplaceParams != null) {
            stringParams.putAll(dynamicReplaceParams);
        }
        return getReplaceTableUpstream(stringParams);
    }

    @Override
    public Map<ReplaceableParamKey, IReplaceableParam<?>> getParentReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams) {
        Map<ReplaceableParamKey, IReplaceableParam<?>> soParams = new HashMap<>();
        if (parent != null) {
            Map<ReplaceableParamKey, IReplaceableParam<?>> parentParams = parent.getParentReplaceParams(customReplaceParams);
            if ((parentParams != null) && (!parentParams.isEmpty())) {
                soParams.putAll(parentParams);
            }
        }
        List<IReplaceableParam<?>> thisSOParams = keyedSO.getCloneParamList();
        if ((thisSOParams != null) && (!thisSOParams.isEmpty())) {
            for (IReplaceableParam<?> rp : thisSOParams) {
                soParams.put(rp.getKey(), rp);
            }
        }
        return soParams;
    }

    @Override
    public Map<ReplaceableParamKey, IReplaceableParam<?>> getAllReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams) {
        return getReplaceParamsUpstream(customReplaceParams);
    }

    protected Map<ReplaceableParamKey, IReplaceableParam<?>> getReplaceParamsUpstream(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams) {
        Map<ReplaceableParamKey, IReplaceableParam<?>> finalMap = new HashMap<>();
        // first get group params the lower group params replace the higher group params
        for (int i = 0; i < soGroup.getElementCount(); i++) {
            ISearchObjectGroup<String> currPath = soGroup.getSubGroup(i);
            List<IReplaceableParam<?>> rpListGroup = SearchObjectController.INSTANCE.getReplaceableParamRepository().getValues(
                    new ReplaceableParamKey.RPKGroupStartsWithPredicate(currPath));
            if (!rpListGroup.isEmpty()) {
                for (IReplaceableParam<?> rp : rpListGroup) {
                    finalMap.put(rp.getKey(), rp);
                }
            }
        }

        /* next get parent SO params the lower SO params (in the hierarchy)
         * replace the higher SO params (in the hierarchy). For example if
         * SO1 is the parent of SO2, SO2 is the parent of SO3 and they all have
         * param AAA with the values:
         * SO1 QQQ
         * SO2 WWW
         * SO3 EEE
         * In the end the value in the final map will be EEE
         */
        Map<ReplaceableParamKey, IReplaceableParam<?>> soParams = getParentReplaceParams(customReplaceParams);
        if ((soParams != null) && (!soParams.isEmpty())) {
            finalMap.putAll(soParams);
        }
        if ((customReplaceParams != null) && (!customReplaceParams.isEmpty())) {
            finalMap.putAll(customReplaceParams);
        }
        log.debug("getFinalReplaceTable " + finalMap);
        return finalMap;
    }

    @Override
    public ISearchObjectGroup<String> getSearchObjectGroup() {
        return soGroup;
    }

    @Override
    public ISearchObjectGroup<String> getCloneSearchObjectGroup() {
        try {
            return soGroup.clone();
        } catch (CloneNotSupportedException e) {
            log.error("getCloneSearchObjectGroup",e);
        }
        return null;
    }

    @Override
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup) {
        soGroup = newGroup;
    }

    @Override
    public Map<String, String> getSearchObjectTags() {
        return new HashMap<>(soTags);
    }

    @Override
    public void setSearchObjectTags(Map<String, String> soTags) {
        this.soTags = soTags;
    }

    @Override
    public Set<LogFileTypeKey> getRequiredLogTypes() {
        return requiredLogFileTypes;
    }

    @Override
    public void setRequiredLogTypes(Set<LogFileTypeKey> requiredLogFileTypes) {
        this.requiredLogFileTypes = requiredLogFileTypes;
    }

    @Override
    public List<ISearchObjectDateInfo> getDateInfos() {
        return dateInfos;
    }

    @Override
    public void setDateInfos(List<ISearchObjectDateInfo> newDateInfo) {
        dateInfos = newDateInfo;
    }

    @Override
    public IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> getParent() {
        return parent;
    }

    @Override
    public void setParent(IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> newParent) {
        parent = newParent;
    }

    @Override
    public KeyedSOImpl clone() throws CloneNotSupportedException {
        KeyedSOImpl cloneObj = (KeyedSOImpl)super.clone();

        return cloneObj;
    }

}
