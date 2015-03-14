package org.eclipselabs.real.core.util;

import org.eclipselabs.real.core.regex.IRealRegex;

public class TagRef {
    TagRefType type;
    IRealRegex nameRegex;
    IRealRegex valueRegex;
    
    public TagRef(TagRefType tp, IRealRegex aNameReg, IRealRegex aValueReg) {
        type = tp;
        nameRegex = aNameReg;
        valueRegex = aValueReg;
    }
    
    public TagRef(IRealRegex aNameReg, IRealRegex aValueReg) {
        this(TagRefType.MATCH, aNameReg, aValueReg);
    }

    public TagRefType getType() {
        return type;
    }

    public void setType(TagRefType type) {
        this.type = type;
    }

    public IRealRegex getNameRegex() {
        return nameRegex;
    }

    public void setNameRegex(IRealRegex nameRegex) {
        this.nameRegex = nameRegex;
    }

    public IRealRegex getValueRegex() {
        return valueRegex;
    }

    public void setValueRegex(IRealRegex valueRegex) {
        this.valueRegex = valueRegex;
    }

    @Override
    public String toString() {
        return "TagRef [type=" + type + ", nameRegex=" + nameRegex + ", valueRegex=" + valueRegex + "]";
    }
}
