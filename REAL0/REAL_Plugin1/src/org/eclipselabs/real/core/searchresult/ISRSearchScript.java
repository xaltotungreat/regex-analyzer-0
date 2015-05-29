package org.eclipselabs.real.core.searchresult;

import java.util.List;

import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISearchProgressMonitor;
import org.eclipselabs.real.core.searchobject.script.SOContainer;
import org.eclipselabs.real.core.searchobject.script.SRContainer;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

/**
 * This is the interface for the search script result.
 * The script result is different in many ways from the complex regex result.
 * The script is more versatile certain parameters may be modified during execution.
 * Therefore the script result serves as the context for the search operation.
 * It stores
 * - internal search objects that are used in the script (cloned from the SO search script)
 * - completed search results
 * - the dynamic replace table. The values in the dynamic replace table may be modified
 *   in the script. This allows for very powerful and flexible scripts.
 * It also provides the API to get an internal SOContainer inside the script.
 *
 * It is important to remember that the script must not use search objects only containers.
 * The classes {@link SOContainer} and {@link SRContainer} have the API necessary to work with
 * search objects in the script without complications like generic parameters etc.
 *
 * Most API in this interface is NOT meant to be used in the scripts. The scripts should use
 * the container API {@link SOContainer} and {@link SRContainer}
 *
 * @author Vadim Korkin
 *
 */
public interface ISRSearchScript extends IKeyedComplexSearchResult<
        IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
        ISRComplexRegexView, ISROComplexRegexView, String> {

    public ISearchProgressMonitor getProgressMonitor();
    public void setProgressMonitor(ISearchProgressMonitor newMonitor);
    /**
     * Returns the text in which the search is being performed
     * @return the text in which the search is being performed
     */
    public String getLogText();
    /**
     * Sets the text in which the search will be performed. For obvoius reasons
     * this method is not used during search.
     * @param logText the text in which the search will be performed
     */
    public void setLogText(String logText);

    /**
     * Returns the list of internal search objects. The internal search objects must be keyed search objects.
     * This is mostly internal method its use in scripts is discouraged.
     * @return the list of internal search objects
     */
    public List<IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>,
                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>>
        getMainRegexList();
    /**
     * Sets the list of internal search objects. The internal search objects must be keyed search objects.
     * This is mostly internal method its use in scripts is discouraged.
     * @param mrList the new list of internal search objects
     */
    public void setMainRegexList(List<IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                    ISRComplexRegexView, ISROComplexRegexView, String>,
                ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> mrList);
    /**
     * Returns an internal keyed search object by name. This is mostly internal method its use in scripts is discouraged.
     * The group for the internal search objects doesn't make sense and its use is implementation-specific.
     * @param complRegName the name of the internal search object
     * @return the keyed SO for this name or null if no SO for this name was found.
     */
    public IKeyedComplexSearchObject<
            ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
                ISRComplexRegexView, ISROComplexRegexView, String>,
            ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>
        getInternalSOByName(String complRegName);

    /**
     * Returns the {@link SOContainer} object for the SO by the name.
     * @param soName the name of the SO. for internal search objects the group is not identified
     * (the value in the group for internal SOs is implementation specific - may be null may be
     * the same group as the parent SO for example).
     * @return the {@link SOContainer} object for the SO by the name
     */
    public SOContainer getByName(String soName);
    /**
     * This method may be used in he scripts to reorder the views.
     * @param viewNames the new order for the views in this search result
     */
    public void setViewOrder(String...viewNames);
}
