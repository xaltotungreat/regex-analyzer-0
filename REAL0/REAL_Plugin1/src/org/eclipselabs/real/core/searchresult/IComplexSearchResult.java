package org.eclipselabs.real.core.searchresult;

import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.searchresult.resultobject.IComplexSearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;

/**
 * The complex search result has all the features of the search result plus views.
 * Views are also search results but they are the result of executing veiw search objects
 * against the main found text. Here is an example:
 * The complex search object:
 * Main regex: John found .*\.
 * View 1: "\d+(?=( apples))" (views are also special search objects)
 * View 2: "\d+(?=( oranges))"
 *
 * The search object is executed against some text.
 * In one case the following text is found:
 * "John found 5 apples and 4 oranges."
 *
 * ViewResult 1: 5
 * ViewResult 2: 4
 *
 * In another case the following text is found:
 * "John found 10 apples and 1 oranges."
 *
 * ViewResult 1: 10
 * ViewResult 2: 1
 *
 * The complex search result object contains not only the main text ("John found 5 apples and 4 oranges.")
 * but also its views. For the complex search result the SRO may be only a descendant of {@link IComplexSearchResultObject}.
 * Also IComplexSearchResult has some API to obtain view texts or the order in which the views must be presented.
 * In the above example the complex search result has the following structure:
 * SRO 1
 * Text: "John found 5 apples and 4 oranges."
 * ViewResult 1: 5
 * ViewResult 2: 4
 *
 * SRO2
 * Text: "John found 10 apples and 1 oranges."
 * ViewResult 1: 10
 * ViewResult 2: 1
 *
 * The view results are stored in the SROs not in the complex search result itself.
 *
 * @author Vadim Korkin
 *
 * @param <O> the search result object type must be a descendant of IComplexSearchResultObject
 * @param <W> the search result type of the view
 * @param <X> the search result object type of the view
 * @param <Q> the key by which the view is referenced in the SRO
 */
public interface IComplexSearchResult<O extends IComplexSearchResultObject<W,X,Q>,
        W extends ISearchResult<X>, X extends ISearchResultObject,Q> extends ISearchResult<O> {

    public List<String> getViewText(Q aViewName);
    public List<W> getView(Q aViewName);
    public List<Q> getViewKeys();
    public void setViewOrder(List<Q> aViewOrder);
    public List<Q> getViewOrder();

    public List<Map<Q, String>> getViewsText();
    public List<Map<Q,W>> getViews();

    public boolean viewsAvailable();

}
