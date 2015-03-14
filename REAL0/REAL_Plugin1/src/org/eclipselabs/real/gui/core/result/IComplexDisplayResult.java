package org.eclipselabs.real.gui.core.result;

import java.util.Collection;
import java.util.List;

public interface IComplexDisplayResult extends IDisplayResult {

    public List<IDRViewItem> getViewItems();
    public void addViewItem(IDRViewItem newItem);
    public void addViewItems(Collection<IDRViewItem> newItems);
    
    public List<String> getViewNames();
    public void setViewNames(List<String> newViewNames);
    public void addViewName(String aName);
}
