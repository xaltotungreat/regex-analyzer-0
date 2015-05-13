package org.eclipselabs.real.test.ref.resolve;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionType;
import org.eclipselabs.real.core.searchobject.crit.IDTIntervalCriterion;
import org.eclipselabs.real.core.searchobject.crit.IRegexAcceptanceCriterion;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.eclipselabs.real.core.searchresult.sort.SortingType;
import org.eclipselabs.real.test.ref.RefTestBase;
import org.junit.Test;

public class RefTestResolve extends RefTestBase {

    public RefTestResolve() {
        super("test_config/ref/resolve/RefTestResolve.xml");
    }

    @Test
    public void testResolveSO() {
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put("Role", "User Object");
        tagsMap.put("Interval", "TRUE");
        assertSOExists("DestSameGroupNoTags", "GlobalTest.ResolveSO1", tagsMap);
        assertSOExists("DestSameGroupLessTags", "GlobalTest.ResolveSO1", tagsMap);
        assertSOExists("DestSameGroupAllTags", "GlobalTest.ResolveSO1", tagsMap);
        assertSONotExists("DestSameGroupMoreTags", "GlobalTest.ResolveSO1", tagsMap);

        assertSOExists("DestDiffGroupNoTags", "GlobalTest.ResolveSO2", tagsMap);
        assertSOExists("DestDiffGroupLessTags", "GlobalTest.ResolveSO2", tagsMap);
        assertSOExists("DestDiffGroupAllTags", "GlobalTest.ResolveSO2", tagsMap);
        assertSONotExists("DestDiffGroupMoreTags", "GlobalTest.ResolveSO2", tagsMap);
    }

    @Test
    public void testRefParams() {
        assertSOStrParamNotExists("ParamSource3", "Dest3", "AddParam", "GlobalTest.Params1", null);
        assertSOStrParamExists("ParamSource5", "Dest5", "AddParam", "GlobalTest.Params1", null);

        assertSOStrParamExists("ParamSource3", "Dest3", "ReplaceAddParam", "GlobalTest.Params1", null);
        assertSOStrParamExists("ParamSource5", "Dest5", "ReplaceAddParam", "GlobalTest.Params1", null);

        assertSOStrParamExists("ParamSource3", "Dest3", "ReplaceParam", "GlobalTest.Params1", null);
        assertSOStrParamNotExists("ParamSource5", "Dest5", "ReplaceParam", "GlobalTest.Params1", null);

        // check that the names have been removed - use diff values for the check
        assertSOStrParamNotExists("ParamSource1", "Dest1", "RemoveParam", "GlobalTest.Params1", null);
        assertSOStrParamNotExists("ParamSource2", "Dest2", "RemoveParam", "GlobalTest.Params1", null);
        assertSOStrParamNotExists("ParamSource3", "Dest3", "RemoveParam", "GlobalTest.Params1", null);

        assertSOStrParamNotExists("ParamSource1", "Dest1", "CombinedParam", "GlobalTest.Params1", null);
        assertSOStrParamExists("Added1", "DestAdded1", "CombinedParam", "GlobalTest.Params1", null);
        assertSOStrParamExists("ParamSource2", "Dest2", "CombinedParam", "GlobalTest.Params1", null);
        assertSOStrParamExists("ReplacedAdded1", "DestReplacedAdded1", "CombinedParam", "GlobalTest.Params1", null);
        assertSOStrParamExists("ParamSource3", "Dest3", "CombinedParam", "GlobalTest.Params1", null);
        assertSOStrParamNotExists("Replaced1", "Tmp", "CombinedParam", "GlobalTest.Params1", null);
        assertSOStrParamNotExists("ParamSource4", "Tmp", "CombinedParam", "GlobalTest.Params1", null);

    }

