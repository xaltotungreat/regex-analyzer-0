package org.eclipselabs.real.core.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.real.core.util.FindTextResult;

public class FindStrategyManyPatternsInstance extends FindStrategyInstanceImpl {

    protected Matcher firstMatcher;
    protected List<Matcher> matchers;
    
    public FindStrategyManyPatternsInstance(List<Pattern> allPatterns, String text, Integer inst) {
        super(FindStrategyType.MANY_PATTERNS_INSTANCE, text, inst);
        if ((allPatterns != null) && (!allPatterns.isEmpty())) {
            matchers = new ArrayList<>();
            for (Pattern pt : allPatterns) {
                Matcher newMatcher = pt.matcher(text);
                matchers.add(newMatcher);
            }
            firstMatcher = matchers.get(0);
        }
    }

    @Override
    public boolean find() {
        boolean mainResult = false;
        if ((matchers != null) && (!matchers.isEmpty())) {
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
                    if (mainInstanceNumber != null) {
                        if (currentInstanceNumber == mainInstanceNumber) {
                            mainResult = true;
                            for (String str : resultText) {
                                sb.append(str);
                            }
                            currResult = new FindTextResult(sb.toString(), firstMatcher.start(), startRegion);
                            currentInstanceNumber++;
                            break;
                        } else {
                            currentInstanceNumber++;
                        }
                    } else {
                        mainResult = true;
                        for (String str : resultText) {
                            sb.append(str);
                        }
                        currResult = new FindTextResult(sb.toString(), firstMatcher.start(), startRegion);
                        currentInstanceNumber++;
                        break;
                    }
                } else if (!flagResultCanBeFound) {
                    mainResult = false;
                    break;
                }
            }
        }
        return mainResult;
    }

    @Override
    public boolean matches() {
        boolean matchVar = false;
        if (find()) {
            if ((mainInstanceNumber == null) || (mainInstanceNumber == 0)) {
                if ((currResult.getStartPos() == regionStart) && (currResult.getEndPos() == regionEnd)) {
                    matchVar = true;
                }
            }
        }
        return matchVar;
    }

    @Override
    public String toString() {
        return "FindStrategyManyPatternsInstance [firstMatcher=" + ((firstMatcher != null)?firstMatcher.pattern().pattern():"null")
                + ", mainInstanceNumber=" + mainInstanceNumber 
                + ", currentInstanceNumber=" + currentInstanceNumber 
                + ", currResult=" + currResult + "]";
    }

}
