package org.eclipselabs.real.gui.e4swt.persist;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.gui.e4swt.NamedBookmark;

@XmlType(propOrder={"id","name","startPos","endPos","stringKeys"})
@XmlAccessorType(XmlAccessType.FIELD)
public class NamedBookmarkPersist {

    protected String id;
    protected String name;
    protected Integer startPos;
    protected Integer endPos;
    @XmlElementWrapper(nillable=true)
    protected List<String> stringKeys;
    
    public NamedBookmarkPersist() { }
    
    public NamedBookmarkPersist(NamedBookmark bkm) {
        id = bkm.getId().toString();
        name = bkm.getBookmarkName();
        startPos = bkm.getStartPos();
        endPos = bkm.getEndPos();
        if ((bkm.getStringKeys() != null) && (!bkm.getStringKeys().isEmpty())) {
            stringKeys = new ArrayList<>(bkm.getStringKeys());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<String> getStringKeys() {
        return stringKeys;
    }

    public void setStringKeys(List<String> stringKeys) {
        this.stringKeys = stringKeys;
    }

    @Override
    public String toString() {
        return "NamedBookmarkPersist [id=" + id + ", name=" + name + ", startPos=" + startPos + ", endPos=" + endPos + ", stringKeys=" + stringKeys + "]";
    }

}
