package org.eclipselabs.real.gui.e4swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.TextStyle;

public class GlobalOOIInfo extends OOIInfo {

    protected List<String> stringKeys = Collections.synchronizedList(new ArrayList<String>());
    
    public GlobalOOIInfo(Pattern pt, TextStyle txtStyle) {
        super(pt,txtStyle);
    }

    public void addStringKey(String aKey) {
        stringKeys.add(aKey);
    }

    public void removeStringKey(String aKey) {
        stringKeys.remove(aKey);
    }

    public void removeStringKey(int index) {
        stringKeys.remove(index);
    }

    public List<String> getStringKeys() {
        return stringKeys;
    }

    public void setStringKeys(List<String> stringKeys) {
        this.stringKeys = stringKeys;
    }

    @Override
    public String toString() {
        return "GlobalOOIInfo {textPattern=" + textPattern + ", style=" + style + ", stringKeys=" + stringKeys + "]";
    }

}
