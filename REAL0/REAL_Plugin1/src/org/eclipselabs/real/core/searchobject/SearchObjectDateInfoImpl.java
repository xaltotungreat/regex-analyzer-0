package org.eclipselabs.real.core.searchobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipselabs.real.core.regex.IRealRegex;

public class SearchObjectDateInfoImpl implements ISearchObjectDateInfo {

    //private static final Logger log = LogManager.getLogger(SearchObjectDateInfoImpl.class);
    protected String dateFormat;
    protected List<IRealRegex> regexList;
    protected List<Locale> possibleLocales;

    public SearchObjectDateInfoImpl() {}

    public SearchObjectDateInfoImpl(String sdf, List<IRealRegex> regList) {
        dateFormat = sdf;
        regexList = regList;
    }

    public SearchObjectDateInfoImpl(String sdf, List<IRealRegex> regList, List<String> localeLanguages) {
        this(sdf, regList);
        if (localeLanguages != null) {
            List<Locale> localeList = new ArrayList<>();
            for (String langStr : localeLanguages) {
                Locale newLoc = new Locale(langStr);
                localeList.add(newLoc);
            }
            if (!localeList.isEmpty()) {
                possibleLocales = localeList;
            }
        }
    }

    @Override
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public List<IRealRegex> getRegexList() {
        return regexList;
    }

    @Override
    public void setRegexList(List<IRealRegex> regexList) {
        this.regexList = regexList;
    }

    public List<Locale> getPossibleLocales() {
        return possibleLocales;
    }

    public void setPossibleLocales(List<Locale> possibleLocales) {
        this.possibleLocales = possibleLocales;
    }

    @Override
    public ISearchObjectDateInfo clone() throws CloneNotSupportedException {
        SearchObjectDateInfoImpl cloneObj = (SearchObjectDateInfoImpl)super.clone();
        if (regexList != null) {
            List<IRealRegex> newRegexList = new ArrayList<>();
            for (IRealRegex currReg : regexList) {
                if (currReg != null) {
                    newRegexList.add(currReg.clone());
                }
            }
            cloneObj.setRegexList(newRegexList);
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        return "SearchObjectDateInfoImpl [dateFormat=" + dateFormat + ", regexList=" + regexList + "]";
    }
}
