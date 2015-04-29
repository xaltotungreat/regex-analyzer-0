package org.eclipselabs.real.gui.core.result;

import java.time.LocalDateTime;
import java.util.List;

public interface IDRViewItem {

    public Integer getStartPos();
    public Integer getEndPos();
    public List<String> getAllViewText();
    public void addViewText(String newText);
    public LocalDateTime getDate();
    public void setDate(LocalDateTime cal);
}
