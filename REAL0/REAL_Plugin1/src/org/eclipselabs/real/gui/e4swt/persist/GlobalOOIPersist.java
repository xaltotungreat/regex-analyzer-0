package org.eclipselabs.real.gui.e4swt.persist;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.gui.e4swt.GlobalOOIInfo;

@XmlType(propOrder={"text","style","stringKeys"})
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalOOIPersist {

    protected String text;
    protected TextStylePersist style;
    @XmlElementWrapper(nillable=true)
    protected List<String> stringKeys;
    
    public GlobalOOIPersist() {   }
    
    public GlobalOOIPersist(GlobalOOIInfo info) {
        text = info.getTextPattern().pattern();
        style = new TextStylePersist(info.getStyle());
        if (info.getStringKeys() != null) {
            stringKeys = new ArrayList<>(info.getStringKeys());
        }
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextStylePersist getStyle() {
        return style;
    }

    public void setStyle(TextStylePersist style) {
        this.style = style;
    }

    public List<String> getStringKeys() {
        return stringKeys;
    }

    public void setStringKeys(List<String> stringKeys) {
        this.stringKeys = stringKeys;
    }

    @Override
    public String toString() {
        return "GlobalOOIPersist [text=" + text + ", style=" + style
                + ", stringKeys=" + stringKeys + "]";
    }

}
