package org.eclipselabs.real.gui.e4swt.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class StyledText2 extends StyledText {

    private static final Logger log = LogManager.getLogger(StyledText2.class);
    protected Point prevComputedPoint;
    protected List<StyleRange> customSelStyleRanges = new ArrayList<>();
    protected List<StyleRange> styleRangesWithinSel = new ArrayList<>();
    protected int[] customSelRanges;
    protected int[] rangesWithinSel;
    protected Point customSelPoint;
    protected Point defaultSelPoint;
    protected volatile Boolean performedCustomSelection = false;
    protected Point selectedWithStyles = new Point(0,0);

    public StyledText2(Composite parent, int style) {
        super(parent, style);

        addCaretListener(new CaretListener() {

            @Override
            public void caretMoved(CaretEvent event) {
                // clean all custom selection variables if the caret has been moved
                if (performedCustomSelection) {
                    performedCustomSelection = false;
                    if (!styleRangesWithinSel.isEmpty()) {
                        setStyleRanges(customSelPoint.x, customSelPoint.y - customSelPoint.x, rangesWithinSel, styleRangesWithinSel.toArray(new StyleRange[]{}));
                    }
                    styleRangesWithinSel.clear();
                    customSelStyleRanges.clear();
                    rangesWithinSel = null;
                    customSelRanges = null;
                }
            }

        });

        addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                // save the simple selection point to be able to get coordinates later
                defaultSelPoint = new Point(getSelection().x, getSelection().y);
            }

        });
    }

    protected void updatePrevPoint(int newX, int newY) {
        log.debug("New PrevPoint \nwidth=" + newX + "\nheight=" + newY);
        prevComputedPoint = new Point(newX, newY);
    }

    @Override
    public void setText(String text) {
        log.debug("setText New PrevPoint \nwidth=" + getClientArea().width
                + "\nheight=" + getClientArea().height);
        prevComputedPoint = new Point(getClientArea().width, getClientArea().height);
        super.setText(text);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        if ((prevComputedPoint == null)
                || ((wHint != -1) && (prevComputedPoint.x != wHint))
                || ((hHint != -1) && (prevComputedPoint.y != hHint))) {
            log.debug("ComputingSize old Point x=" + ((prevComputedPoint != null)?(prevComputedPoint.x):"null")
                    + " height=" + ((prevComputedPoint != null)?(prevComputedPoint.y):"null"));
            log.debug("ComputingSize new client wHint=" + wHint + " hHint=" + hHint);
            prevComputedPoint = new Point(wHint, hHint);
            super.computeSize(wHint, hHint, changed);
        } else {
            //log.debug("NOT ComputingSize old Point x=" + prevComputedPoint.x + " height=" + prevComputedPoint.y);
            //log.debug("NOT ComputingSize new wHint=" + wHint + " hHint=" + hHint);
        }
        return new Point(prevComputedPoint.x, prevComputedPoint.y);
    }

    public Point getSelectionWithStyleRanges() {
        Point res = null;
        if (defaultSelPoint != null) {
            res = new Point(defaultSelPoint.x, defaultSelPoint.y);
        } else {
            res = new Point(selectedWithStyles.x, selectedWithStyles.y);
        }
        return res;
    }

    public void setSelectionWithStyleRanges(int start, int end) {
        // reset the styles for the old selection
        if (!styleRangesWithinSel.isEmpty()) {
            setStyleRanges(customSelPoint.x, customSelPoint.y - customSelPoint.x, rangesWithinSel, styleRangesWithinSel.toArray(new StyleRange[]{}));
        }
        customSelPoint = new Point(start, end);
        styleRangesWithinSel.clear();
        customSelStyleRanges.clear();
        rangesWithinSel = null;
        customSelRanges = null;
        defaultSelPoint = null;
        StyleRange[] allStyleRangesWithinSel = getStyleRanges(start, end - start);
        if ((allStyleRangesWithinSel != null) && (allStyleRangesWithinSel.length > 0)) {
            rangesWithinSel = new int[allStyleRangesWithinSel.length*2];
            int i = 0;
            List<Point> styleRanges = new ArrayList<>();
            for (StyleRange sr : allStyleRangesWithinSel) {
                styleRanges.add(new Point(sr.start, sr.length));
                rangesWithinSel[i] = sr.start;
                rangesWithinSel[i + 1] = sr.length;
                i += 2;
                styleRangesWithinSel.add(sr);
            }

            List<Point> selRanges = new ArrayList<>();
            int currentPos = start;
            if (rangesWithinSel[0] > start) {
                selRanges.add(new Point(start, rangesWithinSel[0] - start));
                currentPos = rangesWithinSel[0] + rangesWithinSel[1];
            }
            i = 0;
            while ((currentPos < end) && (i < rangesWithinSel.length - 3)) {
                if ((rangesWithinSel[i] + rangesWithinSel[i + 1]) < rangesWithinSel[i + 2]) {
                    selRanges.add(new Point(rangesWithinSel[i] + rangesWithinSel[i + 1],
                            rangesWithinSel[i + 2] - (rangesWithinSel[i] + rangesWithinSel[i + 1])));
                }
                currentPos = rangesWithinSel[i] + rangesWithinSel[i + 1];
                i += 2;
            }

            if (rangesWithinSel[rangesWithinSel.length - 2] + rangesWithinSel[rangesWithinSel.length - 1] < end) {
                selRanges.add(new Point(rangesWithinSel[rangesWithinSel.length - 2] + rangesWithinSel[rangesWithinSel.length - 1],
                        end - (rangesWithinSel[rangesWithinSel.length - 2] + rangesWithinSel[rangesWithinSel.length - 1])));
            }

            if (!selRanges.isEmpty()) {
                customSelRanges = new int[selRanges.size()*2];
                i = 0;
                for (Point selRange : selRanges) {
                    customSelRanges[i] = selRange.x;
                    customSelRanges[i + 1] = selRange.y;
                    customSelStyleRanges.add(new StyleRange(selRange.x, selRange.y,
                            getSelectionForeground(), getSelectionBackground()));
                    i += 2;
                }
            }

            List<Point> allStyleRangesPoint = new ArrayList<>();
            allStyleRangesPoint.addAll(styleRanges);
            allStyleRangesPoint.addAll(selRanges);
            Collections.sort(allStyleRangesPoint, new Comparator<Point>() {

                @Override
                public int compare(Point o1, Point o2) {
                    if ((o1 != null) && (o2 != null)) {
                        Integer o1Start = o1.x;
                        Integer o2Start = o2.x;
                        return o1Start.compareTo(o2Start);
                    }
                    return 0;
                }

            });
            int[] allRanges = new int[allStyleRangesPoint.size()*2];
            i = 0;
            for (Point currPoint : allStyleRangesPoint) {
                allRanges[i] = currPoint.x;
                allRanges[i + 1] = currPoint.y;
                i += 2;
            }

            List<StyleRange> allStyleRanges = new ArrayList<>();
            allStyleRanges.addAll(styleRangesWithinSel);
            allStyleRanges.addAll(customSelStyleRanges);
            Collections.sort(allStyleRanges, new Comparator<StyleRange>() {

                @Override
                public int compare(StyleRange o1, StyleRange o2) {
                    if ((o1 != null) && (o2 != null)) {
                        Integer o1Start = o1.start;
                        Integer o2Start = o2.start;
                        return o1Start.compareTo(o2Start);
                    }
                    return 0;
                }
            });

            setStyleRanges(customSelPoint.x, customSelPoint.y - customSelPoint.x, allRanges, allStyleRanges.toArray(new StyleRange[]{}));
            performedCustomSelection = true;
            setSelection(start);
        } else {
            super.setSelection(start, end);
        }
        selectedWithStyles = new Point(start, end);
    }


    @Override
    public void setFont(Font font) {
        if (!getFont().equals(font)) {
            super.setFont(font);
        } else {
            //log.info("Setting the same font ignoring no redraw");
        }
    }



}
