package org.eclipselabs.real.gui.e4swt.util;

public class FindRegexAnalysisResult {

    protected String foundStr;
    protected Integer belowObjects = 0;
    protected Integer aboveObjects = 0;
    protected Integer objectsCount = 0;
    protected Integer positionStart;
    protected Integer positionEnd;
    
    public String getFoundStr() {
        return foundStr;
    }
    public void setFoundStr(String foundStr) {
        this.foundStr = foundStr;
    }
    public Integer getBelowObjects() {
        return belowObjects;
    }
    public void setBelowObjects(Integer belowObjects) {
        this.belowObjects = belowObjects;
    }
    public void increaseBelowObjects() {
        belowObjects++;
    }
    public Integer getAboveObjects() {
        return aboveObjects;
    }
    public void setAboveObjects(Integer aboveObjects) {
        this.aboveObjects = aboveObjects;
    }
    public void increaseAboveObjects() {
        aboveObjects++;
    }
    public Integer getObjectsCount() {
        return objectsCount;
    }
    public void setObjectsCount(Integer objectsCount) {
        this.objectsCount = objectsCount;
    }
    public void increaseObjectsCount() {
        objectsCount++;
    }
    public Integer getPositionStart() {
        return positionStart;
    }
    public void setPositionStart(Integer positionStart) {
        this.positionStart = positionStart;
    }
    public Integer getPositionEnd() {
        return positionEnd;
    }
    public void setPositionEnd(Integer positionEnd) {
        this.positionEnd = positionEnd;
    }
}