    @Test
    public void testRefAcceptanceCriteria() {
        assertSOACExists("AC_Add", AcceptanceCriterionType.FIND, 0, IDTIntervalCriterion.class, "AddAC", "GlobalTest.Acceptances1", null);
        assertSOACNotExists("AC1", AcceptanceCriterionType.MATCH, null, IDTIntervalCriterion.class, "AddAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC10", AcceptanceCriterionType.FIND, 5, IDTIntervalCriterion.class, "AddAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC_end", AcceptanceCriterionType.FIND, 6, IDTIntervalCriterion.class, "AddAC", "GlobalTest.Acceptances1", null);

        assertSOACExists("AC_ReplaceAdd", AcceptanceCriterionType.FIND, 0, IRegexAcceptanceCriterion.class, "ReplaceAddAC", "GlobalTest.Acceptances1", null);
        assertSOACNotExists("AC1", AcceptanceCriterionType.MATCH, null, IRegexAcceptanceCriterion.class, "ReplaceAddAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC2", AcceptanceCriterionType.MATCH, 1, IDTIntervalCriterion.class, "ReplaceAddAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC10", AcceptanceCriterionType.FIND, 4, IRegexAcceptanceCriterion.class, "ReplaceAddAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC_end", AcceptanceCriterionType.FIND, 5, IRegexAcceptanceCriterion.class, "ReplaceAddAC", "GlobalTest.Acceptances1", null);

        assertSOACExists("AC_ReplaceAdd", AcceptanceCriterionType.FIND, 0, IRegexAcceptanceCriterion.class, "ReplaceAC", "GlobalTest.Acceptances1", null);
        assertSOACNotExists("AC1", AcceptanceCriterionType.MATCH, null, IRegexAcceptanceCriterion.class, "ReplaceAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC2", AcceptanceCriterionType.MATCH, 1, IDTIntervalCriterion.class, "ReplaceAC", "GlobalTest.Acceptances1", null);
        assertSOACNotExists("AC10", AcceptanceCriterionType.NOT_FIND, null, IRegexAcceptanceCriterion.class, "ReplaceAC", "GlobalTest.Acceptances1", null);
        assertSOACNotExists("AC_end", AcceptanceCriterionType.NOT_FIND, null, IRegexAcceptanceCriterion.class, "ReplaceAC", "GlobalTest.Acceptances1", null);

        assertSOACNotExists("AC1", AcceptanceCriterionType.MATCH, null, IRegexAcceptanceCriterion.class, "RemoveAC", "GlobalTest.Acceptances1", null);
        assertSOACNotExists("AC2", AcceptanceCriterionType.MATCH, null, IRegexAcceptanceCriterion.class, "RemoveAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC3", AcceptanceCriterionType.MATCH, 0, IRegexAcceptanceCriterion.class, "RemoveAC", "GlobalTest.Acceptances1", null);
        assertSOACExists("AC4", AcceptanceCriterionType.MATCH, 1, IRegexAcceptanceCriterion.class, "RemoveAC", "GlobalTest.Acceptances1", null);
    }

    @Test
    public void testSortRequests() {
        assertSOISRExists("ISR_add0", SortingType.DATE_TIME, 0, SortingApplicability.ALL, "AddISR", "GlobalTest.SortRequests1", null);
        assertSOISRNotExists("ISR2", SortingType.REGEX, 0, SortingApplicability.ALL, "AddISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR_add10", SortingType.DATE_TIME, 5, SortingApplicability.ALL, "AddISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR_addEnd", SortingType.DATE_TIME, 6, SortingApplicability.ALL, "AddISR", "GlobalTest.SortRequests1", null);

        assertSOISRExists("ISR_replaceadd0", SortingType.DATE_TIME, 0, SortingApplicability.ALL, "ReplaceAddISR", "GlobalTest.SortRequests1", null);
        assertSOISRNotExists("ISR1", SortingType.REGEX, null, SortingApplicability.MERGE_RESULTS, "ReplaceAddISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR2", SortingType.REGEX, null, SortingApplicability.ALL, "ReplaceAddISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR_add10", SortingType.DATE_TIME, 4, SortingApplicability.ALL, "ReplaceAddISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR_addEnd", SortingType.DATE_TIME, 5, SortingApplicability.ALL, "ReplaceAddISR", "GlobalTest.SortRequests1", null);

        assertSOISRExists("ISR_replace0", SortingType.DATE_TIME, 0, SortingApplicability.ALL, "ReplaceISR", "GlobalTest.SortRequests1", null);
        assertSOISRNotExists("ISR1", SortingType.REGEX, null, SortingApplicability.MERGE_RESULTS, "ReplaceISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR2", SortingType.REGEX, null, SortingApplicability.ALL, "ReplaceISR", "GlobalTest.SortRequests1", null);
        assertSOISRNotExists("ISR_add10", SortingType.DATE_TIME, null, SortingApplicability.ALL, "ReplaceISR", "GlobalTest.SortRequests1", null);
        assertSOISRNotExists("ISR_addEnd", SortingType.DATE_TIME, null, SortingApplicability.ALL, "ReplaceISR", "GlobalTest.SortRequests1", null);

        assertSOISRNotExists("ISR1", SortingType.REGEX, null, SortingApplicability.MERGE_RESULTS, "RemoveISR", "GlobalTest.SortRequests1", null);
        assertSOISRNotExists("ISR2", SortingType.REGEX, null, SortingApplicability.MERGE_RESULTS, "RemoveISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR3", SortingType.REGEX, 0, SortingApplicability.MERGE_RESULTS, "RemoveISR", "GlobalTest.SortRequests1", null);
        assertSOISRExists("ISR4", SortingType.REGEX, 1, SortingApplicability.MERGE_RESULTS, "RemoveISR", "GlobalTest.SortRequests1", null);
    }

