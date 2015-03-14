package org.eclipselabs.real.gui.e4swt.persist;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"partsInfo"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResultPartStackInfo {

    List<SearchResultPartInfo> partsInfo = new ArrayList<>();
    public SearchResultPartStackInfo() {  }
    
    public List<SearchResultPartInfo> getPartsInfo() {
        return partsInfo;
    }
    
    public void setPartsInfo(List<SearchResultPartInfo> partsInfo) {
        this.partsInfo = partsInfo;
    }

    @Override
    public String toString() {
        return "SearchResultPartStackInfo [partsInfo=" + partsInfo + "]";
    }
    

}
