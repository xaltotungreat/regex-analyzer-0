package org.eclipselabs.real.core.searchobject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipselabs.real.core.exception.IncorrectPatternException;
import org.eclipselabs.real.core.regex.IRealRegex;

public interface ISearchObjectDateInfo extends Cloneable {

    public LocalDateTime parseDate(String dateStr, Map<String,String> replaceTable, Integer regexFlags) throws IncorrectPatternException;

    public String getDateFormat();
    public void setDateFormat(String dateFormat);

    public List<IRealRegex> getRegexList();
    public void setRegexList(List<IRealRegex> regexList);

    public List<Locale> getPossibleLocales();
    public void setPossibleLocales(List<Locale> possibleLocales);

    public ISearchObjectDateInfo clone() throws CloneNotSupportedException;
}
