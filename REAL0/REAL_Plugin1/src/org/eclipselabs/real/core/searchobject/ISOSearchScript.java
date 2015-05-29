package org.eclipselabs.real.core.searchobject;

import java.util.List;

import org.eclipselabs.real.core.searchobject.ref.IRefKeyedSOContainer;
import org.eclipselabs.real.core.searchresult.IKeyedComplexSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.ISRSearchScript;
import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

/**
 * The interface for the search script. Search script is a special kind of search object
 * It contains two main parameters:
 * - the script text that is executed in Groovy
 * - a List of internal search objects that are used in the script to get the final result
 * The result of the search script is practically the same as for the complex regex:
 * a set of search result objects. The difference is that the search script is more flexible,
 * it allows one to combine the results from some complex regexes into one result.
 * The search script may be executed for the current text but is makes sense only if
 * all the internal search objects use the same log type.
 * The script is executed in one thread. That is one search object at a time.
 * It means that even if the files are searched in different threads the script cannot proceed until
 * the current "execute" operation for a container is completed. Groovy is tightly integrated with Java
 * this allows the script to use variables, methods and classes of the REAL framework.
 *
 * This is an example of a search script:
 *      import org.eclipselabs.real.core.searchobject.script.SOContainer;
        import org.eclipselabs.real.core.searchobject.script.SRContainer;

        scriptResult.putToReplaceTable("LOG_TYPE","SGM_SipSp");
        SOContainer currSO = scriptResult.getByName("SGM_SipSp_Any");
        SRContainer currSR = null;
        if (!currSO.isNull()) {
            currSR = currSO.execute();
            if (!currSR.isEmpty()) {
                currSR.pushResultObjects();
            }
        }

        scriptResult.putToReplaceTable("LOG_TYPE","SGM_AmlSp");
        currSO = scriptResult.getByName("SGM_AmlSp_Any");
        if (!currSO.isNull()) {
            currSR = currSO.execute();
            if (!currSR.isEmpty()) {
                currSR.pushResultObjects();
            }
        }

        scriptResult.putToReplaceTable("LOG_TYPE","CMF_OAM");
        currSO = scriptResult.getByName("CMF_OAM_Any");
        if (!currSO.isNull()) {
            currSR = currSO.execute();
            if (!currSR.isEmpty()) {
                currSR.pushResultObjects();
            }
        }
   It is java code that is executed in Groovy. It doesn't have to be a full class. But if a class
   is used it must be imported.
 * The internal search objects are keyed search objects. They may contain their own parameters
 * but these parameters cannot be modified by the user. Only the parameters of the script itself
 * can be modified by the user before the script is executed. Therefore all the modifiable parameters
 * must be moved to the script.
 * The internal search objects must be keyed SOs to allow for refs to be used in the configuration
 * as internal SOs.
 *
 * In the script text internal search objects are obtained from the script result. It is made on purpose
 * for several reasons:
 * - to reduce complexity (use one variable scriptResult).
 * - some search objects may be modified during search therefore they were cloned. In other words
 * in the search script the original search objects are stored. When a new script result is created
 * the original internal SOs are cloned and the clones are stored in the script result.
 *
 * I is also important that any search script is at the same time a ref container. See more {@link IRefKeyedSOContainer}.
 *
 * @author Vadim Korkin
 *
 */
public interface ISOSearchScript extends IKeyedComplexSearchObject<ISRSearchScript,
        IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
        ISOComplexRegexView,ISRComplexRegexView, ISROComplexRegexView,String>, IRefKeyedSOContainer {

    /**
     * Returns the text of he script as a String
     * @return the text of he script as a String
     */
    public String getScriptText();
    /**
     * Sets the script text
     * @param scriptText the new script
     */
    public void setScriptText(String scriptText);

    /**
     * Returns the list of internal search objects. The internal search objects must be keyed search objects.
     * This is mostly internal method its use in scripts is discouraged.
     * @return the list of internal search objects
     */
    public List<IKeyedComplexSearchObject<
        ? extends IKeyedComplexSearchResult<? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
            ISRComplexRegexView, ISROComplexRegexView, String>,
        ? extends IComplexSearchResultObject<ISRComplexRegexView, ISROComplexRegexView, String>,
        ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>> getMainRegexList();
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


}
