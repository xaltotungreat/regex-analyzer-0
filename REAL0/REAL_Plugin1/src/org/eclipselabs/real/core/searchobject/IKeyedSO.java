package org.eclipselabs.real.core.searchobject;

import java.util.Map;
import java.util.Set;

import org.eclipselabs.real.core.logfile.LogFileTypeKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
/**
 * This interface implements main methods for any keyed SO.
 * This interface was created because the search object types hierarchy
 * splits into (simple) keyed search object and complex keyed search object.
 * But both types of keyed search object need to implement the same methods
 * to be part of the same configuration.
 * Mostly this interface contains getters and setters for identifiers
 * for a keyed search object in the configuration.
 *
 * @author Vadim Korkin
 *
 */
public interface IKeyedSO {

    /**
     * Returns a reference to the search object group object for this keyed SO
     * @return a reference to the search object group object for this keyed SO
     */
    public ISearchObjectGroup<String> getSearchObjectGroup();
    /**
     * Returns a clone of the search object group for this keyed SO
     * @return a clone of the search object group for this keyed SO
     */
    public ISearchObjectGroup<String> getCloneSearchObjectGroup();
    /**
     * Sets the search object group for this keyed SO
     * @param newGroup the new group for this keyed SO
     */
    public void setSearchObjectGroup(ISearchObjectGroup<String> newGroup);
    /**
     * Returns the search object tags for this keyed SO
     * @return the search object tags for this keyed SO
     */
    public Map<String, String> getSearchObjectTags();
    /**
     * Sets the search object tags for this keyed SO
     * @param soTags the new tags for this keyed SO
     */
    public void setSearchObjectTags(Map<String, String> soTags);
    /**
     * This method returns the Set of required log file types for this keyed search object.
     * The required log file types are the log file types that must be loaded
     * for this keyed SO to be executed. Most complex regexes don't need more than one
     * required log type but scripts usually need more than one.
     * The elements in the list must be different (it doesn't make sense to add the same log file type
     * two times) therefore the Set is used.
     * @return the Set of required log file types for this keyed search object
     */
    public Set<LogFileTypeKey> getRequiredLogTypes();
    /**
     * Sets the log file types for this keyed SO
     * The required log file types are the log file types that must be loaded
     * for this keyed SO to be executed. Most complex regexes don't need more than one
     * required log type but scripts usually need more than one.
     * The elements in the list must be different (it doesn't make sense to add the same log file type
     * two times) therefore the Set is used.
     * @param newList the list of log file types to set
     */
    public void setRequiredLogTypes(Set<LogFileTypeKey> newList);
    /**
     * This method returns the first available log file type from the required log file types
     * It is mostly the convenience method for the keyed SOs where it is certain
     * there is only one required log file type
     * @return the first available log file type from the required log file types
     */
    public LogFileTypeKey getLogFileType();
    /**
     * Returns the parent for this keyed SO
     * Some keyed SOs may include other keyed SOs inside themselves. Then the included ones are children
     * and the keyed SO that includes others is the parent.
     * As only keyed SOs are independent units in the configuration hierarchy it makes sense
     * to place this method in this interface.
     * @return the parent for this keyed SO
     */
    public IKeyedSearchObject<? extends IKeyedSearchResult<?>, ? extends ISearchResultObject> getParent();
    /**
     * Sets the parent for this keyed SO
     * Some keyed SOs may include other keyed SOs inside themselves. Then the included ones are children
     * and the keyed SO that includes others is the parent.
     * As only keyed SOs are independent units in the configuration hierarchy it makes sense
     * to place this method in this interface.
     * @param newParent the parent for this keyed SO
     */
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
    public Map<String,String> getFinalReplaceTable(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams, Map<String,String> fineTuneReplaceTable);
    public Map<String,String> getParentReplaceTable(Map<String, String> customReplaceTable);

    public Map<ReplaceableParamKey, IReplaceableParam<?>> getAllReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams);
    public Map<ReplaceableParamKey, IReplaceableParam<?>> getParentReplaceParams(Map<ReplaceableParamKey, IReplaceableParam<?>> customReplaceParams);

    /**
     * Returns the date info for this keyed SO.
     * A date info is the object that is used to get the date and time information
     * for the log record. It includes instructions where to find the String that contains the date and time
     * and how to parse the String into actual data
     * @return the date info for this keyed SO
     */
    public ISearchObjectDateInfo getDateInfo();
    /**
     * Sets the date info for this keyed SO
     * A date info is the object that is used to get the date and time information
     * for the log record. It includes instructions where to find the String that contains the date and time
     * and how to parse the String into actual data
     * @param newDateInfo the date info for this keyed SO
     */
    public void setDateInfo(ISearchObjectDateInfo newDateInfo);
}
