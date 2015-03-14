package org.eclipselabs.real.gui.e4swt;

import java.util.regex.Pattern;

import org.eclipse.swt.graphics.TextStyle;

public class OOIInfo {

    protected Pattern textPattern;
    protected TextStyle style;
    
    public OOIInfo(Pattern aPattern, TextStyle aStyle) {
        textPattern = aPattern;
        style = aStyle;
    }
    
    public String getDisplayString() {
        String res = null;
        if (textPattern != null) {
            if ((textPattern.pattern().startsWith("\\Q")) && (textPattern.pattern().endsWith("\\E"))) {
                res = textPattern.pattern().substring(2, textPattern.pattern().length() - 2);
            } else {
                res = textPattern.pattern();
            }
        }
        return res;
    }

    public Pattern getTextPattern() {
        return textPattern;
    }

    public void setTextPattern(Pattern textPattern) {
        this.textPattern = textPattern;
    }

    public TextStyle getStyle() {
        return style;
    }

    public void setStyle(TextStyle style) {
        this.style = style;
    }

    @Override
    public String toString() {
        return "OOIInfo [textPattern=" + textPattern + ", style=" + style + "]";
    }


}
