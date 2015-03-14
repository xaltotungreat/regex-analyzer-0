package org.eclipselabs.real.gui.core.result;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DRViewItemImpl implements IDRViewItem {

    protected List<String> textList = new ArrayList<String>();
    protected Integer startPos;
    protected Integer endPos;
    protected Calendar date;

    public DRViewItemImpl(Integer aStartPos, Integer aEndPos) {
        startPos = aStartPos;
        endPos = aEndPos;
    }

    @Override
    public Integer getStartPos() {
        return startPos;
    }

    @Override
    public Integer getEndPos() {
        return endPos;
    }

    @Override
    public List<String> getAllViewText() {
        return textList;
    }

    @Override
    public void addViewText(String newText) {
        textList.add(newText);
    }

    @Override
    public Calendar getDate() {
        return date;
    }

    @Override
    public void setDate(Calendar cal) {
        date = cal;
    }

}
