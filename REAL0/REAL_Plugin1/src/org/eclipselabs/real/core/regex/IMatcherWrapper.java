package org.eclipselabs.real.core.regex;

import org.eclipselabs.real.core.util.FindTextResult;

/**
 * This interface is for a wrapper for the Matcher. The wrapper has some additional functions:
 * 1. The instance number can be specified.. The find method only returns true
 * if it finds a result with the correct number. The number is a regex parameter.
 *
 * 2. It allows for more than one pattern to be analyzed. The idea of the feature is
 * to allow for searching for very complex regexes. usually after some point of complexity
 * the Java regex engine simply hangs because processing of all * and + takes too much time.
 * The issue is known as catastrophic backtracking. In this case one can write several regexes that are executed
 * one after another. The principle is
 * "If n'th is found then search for (n+1)th starting from the end of the n'th".
 *
 * 3. Some wrapper strategies also allow for breaking the file into several regions
 * to reduce memory spikes at the beginning of the search. For a 50MB file used heap can reach
 * as much as 750 MB. Breaking the file into ~100 regions reduces the spikes to 500 MB,
 * ~500 regions reduced the spikes to no more than 200 MB. But there is a payoff -
 * for ~100 regions the time increases by a factor of 2, for ~500 regions by a factor of 4.
 * The max region size and the regex to break the file into regions are specified as parameters
 * to the regex
 *
 * @author Vadim Korkin
 *
 */
public interface IMatcherWrapper {

    public boolean find();
    public boolean matches();
    public void region(int rgStart, int rgEnd);
    public FindTextResult getResult();

    public FindStrategyType getType();

    /**
     * For debugging purposes
     * @return the String pattern for the first matcher of the wrapper
     */
    //public String getFirstPattern();
}
