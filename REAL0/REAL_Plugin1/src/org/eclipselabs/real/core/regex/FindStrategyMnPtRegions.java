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
import org.eclipselabs.real.core.regex.FindStrategyPtRegions.RegionInfo;
import org.eclipselabs.real.core.util.FindTextResult;

public class FindStrategyMnPtRegions extends FindStrategyImpl {

    private static final Logger log = LogManager.getLogger(FindStrategyMnPtRegions.class);
    
    protected Matcher firstMatcher;
    protected List<Matcher> matchers;

    protected List<RegionInfo> regions;
    protected RegionInfo currentRegion;
    protected int currentRegionIndex;
    
    public FindStrategyMnPtRegions(List<Pattern> allPatterns, String text, IRealRegex regRegex, 
            Map<String,String> replMap, Integer maxRegSize, Integer externalFlags) {
        super(FindStrategyType.MANY_PATTERNS_REGIONS, text);
        if ((allPatterns != null) && (!allPatterns.isEmpty())) {
            matchers = new ArrayList<>();
            for (Pattern pt : allPatterns) {
                Matcher newMatcher = pt.matcher(text);
                matchers.add(newMatcher);
            }
            firstMatcher = matchers.get(0);
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
                            return Integer.valueOf(o1.getRegionStart()).compareTo(Integer.valueOf(o2.getRegionStart()));
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
        if ((matchers != null) && (!matchers.isEmpty())) {
            if ((regions != null) && (!regions.isEmpty())) {
                if (currentRegion == null) {
                    currentRegionIndex = 0;
                    currentRegion = regions.get(currentRegionIndex);
                    region(currentRegion.getRegionStart(), currentRegion.getRegionEnd());
                }
                boolean flagResultFound = false;
                boolean flagResultCanBeFound = true;
                boolean flagFoundInThisRegion = true;
                List<String> resultText = new ArrayList<>();
                int[] foundStartPos = new int[matchers.size()];
                StringBuilder sb = new StringBuilder();
                int startRegion = 0;
                int endRegion = endText;
                while(flagFoundInThisRegion) {
                    flagFoundInThisRegion = firstMatcher.find();
                    while ((!flagFoundInThisRegion) && (currentRegionIndex < regions.size() - 1)) {
                        currentRegionIndex++;
                        currentRegion = regions.get(currentRegionIndex);
                        region(currentRegion.getRegionStart(), currentRegion.getRegionEnd());
                        flagFoundInThisRegion = firstMatcher.find();
                    }
                    foundStartPos[0] = firstMatcher.start();
                    if (foundStartPos[1] > firstMatcher.end()) {
                        continue;
                    }
                    resultText.clear();
                    resultText.add(firstMatcher.group());
                    flagResultFound = true;
                    startRegion = firstMatcher.end();
                    for (int i = 1; i < matchers.size(); i++) {
                        Matcher currMatcher = matchers.get(i);
                        currMatcher.region(startRegion, endRegion);
                        if (currMatcher.find()) {
                            foundStartPos[i] = currMatcher.start();
                            if (((i + 1 < matchers.size()) && (foundStartPos[i + 1] > currMatcher.end()))
                                    || (currMatcher.start() > startRegion)) {
                                flagResultFound = false;
                                break;
                            } else {
                                startRegion = currMatcher.end();
                                resultText.add(currMatcher.group());
                            }
                        } else {
                            flagResultFound = false;
                            flagResultCanBeFound = false;
                            break;
                        }
                    }
                    if (flagResultFound) {
                        mainResult = true;
                        for (String str : resultText) {
                            sb.append(str);
                        }
                        currResult = new FindTextResult(sb.toString(), firstMatcher.start(), startRegion);
                        break;
                    } else if (!flagResultCanBeFound) {
                        mainResult = false;
                        break;
                    }
                }
            } else {
                boolean flagResultFound = false;
                boolean flagResultCanBeFound = true;
                List<String> resultText = new ArrayList<>();
                int[] foundStartPos = new int[matchers.size()];
                StringBuilder sb = new StringBuilder();
                int startRegion = 0;
                int endRegion = endText;
                while(firstMatcher.find()) {
                    foundStartPos[0] = firstMatcher.start();
                    if (foundStartPos[1] > firstMatcher.end()) {
                        continue;
                    }
                    resultText.clear();
                    resultText.add(firstMatcher.group());
                    flagResultFound = true;
                    startRegion = firstMatcher.end();
                    for (int i = 1; i < matchers.size(); i++) {
                        Matcher currMatcher = matchers.get(i);
                        currMatcher.region(startRegion, endRegion);
                        if (currMatcher.find()) {
                            foundStartPos[i] = currMatcher.start();
                            if (((i + 1 < matchers.size()) && (foundStartPos[i + 1] > currMatcher.end()))
                                    || (currMatcher.start() > startRegion)) {
                                flagResultFound = false;
                                break;
                            } else {
                                startRegion = currMatcher.end();
                                resultText.add(currMatcher.group());
                            }
                        } else {
                            flagResultFound = false;
                            flagResultCanBeFound = false;
                            break;
                        }
                    }
                    if (flagResultFound) {
                        mainResult = true;
                        for (String str : resultText) {
                            sb.append(str);
                        }
                        currResult = new FindTextResult(sb.toString(), firstMatcher.start(), startRegion);
                        break;
                    } else if (!flagResultCanBeFound) {
                        mainResult = false;
                        break;
                    }
                }
            }
        } else {
            log.warn("find() matchers null or empty returning false");
        }
        return mainResult;
    }

    @Override
    public boolean matches() {
        boolean matchVar = false;
        if (find()) {
            if ((currResult.getStartPos() == regionStart) && (currResult.getEndPos() == regionEnd)) {
                matchVar = true;
            }
        }
        return matchVar;
    }
    
    @Override
    public void region(int rgStart, int rgEnd) {
        super.region(rgStart, rgEnd);
        if ((matchers != null) && (!matchers.isEmpty())) {
            for (Matcher mt : matchers) {
                mt.region(rgStart, rgEnd);
            }
        }
    }

}
