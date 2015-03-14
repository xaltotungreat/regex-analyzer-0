package org.eclipselabs.real.core.searchobject;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

public interface IKeyedSO {

    public ISearchObjectGroup<String> getSearchObjectGroup();
    public ISearchObjectGroup<String> getCloneSearchObjectGroup();
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup);
    public Map<String, String> getSearchObjectTags();
    public void setSearchObjectTags(Map<String, String> soTags);
    public List<LogFileTypeKey> getRequiredLogTypes();
    public void setRequiredLogTypes(List<LogFileTypeKey> newList);
    public LogFileTypeKey getLogFileType();
    public IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> getParent();
    public void setParent(IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> newParent);

    /**
     * This method picks up existing values for replace names
     * from the custom table. Replace tables are processed from the highest to the lowest level
     * of the hierarchy. The lower values replace higher values.
     *
     *
     * Here is an example:
     * Custom replace table
     * Param1 Value1
     * Param2 Value2
     *
     * Group 1 Replace Table
     * ParamGr11 ValueGr11 ReplaceNames: Param1, ParamGr1X
     *
     * Group 2 Replace Table
     * ParamGr21 ValueGr21 ReplaceNames: Param2, ParamGr2X
     *
     * Step1:
     * 1. For every Group 1 param the replace names (if present) are searched
     * if a replace names matches a param in the custom table. If a matching name is found
     * then the value from the custom table is used.
     * After step 1:
     * Param1  Value1
     * ParamGr1X  Value1
     *
     * 2. Step 1 is repeated with the next level in the hierarchy then the resulting values override
     * the values already present in the table:
     * After step 2:
     * Param1  Value1
     * ParamGr1X  Value1
     * Param2  Value2
     * ParamGr2X  Value2
     *
     * Then a similar procedure is repeated if this SO is part of a hierarhy of SOs. The values of params
     * in the higher levels are replaced with the values from the lower levels.
     */
    public Map<String,String> getFinalReplaceTable(Map<ReplaceParamKey, IReplaceParam<?>> customReplaceParams, Map<String,String> fineTuneReplaceTable);
    public Map<String,String> getParentReplaceTable(Map<String, String> customReplaceTable);

    public Map<ReplaceParamKey, IReplaceParam<?>> getAllReplaceParams(Map<ReplaceParamKey, IReplaceParam<?>> customReplaceParams);
    public Map<ReplaceParamKey, IReplaceParam<?>> getParentReplaceParams(Map<ReplaceParamKey, IReplaceParam<?>> customReplaceParams);

    public ISearchObjectDateInfo getDateInfo();
    public void setDateInfo(ISearchObjectDateInfo newDateInfo);
}
