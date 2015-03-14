package org.eclipselabs.real.core.searchresult.resultobject;

import java.util.Calendar;

public interface ISearchResultObject extends Cloneable {
    public String getText();
    public void setText(String aText);
    public void appendText(String appText);
    
    public Integer getStartPos();
    public void setStartPos(Integer aStartPos);
    public Integer getEndPos();
    public void setEndPos(Integer aEndPos);
    
    public Calendar getDate();
    public void setDate(Calendar newDate);
    
    public void clean();
    public ISearchResultObject clone() throws CloneNotSupportedException;
}
