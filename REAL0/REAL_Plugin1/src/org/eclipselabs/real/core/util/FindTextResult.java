package org.eclipselabs.real.core.util;

public class FindTextResult {

    protected String strResult;
    protected Integer startPos;
    protected Integer endPos;
    
    public FindTextResult(String res, Integer stPos, Integer edPos) {
        strResult = res;
        startPos = stPos;
        endPos = edPos;
    }

    public String getStrResult() {
        return strResult;
    }

    public void setStrResult(String strResult) {
        this.strResult = strResult;
    }

    public Integer getStartPos() {
        return startPos;
    }

    public void setStartPos(Integer startPos) {
        this.startPos = startPos;
    }

    public Integer getEndPos() {
        return endPos;
    }

    public void setEndPos(Integer endPos) {
        this.endPos = endPos;
    }

}
