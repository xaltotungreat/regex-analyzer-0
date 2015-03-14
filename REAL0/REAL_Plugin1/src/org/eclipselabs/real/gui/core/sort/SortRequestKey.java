package org.eclipselabs.real.gui.core.sort;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.searchresult.sort.SortingType;

public class SortRequestKey implements Cloneable {

    private static final Logger log = LogManager.getLogger(SortRequestKey.class);
    protected SortingType sortingType;
    protected List<SortRequestKeyParam<?>> paramList;
    
    public SortRequestKey(SortingType sortType, List<SortRequestKeyParam<?>> paramLst) {
        sortingType = sortType;
        paramList = paramLst;
    }
    
    public SortRequestKey(SortRequestKey copyObj) {
        List<SortRequestKeyParam<?>> newLst = null;
        if ((copyObj.getParamList() != null) && (!copyObj.getParamList().isEmpty())) {
            newLst = new ArrayList<>();
            for (SortRequestKeyParam<?> currParam : copyObj.getParamList()) {
                try {
                    newLst.add(currParam.clone());
                } catch (CloneNotSupportedException e) {
                    log.error("Error while cloning SortRequestKey",e);
                }
            }
        }
        sortingType = copyObj.getSortingType();
        paramList = newLst;
    }
    
    public SortingType getSortingType() {
        return sortingType;
    }
    public void setSortingType(SortingType sortingType) {
        this.sortingType = sortingType;
    }
    public List<SortRequestKeyParam<?>> getParamList() {
        return paramList;
    }
    public void setParamList(List<SortRequestKeyParam<?>> paramList) {
        this.paramList = paramList;
    }
    @Override
    public SortRequestKey clone() throws CloneNotSupportedException {
        SortRequestKey newInst = (SortRequestKey)super.clone();
        List<SortRequestKeyParam<?>> newLst = null;
        if ((paramList != null) && (!paramList.isEmpty())) {
            newLst = new ArrayList<>();
            for (SortRequestKeyParam<?> currParam : paramList) {
                try {
                    newLst.add(currParam.clone());
                } catch (CloneNotSupportedException e) {
                    log.error("Error while cloning SortRequestKey",e);
                }
            }
        }
        newInst.paramList = newLst;
        return newInst;
    }

    @Override
    public String toString() {
        return "SortRequestKey [sortingType=" + sortingType + ", paramList=" + paramList + "]";
    }


}
