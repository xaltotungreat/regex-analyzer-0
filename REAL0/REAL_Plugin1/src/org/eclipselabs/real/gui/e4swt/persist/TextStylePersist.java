package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.swt.graphics.TextStyle;

@XmlType(propOrder={"font","foreground","background"})
@XmlAccessorType(XmlAccessType.FIELD)
public class TextStylePersist {

    protected FontPersist font;
    protected ColorPersist foreground;
    protected ColorPersist background;
    public TextStylePersist() {
    }
    
    public TextStylePersist(FontPersist fnt, ColorPersist frgr, ColorPersist bkgr) {
        font = fnt;
        foreground = frgr;
        background = bkgr;
    }
    
    public TextStylePersist(TextStyle origStyle) {
        if (origStyle.font != null) {
            font = new FontPersist(origStyle.font);
        }
        if (origStyle.foreground != null) {
            foreground = new ColorPersist(origStyle.foreground);
        }
        if (origStyle.background != null) {
            background = new ColorPersist(origStyle.background);
        }
    }
    
    public FontPersist getFont() {
        return font;
    }
    public void setFont(FontPersist font) {
        this.font = font;
    }
    public ColorPersist getForeground() {
        return foreground;
    }
    public void setForeground(ColorPersist foreground) {
        this.foreground = foreground;
    }
    public ColorPersist getBackground() {
        return background;
    }
    public void setBackground(ColorPersist background) {
        this.background = background;
    }

    @Override
    public String toString() {
        return "TextStylePersist [font=" + font + ", foreground=" + foreground
                + ", background=" + background + "]";
    }

}
