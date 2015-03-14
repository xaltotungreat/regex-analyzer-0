package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

@XmlType(propOrder={"fontName","fontSize","fontStyle"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FontPersist {

    String fontName;
    Integer fontSize;
    Integer fontStyle;
    
    public FontPersist() {
    }
    
    public FontPersist(String aName, int aSize, int aStyle) {
        fontName = aName;
        fontSize = aSize;
        fontStyle = aStyle;
    }
    
    public FontPersist(Font origFont) {
        if (origFont != null) {
            FontData dt = origFont.getFontData()[0];
            fontName = dt.getName();
            fontSize = dt.getHeight();
            fontStyle = dt.getStyle();
        }
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public Integer getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(Integer fontStyle) {
        this.fontStyle = fontStyle;
    }

    @Override
    public String toString() {
        return "FontPersist [fontName=" + fontName + ", fontSize=" + fontSize
                + ", fontStyle=" + fontStyle + "]";
    }

}
