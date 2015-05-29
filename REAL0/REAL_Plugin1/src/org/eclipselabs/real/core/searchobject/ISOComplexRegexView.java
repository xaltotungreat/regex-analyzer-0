package org.eclipselabs.real.core.searchobject;

import java.util.List;

import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;

/**
 * This is the interface for the view of the complex regex.
 * To make the view more flexible it is not just a {@link IRealRegex}
 * but a list of objects. Currently the object may be either a {@link IRealRegex} or a String.
 * When calculating the result the String object is appended directly to the text while for the {@link IRealRegex}
 * object all the results are found and appended.
 * For example:
 * Text: John found 5 apples and 4 oranges
 * Objects:
 * 1. String "Apples - "
 * 2. IRealRegex "\d+(?=( apples))"
 * 3. String " Oranges - "
 * 4 IRealRegex "\d+(?=( oranges))"
 * The resulting text of this view
 * "Apples - 5 Oranges - 4"
 *
 * @author Vadim Korkin
 *
 */
public interface ISOComplexRegexView extends ISearchObject<ISRComplexRegexView,ISROComplexRegexView> {

    /**
     * Add a new object to the view
     * @param viewObject the object to add
     */
    public void add(Object viewObject);
    /**
     * Returns the list of this view's objects
     * @return the list of this view's objects
     */
    public List<Object> getViewObjects();
    /**
     * Sets the list of objects for this view
     * @param newViewObjects the new list of objects
     */
    public void setViewObjects(List<Object> newViewObjects);
}
