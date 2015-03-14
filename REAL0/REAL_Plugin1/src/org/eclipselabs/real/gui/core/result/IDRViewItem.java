package org.eclipselabs.real.gui.core.result;

import java.util.Calendar;
import java.util.List;

public interface IDRViewItem {

    public Integer getStartPos();
    public Integer getEndPos();
    public List<String> getAllViewText();
    public void addViewText(String newText);
    public Calendar getDate();
    public void setDate(Calendar cal);
}
