package org.eclipselabs.real.core.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.util.FindTextResult;

public class FindStrategyPtRegions extends FindStrategyImpl {

    private static final Logger log = LogManager.getLogger(FindStrategyPtRegions.class);
    protected Matcher oneMatcher;

    protected List<RegionInfo> regions;
    protected RegionInfo currentRegion;
    protected int currentRegionIndex;

    public static class RegionInfo {
        protected int regionStart;
        protected int regionEnd;

        public RegionInfo(int start, int end) {
            regionStart = start;
            regionEnd = end;
        }

        public int getRegionStart() {
            return regionStart;
        }

        public void setRegionStart(int regionStart) {
            this.regionStart = regionStart;
        }

        public int getRegionEnd() {
            return regionEnd;
        }

        public void setRegionEnd(int regionEnd) {
            this.regionEnd = regionEnd;
        }

        @Override
        public String toString() {
            return "RegionInfo [regionStart=" + regionStart + ", regionEnd=" + regionEnd + "]";
        }
    }

    public FindStrategyPtRegions(Pattern mainPt, String text, IRealRegex regRegex,
            Map<String,String> replMap, Integer maxRegSize, Integer externalFlags) {
        super(FindStrategyType.ONE_PATTERN_REGIONS, text);
        if (mainPt != null) {
            oneMatcher = mainPt.matcher(text);
        }
        if ((regRegex != null) && (maxRegSize != null)) {
            IMatcherWrapper regionMatcher = regRegex.getMatcherWrapper(text, replMap, externalFlags);
            if (regionMatcher != null) {
                regions = new ArrayList<>();
                initRegions(regionMatcher, maxRegSize, 0, text.length());

                if ((regions != null) && (!regions.isEmpty())) {
                    Collections.sort(regions, new Comparator<RegionInfo>() {

                        @Override
                        public int compare(RegionInfo o1, RegionInfo o2) {
                            return Integer.compare(o1.getRegionStart(), o2.getRegionStart());
                        }
                    });
                    log.debug("Regions number " + regions.size());
                    currentRegion = regions.get(0);
                    currentRegionIndex = 0;
                    region(currentRegion.getRegionStart(), currentRegion.getRegionEnd());
                }
            } else {
                log.error("Unable to calculate regions");
            }
        }
    }

    protected void initRegions(IMatcherWrapper regionMt, Integer maxRegSize, int start, int end) {
        if ((end - start) > maxRegSize) {
            int middlePos = (end - start)/2 + start;
            regionMt.region(middlePos, end);
            if (regionMt.find()) {
                int currMiddlePos = regionMt.getResult().getStartPos();
                initRegions(regionMt, maxRegSize, currMiddlePos, end);
                initRegions(regionMt, maxRegSize, start, currMiddlePos);
            } else {
                regions.add(new RegionInfo(start, end));
            }
        } else {
            regions.add(new RegionInfo(start, end));
        }
    }

    @Override
    public boolean find() {
        boolean mainResult = false;
        if ((regions != null) && (!regions.isEmpty())) {
            if (currentRegion == null) {
                currentRegionIndex = 0;
                currentRegion = regions.get(currentRegionIndex);
                region(currentRegion.getRegionStart(), currentRegion.getRegionEnd());
            }
            if (oneMatcher != null) {
                //boolean flagFoundInThisRegion = false;
                while (!mainResult) {
                    mainResult = oneMatcher.find();
                    if ((!mainResult) && (currentRegionIndex < regions.size() - 1)) {
                        mainResult = false;
                        currentRegionIndex++;
                        currentRegion = regions.get(currentRegionIndex);
                        region(currentRegion.getRegionStart(), currentRegion.getRegionEnd());
                    } else {
                        break;
                    }
                }
                //mainResult = flagFoundInThisRegion;//oneMatcher.find();
                if (mainResult) {
                    currResult = new FindTextResult(oneMatcher.group(), oneMatcher.start(), oneMatcher.end());
                } else {
                    // cleaning memory
                    // the java regex engine leaves a lot of garbage
                    // after searching in large files. It will be collected some time
                    // in the future but collecting it now significantly reduces memory consumption
                    System.gc();
                }
            } else {
                log.warn("find() oneMatcher null returning false");
            }
        } else {
            if (oneMatcher != null) {
                mainResult = oneMatcher.find();
                if (mainResult) {
                    currResult = new FindTextResult(oneMatcher.group(), oneMatcher.start(), oneMatcher.end());
                }
            }
        }
        return mainResult;
    }

    @Override
    public boolean matches() {
        return false;
    }

    @Override
    public void region(int rgStart, int rgEnd) {
        super.region(rgStart, rgEnd);
        // the java regex engine leaves a lot of garbage
        // after searching in large files. It will be collected some time
        // in the future but collecting it now significantly reduced memory consumption
        System.gc();
        if (oneMatcher != null) {
            oneMatcher.region(rgStart, rgEnd);
        }
    }

}
