package org.eclipselabs.real.core.searchresult.sort;

import java.util.Collections;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchresult.ISearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.resultobject.SRODateTimeComparator;

public class DateTimeSortRequest extends InternalSortRequest implements IDateTimeSortRequest {

    private static final Logger log = LogManager.getLogger(DateTimeSortRequest.class);
    
    public DateTimeSortRequest() {
        super(SortingType.DATE_TIME);
    }
    
    public DateTimeSortRequest(String aName) {
        super(SortingType.DATE_TIME, aName);
    }
    
    public DateTimeSortRequest(SortingApplicability appl, String aName) {
        super(SortingType.DATE_TIME, appl, aName);
    }
    
    public DateTimeSortRequest(DateTimeSortRequest copyObj) {
        super(SortingType.DATE_TIME, (copyObj != null)?copyObj.getSortApplicability():null, (copyObj != null)?copyObj.getName():null);
    }

    @Override
    public <R extends ISearchResult<O>, O extends ISearchResultObject> Comparator<O> getComparator(R searchRes) {
        return new SRODateTimeComparator<O>();
    }
    
    @Override
    public <R extends ISearchResult<O>, O extends ISearchResultObject> void sortResultObjects(R searchRes) {
        if (sortAvailable()) {
            if ((searchRes != null) && (searchRes.getSRObjects() != null) && (!searchRes.getSRObjects().isEmpty())){
                Collections.sort(searchRes.getSRObjects(), getComparator(searchRes));
            } else {
                log.warn("sort Null search result or no result objects");
            }
        } else {
            log.debug("sortResultObjects This internal sort request" + this + " doesn't have " 
                    + SortingApplicability.ALL + " in the scope. No sort performed");
        }
    }

    @Override
    public DateTimeSortRequest clone() throws CloneNotSupportedException {
        return (DateTimeSortRequest)super.clone();
    }

    @Override
    public String toString() {
        return "DateTimeSortRequest [name=" + name + ", type=" + type + ", sortApplicability=" + sortApplicability + "]";
    }

}
