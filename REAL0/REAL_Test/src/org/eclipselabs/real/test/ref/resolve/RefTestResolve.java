package org.eclipselabs.real.test.ref.resolve;

import java.util.HashMap;
import java.util.Map;

import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionType;
import org.eclipselabs.real.core.searchobject.crit.IDTIntervalCriterion;
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
    }

}
