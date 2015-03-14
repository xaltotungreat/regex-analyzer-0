package org.eclipselabs.real.gui.core.sotree;

import java.util.List;
import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.util.TagRef;
import org.eclipselabs.real.gui.core.sort.SortRequestKey;

public interface IDisplaySOSelector extends IDisplaySOTemplateAbstract {

    public List<IRealRegex> getNameRegexes();
    public void setNameRegexes(List<IRealRegex> nameRegexes);

    public List<IRealRegex> getGroupRegexes();
    public void setGroupRegexes(List<IRealRegex> groupRegexes);

    public List<TagRef> getTagsRegexes();
    public void setTagsRegexes(List<TagRef> tagsRegexes);
    
    public String getTextViewName();
    public void setTextViewName(String textViewName);
    
    public List<String> getViewPatterns();
    public void setViewPatterns(List<String> shortViewNames);
    public void addViewPattern(String aViewName);
    
    public List<SortRequestKey> getSortRequestKeys();
    public void setSortRequestKeys(List<SortRequestKey> reqList);
    
}
