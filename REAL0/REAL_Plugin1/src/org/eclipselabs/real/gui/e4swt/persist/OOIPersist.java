package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.real.gui.e4swt.OOIInfo;

@XmlType(propOrder={"text","style"})
@XmlAccessorType(XmlAccessType.FIELD)
public class OOIPersist {

    protected String text;
    protected TextStylePersist style;
    
    public OOIPersist() {    }
    
    public OOIPersist(OOIInfo info) {
        text = info.getTextPattern().pattern();
        style = new TextStylePersist(info.getStyle());
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

    @Override
    public String toString() {
        return "OOIPersist [text=" + text + ", style=" + style + "]";
    }

}
