package org.eclipselabs.real.core.searchobject.crit;

/**
 * This enum contains the types of acceptance criteria.
 *
 * @author Vadim Korkin
 *
 */
public enum AcceptanceCriterionType {

    /**
     * This is for regex criteria. If the specified regex IS found
     * in the SRO text the criterion IS met.
     * If more than one regex is specified all regex must BE found.
     * If at least one IS NOT found the criterion IS NOT met, the SRO NOT accepted
     */
    FIND,
    /**
     * This is for regex criteria. If the specified regex IS found
     * in the SRO text the criterion IS met.
     * If more than one regex is specified all regex must BE found.
     * If at least one IS NOT found the criterion IS NOT met, the SRO NOT accepted
     *
     * This is a special case of the more general FIND. If the pattern has zero length
     * i.e. "" it is NEVER searched and this zero-length regex is satisfied. If all regexes of a criterion
     * have zero length then this criterion is met no matter what the SRO text contains.
     * If some regexes have zero length and others non-zero then the non-zero ones
     * MUST BE FOUND in the SRO text.
     */
    FIND_NULLABLE,
    /**
     * This is for regex criteria. If the SRO text IS a match for the pattern the criteria IS met.
     * If more than one regex is specified the SRO text must BE a match for all patterns.
     * If at least one IS NOT matched the criterion IS NOT met, the SRO NOT accepted
     */
    MATCH,
    /**
     * This is for regex criteria. If the specified regex IS NOT found
     * in the SRO text the criterion IS met.
     * If more than one regex is specified all regex must NOT BE found.
     * If at least one IS found the criterion IS NOT met, the SRO NOT accepted
     */
    NOT_FIND,
    /**
     * This is for regex criteria. If the specified regex IS NOT found
     * in the SRO text the criterion IS met.
     * If more than one regex is specified all regex must NOT BE found.
     * If at least one IS found the criterion IS NOT met, the SRO NOT accepted
     *
     * This is a special case of the more general NOT_FIND. If the pattern has zero length
     * i.e. "" it is NEVER searched and this zero-length regex is satisfied. If all regexes of a criterion
     * have zero length then this criterion is met no matter what the SRO text contains.
     * If some regexes have zero length and others non-zero then the non-zero ones
     * MUST NOT BE FOUND in the SRO text.
     */
    NOT_FIND_NULLABLE,
    /**
     * This is for regex criteria. If the SRO text IS a match for the pattern the criteria IS NOT met.
     * If more than one regex is specified the SRO text must NOT BE a match for all patterns.
     * If at least one IS matched the criterion IS NOT met, the SRO NOT accepted
     */
    NOT_MATCH,
    /**
     * This is for the datetime interval. The criterion of this type must specify
     * the low and high bounds to select only the log records within the bounds.
     * This interval is inclusive i.e. includes the bounds themselves.
     */
    INTERVAL,
    /**
     * This is for the datetime interval. The criterion of this type must specify
     * the low and high bounds to select only the log records within the bounds.
     * This interval is exclusive i.e. excludes the bounds themselves.
     */
    INTERVAL_EXCLUSIVE,
    /**
     * Selects only distinct records. The criterion of this type contains a number of regexes
     * to find distinct values. The patterns are all searched in the SRO text. The values
     * are stored in the map.
     * Example.
     *
     * regex: (?<=(aaa))\w{3}(?=(bb))
     *
     * LogRecord 1
     * qqq aaaWWWbb
     *
     * LogRecord 2
     * zxczx aaaRRRbb
     *
     * LogRecord 3
     * yiouio aaaWWWbb sdfsdf
     *
     * After processing the records 1 and 2 the set will contain for regex 1:
     * [WWW, RRR]
     *
     * For the 3rd record the value again WWW it already exists and the SRO is not accepted
     *
     * Even though it is possible to create a distinct criterion with more than one regex
     * it can lead to confusing results. Consider:
     *
     * regex1: (?<=(aaa))\w{3}(?=(bb))
     * regex2: \d{3}(?=(q))
     *
     * LogRecord 1
     * qqq aaaWWWbb  123q
     *
     * LogRecord 2
     * zxczx aaaRRRbb 456q
     *
     * LogRecord 3
     * yiouio aaaWWWbb sdfsdf 456q
     *
     * After processing the records 1 and 2 the map will contain:
     * regex1:
     * [WWW,RRR]
     * regex2:
     * [123,456]
     *
     * For the 3rd record
     * regex1: WWW
     * regex2: 456
     *
     * The 3rd record will not be accepted but it cannot be considered similar to 1 and 2
     * based on the values from the regexes. The bottom line is: use only one regex
     * in distinct criteria.
     *
     */
    DISTINCT;
}
