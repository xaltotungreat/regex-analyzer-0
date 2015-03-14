package org.eclipselabs.real.gui.e4swt.persist;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"savedTime","srPartStackInfo","globalOOIList"})
public class SavedWorkspace {

    protected Date savedTime;
    protected SearchResultPartStackInfo srPartStackInfo;
    protected List<GlobalOOIPersist> globalOOIList;
    public SavedWorkspace() {  }

    public SavedWorkspace(SearchResultPartStackInfo psInf) {
        srPartStackInfo = psInf;
    }

    public Date getSavedTime() {
        return savedTime;
    }

    public void setSavedTime(Date savedTime) {
        this.savedTime = savedTime;
    }

    public SearchResultPartStackInfo getSrPartStackInfo() {
        return srPartStackInfo;
    }

    public void setSrPartStackInfo(SearchResultPartStackInfo srPartStackInfo) {
        this.srPartStackInfo = srPartStackInfo;
    }

    public List<GlobalOOIPersist> getGlobalOOIList() {
        return globalOOIList;
    }

    public void setGlobalOOIList(List<GlobalOOIPersist> globalOOIList) {
        this.globalOOIList = globalOOIList;
    }

    @Override
    public String toString() {
        return "SavedWorkspace [srPartStackInfo=" + srPartStackInfo
                + ", globalOOIList=" + globalOOIList + "]";
    }


}
