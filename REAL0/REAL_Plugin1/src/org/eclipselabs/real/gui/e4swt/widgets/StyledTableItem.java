package org.eclipselabs.real.gui.e4swt.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.TableItem;

public class StyledTableItem extends TableItem {

    private static final Logger log = LogManager.getLogger(StyledTableItem.class);
    protected StyledTable styledParent;
    protected List<String> itemContents = new ArrayList<>();
    protected Map<Integer,TextLayout> itemLayouts = new HashMap<>();
    protected Boolean reapplyStyles = true;
    protected Integer tablePosition;
    
    public StyledTableItem(StyledTable parent, int style) {
        super (parent, style);
        styledParent = parent;
        tablePosition = styledParent.addStyledItem(this);
    }

    public StyledTableItem(StyledTable parent, int style, int index) {
        super (parent, style, index);
        styledParent = parent;
        tablePosition = styledParent.addStyledItem(this);
    }
    
    @Override
    public void setText(int index, String string) {
        if (string != null) {
            if (index >= itemContents.size()) {
                itemContents.add(string);
            } else {
                itemContents.set(index, string);
            }
            TextLayout chLayout = createNewLayout(string);
            boolean addLayout = false;
            if (reapplyStyles) {
                String chString = chLayout.getText();
                Map<String,TextStyle> allSt = styledParent.getAllStyles();
                Pattern pt;
                for (Map.Entry<String,TextStyle> stEntry : allSt.entrySet()) {
                    pt = Pattern.compile(stEntry.getKey());
                    Matcher mt = pt.matcher(chString);
                    while (mt.find()) {
                        addLayout = true;
                        chLayout.setStyle(stEntry.getValue(), mt.start(), mt.end() - 1);
                    }
                }
            }
            if (addLayout) {
                itemLayouts.put(index, chLayout);
                super.setText(index, "");
            } else {
                super.setText(index, string);
            }
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if ((itemLayouts != null) && (!itemLayouts.isEmpty())) {
            for (TextLayout currLt : itemLayouts.values()) {
                currLt.dispose();
            }
        }
    }
    
    @Override
    protected void checkSubclass() {
        // TODO Auto-generated method stub
        //super.checkSubclass();
    }
    
    public void setStyle(Pattern pt, TextStyle newStyle) {
        setStyle(pt, newStyle, true);
    }
    
    void setStyle(Pattern pt, TextStyle newStyle, boolean redrawTable) {
        if (this.isDisposed()) {
            log.error("StyledTableItem is disposed " + itemContents);
        }
        for (int i = 0; i < itemContents.size(); i++) {
            String itemStr = itemContents.get(i);
            Matcher mt = pt.matcher(itemStr);
            TextLayout currLayout = itemLayouts.get(i);
            boolean addLayout = false;
            while (mt.find()) {
                if (currLayout == null) {
                    currLayout = createNewLayout(itemStr);
                    addLayout = true;
                }
                currLayout.setStyle(newStyle, mt.start(), mt.end() - 1);
            }
            if (addLayout) {
                itemLayouts.put(i, currLayout);
                super.setText(i, "");
            }
        }
        
        if (redrawTable) {
            styledParent.redraw();
        }
    }
    
    public void setStyle(int columnNumber, TextStyle txtStyle) {
        if ((columnNumber > 0) && (columnNumber < itemContents.size()) && (txtStyle != null)) {
            TextLayout colLayout = itemLayouts.get(columnNumber);
            if (colLayout == null) {
                colLayout = createNewLayout(itemContents.get(columnNumber));
                itemLayouts.put(columnNumber, colLayout);
            }
            colLayout.setStyle(txtStyle, 0, colLayout.getText().length());
        }
    }
    
    public void setStyle(TextStyle txtStyle) {
        if (txtStyle != null) {
            for (int i = 0; i < itemContents.size(); i++) {
                TextLayout currLayout = itemLayouts.get(i);
                if (currLayout == null) {
                    currLayout = createNewLayout(itemContents.get(i)); 
                }
                currLayout.setStyle(txtStyle, 0, currLayout.getText().length());
            }
        }
    }
    
    void removeStyle(Pattern pt, boolean redrawTable) {
        Map<String,TextStyle> allSt = styledParent.getAllStyles();
        for (int i = 0; i < itemContents.size(); i++) {
            String currText = itemContents.get(i);
            TextLayout remLayout = itemLayouts.remove(i);
            if (remLayout != null) {
                remLayout.dispose();
            }
            TextLayout currLayout = null;
            Pattern ptActive;
            boolean addLayout = false;
            for (Map.Entry<String,TextStyle> stEntry : allSt.entrySet()) {
                ptActive = Pattern.compile(stEntry.getKey());
                Matcher mtActive = ptActive.matcher(currText);
                while (mtActive.find()) {
                    if (currLayout == null) {
                        currLayout = createNewLayout(itemContents.get(i));
                        addLayout = true;
                    }
                    currLayout.setStyle(stEntry.getValue(), mtActive.start(), mtActive.end() - 1);
                }
            }
            if (addLayout) {
                itemLayouts.put(i, currLayout);
                super.setText(i, "");
            } else {
                super.setText(i, currText);
            }
        }
        if (redrawTable) {
            styledParent.redraw();
        }
    }
    
    void removeAllStyles(boolean redrawTable) {
        for (int i = 0; i < itemContents.size(); i++) {
            TextLayout remLayout = itemLayouts.remove(i);
            if (remLayout != null) {
                remLayout.dispose();
            }
            super.setText(i, itemContents.get(i));
        }
        if (redrawTable) {
            styledParent.redraw();
        }
    }
    
    public TextLayout getLayout(int index) {
        return itemLayouts.get(index);
    }
    
    public TextLayout getTempLayout(int index) {
        return createNewLayout(itemContents.get(index));
    }
    
    protected TextLayout createNewLayout(String txt) {
        TextLayout newLayout = new TextLayout(getDisplay());
        if (txt != null) {
            newLayout.setText(txt);
        }
        newLayout.setFont(styledParent.getFont());
        return newLayout;
    }
    
    public Boolean getReapplyStyles() {
        return reapplyStyles;
    }

    public void setReapplyStyles(Boolean reapplyStyles) {
        this.reapplyStyles = reapplyStyles;
    }

    public Integer getTablePosition() {
        return tablePosition;
    }

    public void setTablePosition(Integer tablePosition) {
        this.tablePosition = tablePosition;
    }

    @Override
    public String toString() {
        return (itemContents != null)?itemContents.toString():"null";
    }

}
