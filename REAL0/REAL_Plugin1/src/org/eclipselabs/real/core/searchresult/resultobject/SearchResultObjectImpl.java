package org.eclipselabs.real.core.searchresult.resultobject;

import java.util.Calendar;

public class SearchResultObjectImpl implements ISearchResultObject {

    protected String text;
    protected Integer startPos;
    protected Integer endPos;
    protected Calendar date;
    
    public SearchResultObjectImpl(String aText, Integer aStartPos, Integer aEndPos) {
        text = aText;
        startPos = aStartPos;
        endPos = aEndPos;
    }
    
    public SearchResultObjectImpl(String aText, Integer aStartPos, Integer aEndPos, Calendar newDate) {
        text = aText;
        startPos = aStartPos;
        endPos = aEndPos;
        date = newDate;
    }

    @Override
    public void clean() {
        text = null;
        startPos = null;
        endPos = null;
        date = null;
    }
    
    @Override
    public ISearchResultObject clone() throws CloneNotSupportedException {
        SearchResultObjectImpl cloneObbj = null;
        cloneObbj = (SearchResultObjectImpl)super.clone();
        if (date != null) {
            cloneObbj.setDate((Calendar)date.clone());
        }
        return cloneObbj;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String aText) {
        text = aText;
    }
    
    @Override
    public void appendText(String appText) {
        StringBuilder sb = new StringBuilder();
        if (text != null) {
            sb.append(text);
        }
        if (appText != null) {
            sb.append(appText);
        }
        text = sb.toString();
    }

    @Override
    public Integer getStartPos() {
        return startPos;
    }

    @Override
    public void setStartPos(Integer aStartPos) {
        startPos = aStartPos;
    }

    @Override
    public Integer getEndPos() {
        return endPos;
    }

    @Override
    public void setEndPos(Integer aEndPos) {
        endPos = aEndPos;
    }

    @Override
    public String toString() {
        return "SearchResultObjectImpl [text=" + text + ", startPos=" + startPos + ", endPos=" + endPos + "]";
    }
    
    @Override
    public Calendar getDate() {
        return date;
    }

    @Override
    public void setDate(Calendar date) {
        this.date = date;
    }


}
