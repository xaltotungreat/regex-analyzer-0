package org.eclipselabs.real.core.searchresult.resultobject;

import java.io.Serializable;
import java.util.Comparator;

public class SRODateTimeComparator<C extends ISearchResultObject> implements Comparator<C>, Serializable {

    //private static final Logger log = LogManager.getLogger(SRODateTimeComparator.class);
    /**
     * Default Serial Version UID
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ISearchResultObject o1, ISearchResultObject o2) {
        int result = 0;
        if ((o1 != null) && (o2 != null)) {
            if ((o1.getDate() != null) && (o2.getDate() != null)) {
                result = o1.getDate().compareTo(o2.getDate());
                //log.debug("O1date " + getCalendarString(o1.getDate()) + " O2date " + getCalendarString(o2.getDate()) + " result=" + result);
            } else if ((o1.getDate() == null) && (o2.getDate() != null)) {
                result = -1;
            } else if ((o1.getDate() != null) && (o2.getDate() == null)) {
                result = 1;
            }
        } else if ((o1 == null) && (o2 != null)) {
            result = -1;
        } else if ((o1 != null) && (o2 == null)) {
            result = 1;
        }
        return result;
    }

    /*private String getCalendarString(Calendar cal) {
        StringBuilder sb = new StringBuilder();
        sb.append(cal.get(Calendar.YEAR));
        sb.append("-" + cal.get(Calendar.MONTH));
        sb.append("-" + cal.get(Calendar.DAY_OF_MONTH));
        
        sb.append(" " + cal.get(Calendar.HOUR_OF_DAY));
        sb.append(":" + cal.get(Calendar.MINUTE));
        sb.append(":" + cal.get(Calendar.SECOND));
        sb.append(":" + cal.get(Calendar.MILLISECOND));
        return sb.toString();
    }*/
}
