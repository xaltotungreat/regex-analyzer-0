package org.eclipselabs.real.gui.e4swt.persist;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;

@XmlType(propOrder={"pathElements"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchObjectGroupPersist {

    List<String> pathElements = new ArrayList<>();
    public SearchObjectGroupPersist() {
    }

    public SearchObjectGroupPersist(ISearchObjectGroup<String> originalGroup) {
        if ((originalGroup != null) && (originalGroup.getElementCount() > 0)) {
            pathElements.addAll(originalGroup.getGroupElements());
        }
    }

    public List<String> getPathElements() {
        return pathElements;
    }

    public void setPathElements(List<String> pathElements) {
        this.pathElements = pathElements;
    }

    public String getString() {
        return getString(ISearchObjectGroup.DEFAULT_DELIMITER);
    }

    public String getString(String delim) {
        StringBuilder sb = new StringBuilder();
        if ((!pathElements.isEmpty()) && (delim != null)) {
            sb.append(pathElements.get(0));
            for (int i = 1; i < pathElements.size(); i++) {
                sb.append(delim + pathElements.get(i));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getString();
    }

}