    @Test
    public void testDateInfo() {
        assertSODateInfoNotExists("NotAdded", "AddDateInfo1", "GlobalTest.DateInfo1", null);
        assertSODateInfoExists("Added", "AddDateInfo2", "GlobalTest.DateInfo1", null);

        assertSODateInfoExists("ReplacedDF", "ReplaceAddDateInfo1", "GlobalTest.DateInfo1", null);
        assertSODateInfoExists("AddedDF", "ReplaceAddDateInfo2", "GlobalTest.DateInfo1", null);

        assertSODateInfoExists("ReplacedDF", "ReplaceDateInfo1", "GlobalTest.DateInfo1", null);
        assertSODateInfoNotExists("NotAddedDF", "ReplaceDateInfo2", "GlobalTest.DateInfo1", null);

        assertSODateInfoNotExists(null, "RemoveDateInfo1", "GlobalTest.DateInfo1", null);
        assertSODateInfoNotExists(null, "RemoveDateInfo2", "GlobalTest.DateInfo1", null);
    }

    @Test
    public void testRegexFlags() {
        assertSORegexFlagExists(Pattern.CASE_INSENSITIVE, "AddRegexFlags1", "GlobalTest.RegexFlags1", null);
        assertSORegexFlagExists(Pattern.UNICODE_CASE, "AddRegexFlags1", "GlobalTest.RegexFlags1", null);

        assertSORegexFlagExists(Pattern.DOTALL, "ReplaceAddRegexFlags1", "GlobalTest.RegexFlags1", null);
        assertSORegexFlagExists(Pattern.MULTILINE, "ReplaceAddRegexFlags1", "GlobalTest.RegexFlags1", null);

        assertSORegexFlagNotExists(Pattern.CANON_EQ, "RemoveRegexFlags1", "GlobalTest.RegexFlags1", null);
        assertSORegexFlagNotExists(Pattern.COMMENTS, "RemoveRegexFlags1", "GlobalTest.RegexFlags1", null);
    }

    @Test
    public void testViews() {
        assertSOViewExists("Add1", 0, "AddRegex1", "AddViews1", "GlobalTest.Views1", null);
        assertSOViewNotExists("View1", null, "NotAddRegex1", "AddViews1", "GlobalTest.Views1", null);

        assertSOViewExists("ReplaceAdd1", 0, "ReplaceAddRegex1", "ReplaceAddViews1", "GlobalTest.Views1", null);
        assertSOViewExists("View2", null, "ReplacedRegex1", "ReplaceAddViews1", "GlobalTest.Views1", null);
        assertSOViewExists("ReplaceAdd15", 4, "ReplaceAddRegex1", "ReplaceAddViews1", "GlobalTest.Views1", null);
        assertSOViewExists("ReplaceAdd_End", 5, "ReplaceAddRegexEnd", "ReplaceAddViews1", "GlobalTest.Views1", null);

        assertSOViewExists("Replace1", 0, "ReplaceRegex1", "ReplaceViews1", "GlobalTest.Views1", null);
        assertSOViewExists("View2", null, "ReplacedRegex1", "ReplaceViews1", "GlobalTest.Views1", null);
        assertSOViewNotExists("NotReplaced15", null, null, "ReplaceViews1", "GlobalTest.Views1", null);
        assertSOViewNotExists("NotReplaced_End", null, null, "ReplaceViews1", "GlobalTest.Views1", null);

        assertSOViewNotExists("View1", null, null, "RemoveViews1", "GlobalTest.Views1", null);
        assertSOViewNotExists("View2", null, null, "RemoveViews1", "GlobalTest.Views1", null);
        assertSOViewExists("View3", 0, "View3Regex1", "RemoveViews1", "GlobalTest.Views1", null);
        assertSOViewExists("View4", 1, "View4Regex1", "RemoveViews1", "GlobalTest.Views1", null);
    }

}
