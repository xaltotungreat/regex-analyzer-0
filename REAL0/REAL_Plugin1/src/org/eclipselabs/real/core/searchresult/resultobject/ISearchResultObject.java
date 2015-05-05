package org.eclipselabs.real.core.searchresult.resultobject;

import java.time.LocalDateTime;

/**
 * This is the highest level abstraction of a search result object.
 * The SRO may contain
 * - the text as String
 * - the date of this SRO. Usually the log record contains the date and time
 *      when it was written. In REAL this datetime is extracted with special
 *      regular expressions and stored in the SRO. The date is NOT mandatory fro the SRO.
 * - start position and end position - the start and end positions of the text of this SRO
 *      in the ORIGINAL searched text. For example if the original text was 'qqq www aa'
 *      and the text in this SRO is 'www' then the start position is 4 and
 *      the end position is 7.
 *
 * @author Vadim Korkin
 *
 */
public interface ISearchResultObject extends Cloneable {
    public String getText();
    public void setText(String aText);
    public void appendText(String appText);

    public Integer getStartPos();
    public void setStartPos(Integer aStartPos);
    public Integer getEndPos();
    public void setEndPos(Integer aEndPos);

    public LocalDateTime getDate();
    public void setDate(LocalDateTime newDate);

    public void clean();
    public ISearchResultObject clone() throws CloneNotSupportedException;
}
