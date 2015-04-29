package org.eclipselabs.real.core.searchresult.resultobject;

import java.time.LocalDateTime;

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
