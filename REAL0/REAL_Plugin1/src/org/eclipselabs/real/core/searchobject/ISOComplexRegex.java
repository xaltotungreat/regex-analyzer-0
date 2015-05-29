package org.eclipselabs.real.core.searchobject;

import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISRComplexRegex;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegex;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

/**
 * The main interface for the complex regex. The complex regex contains one or more {@link IRealRegex} objects.
 * During search complex regex first finds all the values for the first {@link IRealRegex} verifies
 * the found value passes all acceptance criteria and adds the corresponding search result objects.
 * Then the procedure is repeated for the second etc.
 * Exact sequence of operations is implementation-specific
 *
 * @author Vadim Korkin
 *
 */
public interface ISOComplexRegex extends IKeyedComplexSearchObject<ISRComplexRegex, ISROComplexRegex,
        ISOComplexRegexView,ISRComplexRegexView, ISROComplexRegexView,String> {
    /**
     * Returns the List of {@link IRealRegex} objects for this complex regex
     * @return
     */
    public List<IRealRegex> getMainRegexList();
    /**
     * Sets the List of main regexes
     * @param mrList
     */
    public void setMainRegexList(List<IRealRegex> mrList);

}
