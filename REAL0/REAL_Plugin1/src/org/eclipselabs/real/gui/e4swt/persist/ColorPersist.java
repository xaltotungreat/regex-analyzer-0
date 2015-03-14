package org.eclipselabs.real.gui.e4swt.persist;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.swt.graphics.Color;

@XmlType(propOrder={"red","blue","green"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ColorPersist {

    protected Integer red;
    protected Integer blue;
    protected Integer green;
    
    public ColorPersist() {    }
    
    public ColorPersist(Color swtColor) {
        if (swtColor != null) {
            red = swtColor.getRed();
            blue = swtColor.getBlue();
            green = swtColor.getGreen();
        }
    }

    public Integer getRed() {
        return red;
    }

    public void setRed(Integer red) {
        this.red = red;
    }

    public Integer getBlue() {
        return blue;
    }

    public void setBlue(Integer blue) {
        this.blue = blue;
    }

    public Integer getGreen() {
        return green;
    }

    public void setGreen(Integer green) {
        this.green = green;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((blue == null) ? 0 : blue.hashCode());
        result = prime * result + ((green == null) ? 0 : green.hashCode());
        result = prime * result + ((red == null) ? 0 : red.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ColorPersist other = (ColorPersist) obj;
        if (blue == null) {
            if (other.blue != null)
                return false;
        } else if (!blue.equals(other.blue))
            return false;
        if (green == null) {
            if (other.green != null)
                return false;
        } else if (!green.equals(other.green))
            return false;
        if (red == null) {
            if (other.red != null)
                return false;
        } else if (!red.equals(other.red))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ColorPersist [red=" + red + ", blue=" + blue + ", green="
                + green + "]";
    }

}
