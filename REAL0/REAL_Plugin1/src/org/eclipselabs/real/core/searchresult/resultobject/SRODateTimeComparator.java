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
                //log.debug("O1date " + getDateTimeString(o1.getDate()) + " O2date " + getDateTimeString(o2.getDate()) + " result=" + result);
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

    /*private String getDateTimeString(LocalDateTime cal) {
        StringBuilder sb = new StringBuilder();
        sb.append(cal.getYear());
        sb.append("-" + cal.getMonthValue());
        sb.append("-" + cal.getDayOfMonth());

        sb.append(" " + cal.getHour());
        sb.append(":" + cal.getMinute());
        sb.append(":" + cal.getSecond());
        sb.append(":" + cal.get(ChronoField.MILLI_OF_SECOND));
        return sb.toString();
    }*/
}
