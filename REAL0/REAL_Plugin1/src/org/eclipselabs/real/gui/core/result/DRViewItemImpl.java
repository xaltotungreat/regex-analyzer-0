package org.eclipselabs.real.gui.core.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DRViewItemImpl implements IDRViewItem {

    protected List<String> textList = new ArrayList<String>();
    protected Integer startPos;
    protected Integer endPos;
    protected LocalDateTime date;

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
    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public void setDate(LocalDateTime cal) {
        date = cal;
    }

}
