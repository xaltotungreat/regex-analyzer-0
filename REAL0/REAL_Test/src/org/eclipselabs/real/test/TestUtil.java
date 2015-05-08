package org.eclipselabs.real.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionType;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamValueType;

public class TestUtil {

    public static void assertSOParamNameExists(String paramName, ISearchObject<?, ?> so) {
        assertTrue(so.getParam(new ReplaceParamKey(paramName)).isPresent());
    }

    public static void assertSONotParamNameExists(String paramName, ISearchObject<?, ?> so) {
        assertFalse(so.getParam(new ReplaceParamKey(paramName)).isPresent());
    }

    public static void assertSOStrParamExists(String paramName, String paramValue, ISearchObject<?, ?> so) {
        Optional<IReplaceParam<?>> optParam = so.getParam(new ReplaceParamKey(paramName));
        assertTrue(optParam.isPresent());
        if ((optParam.isPresent()) && (ReplaceParamValueType.STRING.equals(optParam.get().getType()))) {
            IReplaceParam<String> param = (IReplaceParam<String>)optParam.get();
            assertEquals(paramValue, param.getValue());
        }
    }

    public static void assertSOStrParamNotExists(String paramName, String paramValue, ISearchObject<?, ?> so) {
        Optional<IReplaceParam<?>> optParam = so.getParam(new ReplaceParamKey(paramName));
        if ((optParam.isPresent()) && (ReplaceParamValueType.STRING.equals(optParam.get().getType()))) {
            IReplaceParam<String> param = (IReplaceParam<String>)optParam.get();
            assertNotEquals(paramValue, param.getValue());
        } else {
            assertFalse(optParam.isPresent());
        }
    }

    public static void assertSOACExists(String name, AcceptanceCriterionType type, Integer pos, ISearchObject<?, ?> so) {
        assertSOACExists(name, type, pos, null, so);
    }

    public static void assertSOACExists(String name, AcceptanceCriterionType type, Integer pos,
            Class<?> acClass, ISearchObject<?, ?> so) {
        List<IAcceptanceCriterion> listAC = so.getAcceptanceList();
        if (pos != null) {
            assertTrue(0 <= pos && pos < listAC.size());
            IAcceptanceCriterion posCrit = listAC.get(pos);
            assertTrue(posCrit.getName().equals(name) && posCrit.getType().equals(type));
            if (acClass != null) {
                assertTrue(acClass.isAssignableFrom(posCrit.getClass()));
            }
        } else {
            Optional<IAcceptanceCriterion> optCrit = listAC.stream().filter((tmpCrit) ->
                tmpCrit.getName().equals(name) && tmpCrit.getType().equals(type)).findAny();
            assertTrue(optCrit.isPresent());
            if ((optCrit.isPresent()) && (acClass != null)) {
                assertTrue(acClass.isAssignableFrom(optCrit.get().getClass()));
            }
        }
    }

    public static void assertSOACNotExists(String name, AcceptanceCriterionType type, Integer pos, ISearchObject<?, ?> so) {
        assertSOACNotExists(name, type, pos, null, so);
    }

    public static void assertSOACNotExists(String name, AcceptanceCriterionType type, Integer pos,
            Class<?> acClass, ISearchObject<?, ?> so) {
        List<IAcceptanceCriterion> listAC = so.getAcceptanceList();
        if (pos != null) {
            assertTrue(0 <= pos && pos < listAC.size());
            IAcceptanceCriterion posCrit = listAC.get(pos);
            if (posCrit.getName().equals(name) && posCrit.getType().equals(type)) {
                if (acClass != null) {
                    assertFalse(acClass.isAssignableFrom(posCrit.getClass()));
                } else {
                    assertFalse(posCrit.getName().equals(name) && posCrit.getType().equals(type));
                }
            }
        } else {
            Optional<IAcceptanceCriterion> optCrit = listAC.stream().filter((tmpCrit) ->
                tmpCrit.getName().equals(name) && tmpCrit.getType().equals(type)).findAny();
            if ((optCrit.isPresent()) && (acClass != null)) {
                assertFalse(acClass.isAssignableFrom(optCrit.get().getClass()));
            } else {
                assertFalse(optCrit.isPresent());
            }
        }
    }

}
