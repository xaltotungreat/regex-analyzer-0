package org.eclipselabs.real.gui.e4swt.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;

public class StyledTable extends Table {

    private static final Logger log = LogManager.getLogger(StyledTable.class);
    protected Map<String,TextStyle> allStyles = new HashMap<>();
    protected List<StyledTableItem> allItems = new ArrayList<>();
    protected List<Double> colWeights;
    
    
    public StyledTable(Composite parent, int style) {
        super(parent, style);
        
        addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.item instanceof StyledTableItem) {
                    StyledTableItem sti = (StyledTableItem)event.item;
                    TextLayout cellLayout = sti.getLayout(event.index);
                    if (cellLayout == null) {
                        Rectangle currRect = sti.getBounds(event.index);
                        // For drawing manually a 5 pixel indent between the border and the text is used
                        // these 5 pixels add some additional space for the cell just in case
                        event.width = currRect.width + 5;
                        event.height = currRect.height;// + 2;
                    } else {
                        Rectangle currRect = cellLayout.getBounds();
                        // For drawing manually a 5 pixel indent between the border and the text is used
                        // these 5 pixels add some additional space for the cell just in case
                        event.width = currRect.width + 5;
                        event.height = currRect.height;// + 2;
                    }
                }
            }
        });
        
        addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.item instanceof StyledTableItem) {
                    StyledTableItem sti = (StyledTableItem)event.item;
                    TextLayout cellLayout = sti.getLayout(event.index);
                    if ((event.detail & SWT.SELECTED) == SWT.SELECTED) {
                        org.eclipse.swt.graphics.Pattern bkgrGradPt = new org.eclipse.swt.graphics.Pattern(getDisplay(), 
                                event.x, event.y, (event.x), event.y + sti.getBounds().height, 
                                SWTResourceManager.getColor(216,236,213), 50, SWTResourceManager.getColor(60,66,59), 80);
                        event.gc.setBackgroundPattern(bkgrGradPt);
                        StyledTable st = (StyledTable)sti.getParent();
                        Rectangle tableBounds = st.getBounds();
                        int colWidth = getColumn(event.index).getWidth();
                        int xLeft = 0;
                        if (event.index == 0) {
                            xLeft = tableBounds.x;
                        } else {
                            xLeft = event.x;
                        }
                        event.gc.fillRectangle(new Rectangle(xLeft, event.y, 
                                colWidth, sti.getBounds().height));
                        if (cellLayout != null) {
                            // standard STW drawing uses Windows functions to draw a table
                            // windows puts some space between the border and the text
                            // approximately 5 pixels. SO when the text is drawn manually 
                            // the 5 pixel indentation should be kept
                            cellLayout.draw(event.gc, event.x + 5, event.y);
                        }
                        bkgrGradPt.dispose();
                    } else {
                        if (cellLayout != null) {
                            // standard STW drawing uses Windows functions to draw a table
                            // windows puts some space between the border and the text
                            // approximately 5 pixels. SO when the text is drawn manually 
                            // the 5 pixel indentation should be kept
                            cellLayout.draw(event.gc, event.x + 5, event.y);
                        }
                    }
                }
            }
        });
        
        addControlListener(new ControlAdapter() {
            
            @Override
            public void controlResized(ControlEvent e) {
                Table table = (Table)e.getSource();
                if ((colWeights != null) && (colWeights.size() == table.getColumnCount())) { 
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        TableColumn tc = table.getColumn(i);
                        tc.setWidth((int)(table.getClientArea().width*colWeights.get(i)));
                    }
                }
            }
        });
    }
    
    public TextStyle getDefaultStyle() {
        return new TextStyle(getFont(), getForeground(), getBackground());
    }
    
    public int addStyledItem(StyledTableItem newSti) {
        synchronized(allItems) {
            allItems.add(newSti);
        }
        return allItems.size() - 1;
    }
    
    public void removeStyledItem(StyledTableItem disposedSti) {
        synchronized(allItems) {
            allItems.remove(disposedSti);
        }
    }
    
    public List<StyledTableItem> getStyledItems() {
        return allItems;
    }
    
    

    @Override
    protected void checkSubclass() {
        // TODO Auto-generated method stub
        //super.checkSubclass();
    }

    public void setStyle(int columnNumber, TextStyle txtStyle) {
        synchronized(allItems) {
            for (StyledTableItem sti : allItems) {
                sti.setStyle(columnNumber, txtStyle);
            }
        }
    }
    
    public void setStyle(Pattern searchPt, TextStyle newStyle) {
        checkWidget ();
        boolean proceed = true;
        if ((searchPt == null) || (newStyle == null)) {
            log.error("setStyle returning txt=" + searchPt + " newStyle=" + newStyle);
            return;
        }
        if (proceed && (allStyles.containsKey(searchPt.toString())) && (allStyles.get(searchPt.toString()) != null) 
                && (allStyles.get(searchPt.toString()).equals(newStyle))) {
            proceed = false;
        }
        if (!proceed) {
            return;
        }
        
        List<StyledTableItem> itemLst = getStyledItems();
        for (StyledTableItem sti : itemLst) {
            sti.setStyle(searchPt, newStyle, false);
        }
        allStyles.put(searchPt.toString(), newStyle);
        redraw();
    }
    
    public void removeStyle(String txt, boolean isRegex) {
        checkWidget ();
        boolean proceed = true;
        if (txt == null) {
            proceed = false;
        }
        
        if (!proceed) {
            return;
        }
        
        Pattern searchPt = null;
        if (isRegex) {
            searchPt = Pattern.compile(txt);
        } else {
            searchPt = Pattern.compile(Pattern.quote(txt));
        }
        allStyles.remove(searchPt.toString());
        List<StyledTableItem> itemLst = getStyledItems();
        for (StyledTableItem sti : itemLst) {
            sti.removeStyle(searchPt, false);
        }
        redraw();
    }
    
    public void removeAllStyles() {
        List<StyledTableItem> itemLst = getStyledItems();
        for (StyledTableItem sti : itemLst) {
            sti.removeAllStyles(false);
        }
        redraw();
    }
    
    @Override
    public void clearAll() {
        super.clearAll();
        if (allItems != null) {
            synchronized(allItems) {
                allItems.clear();
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (allItems != null) {
            synchronized (allItems) {
                for (StyledTableItem currItem : allItems) {
                    currItem.dispose();
                }
                allItems.clear();
            }
        }
    }

    public Map<String, TextStyle> getAllStyles() {
        return allStyles;
    }

    public List<Double> getColWeights() {
        return colWeights;
    }

    public void setColWeights(List<Double> colWeights) {
        if ((colWeights != null) && (getColumnCount() == colWeights.size())) {
            this.colWeights = colWeights;
        } else {
            log.error("Cannot set column weights " + colWeights);
        }
    }
    
    public void setColWeights(double[] ws) {
        if ((ws != null) && (getColumnCount() == ws.length)) {
            colWeights = new ArrayList<>();
            for (double d1 : ws) {
                colWeights.add(d1);
            }
        } else {
            log.error("Cannot set column weights " + Arrays.toString(ws));
        }
    }

}
