package org.eclipselabs.real.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipselabs.real.core.regex.IRealRegex;
import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionType;
import org.eclipselabs.real.core.searchobject.crit.IAcceptanceCriterion;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamValueType;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.sort.IInternalSortRequest;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.eclipselabs.real.core.searchresult.sort.SortingType;

public class TestUtil {

    public static void assertSOParamNameExists(String paramName, IKeyedSearchObject<?, ?> so) {
        assertTrue(so.getParam(new ReplaceParamKey(paramName)).isPresent());
    }

    public static void assertSONotParamNameExists(String paramName, IKeyedSearchObject<?, ?> so) {
        assertFalse(so.getParam(new ReplaceParamKey(paramName)).isPresent());
    }

    public static void assertSOStrParamExists(String paramName, String paramValue, IKeyedSearchObject<?, ?> so) {
        Optional<IReplaceParam<?>> optParam = so.getParam(new ReplaceParamKey(paramName));
        assertTrue(optParam.isPresent());
        if ((optParam.isPresent()) && (ReplaceParamValueType.STRING.equals(optParam.get().getType()))) {
            IReplaceParam<String> param = (IReplaceParam<String>)optParam.get();
            assertEquals(paramValue, param.getValue());
        }
    }

    public static void assertSOStrParamNotExists(String paramName, String paramValue, IKeyedSearchObject<?, ?> so) {
        Optional<IReplaceParam<?>> optParam = so.getParam(new ReplaceParamKey(paramName));
        if ((optParam.isPresent()) && (ReplaceParamValueType.STRING.equals(optParam.get().getType()))) {
            IReplaceParam<String> param = (IReplaceParam<String>)optParam.get();
            assertNotEquals(paramValue, param.getValue());
        } else {
            assertFalse(optParam.isPresent());
        }
    }

    public static void assertSOACExists(String name, AcceptanceCriterionType type, Integer pos, IKeyedSearchObject<?, ?> so) {
        assertSOACExists(name, type, pos, null, so);
    }

    public static void assertSOACExists(String name, AcceptanceCriterionType type, Integer pos,
            Class<?> acClass, IKeyedSearchObject<?, ?> so) {
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

    public static void assertSOACNotExists(String name, AcceptanceCriterionType type, Integer pos, IKeyedSearchObject<?, ?> so) {
        assertSOACNotExists(name, type, pos, null, so);
    }

    public static void assertSOACNotExists(String name, AcceptanceCriterionType type, Integer pos,
            Class<?> acClass, IKeyedSearchObject<?, ?> so) {
        List<IAcceptanceCriterion> listAC = so.getAcceptanceList();
        if (pos != null) {
            // if the position is incorrect this AC doesn't exist
            if ((0 <= pos) && (pos < listAC.size())) {
                IAcceptanceCriterion posCrit = listAC.get(pos);
                if (posCrit.getName().equals(name) && posCrit.getType().equals(type)) {
                    if (acClass != null) {
                        assertFalse(acClass.isAssignableFrom(posCrit.getClass()));
                    } else {
                        // the name and type match the class is null
                        // the AC exists consequently assert false
                        assertFalse(true);
                    }
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

    public static void assertSOISRExists(String name, SortingType type, Integer pos, IKeyedSearchObject<?, ?> so) {
        assertSOISRExists(name, type, pos, null, so);
    }

    public static void assertSOISRExists(String name, SortingType type, Integer pos, SortingApplicability appl,
            IKeyedSearchObject<?, ?> so) {
        List<IInternalSortRequest> reqList = so.getSortRequestList();
        if (pos != null) {
            assertTrue(0 <= pos && pos < reqList.size());
            IInternalSortRequest req = reqList.get(pos);
            assertTrue(req.getName().equals(name) && req.getType().equals(type));
            if (appl != null) {
                assertEquals(appl, req.getSortApplicability());
            }
        } else {
            Optional<IInternalSortRequest> optCrit = reqList.stream().filter((tmpCrit) ->
                tmpCrit.getName().equals(name) && tmpCrit.getType().equals(type)).findAny();
            assertTrue(optCrit.isPresent());
            if (appl != null) {
                assertEquals(appl, optCrit.get().getSortApplicability());
            }
        }
    }

    public static void assertSOISRNotExists(String name, SortingType type, Integer pos, IKeyedSearchObject<?, ?> so) {
        assertSOISRNotExists(name, type, pos, null, so);
    }

    public static void assertSOISRNotExists(String name, SortingType type, Integer pos, SortingApplicability appl,
            IKeyedSearchObject<?, ?> so) {
        List<IInternalSortRequest> reqList = so.getSortRequestList();
        if (pos != null) {
            // if the position is incorrect this ISR doesn't exist
            if ((0 <= pos && pos < reqList.size())) {
                IInternalSortRequest req = reqList.get(pos);
                if (req.getName().equals(name) && req.getType().equals(type)) {
                    if (appl != null) {
                        assertNotEquals(req.getSortApplicability(), appl);
                    } else {
                        // the name and type match the applicability is null
                        // the ISR exists consequently assert false
                        assertFalse(true);
                    }
                }
            }
        } else {
            Optional<IInternalSortRequest> optCrit = reqList.stream().filter((tmpCrit) ->
                tmpCrit.getName().equals(name) && tmpCrit.getType().equals(type)).findAny();
            if (optCrit.isPresent() && (appl != null)) {
                assertNotEquals(optCrit.get().getSortApplicability(), appl);
            } else {
                assertFalse(optCrit.isPresent());
            }
        }
    }

    public static void assertSODateInfoExists(String dateFormat, IKeyedSearchObject<?, ?> so) {
        assertNotNull(so.getDateInfo());
        if (dateFormat != null) {
            assertEquals(dateFormat, so.getDateInfo().getDateFormat());
        }
    }

    public static void assertSODateInfoNotExists(String dateFormat, IKeyedSearchObject<?, ?> so) {
        if ((so.getDateInfo() != null) && (dateFormat != null)) {
            assertNotEquals(dateFormat, so.getDateInfo().getDateFormat());
        } else {
            assertNull(so.getDateInfo());
        }
    }

    public static void assertSORegexFlagExists(int flag, IKeyedSearchObject<?, ?> so) {
        assertNotNull(so.getRegexFlags());
        assertEquals(flag, so.getRegexFlags() & flag);
    }

    public static void assertSORegexFlagNotExists(int flag, IKeyedSearchObject<?, ?> so) {
        if (so.getRegexFlags() != null) {
            assertNotEquals(flag, so.getRegexFlags() & flag);
        }
    }

    public static void assertSOViewExists(String viewName, Integer pos, String regexName,
            IKeyedComplexSearchObject<?, ?, ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> so) {
        ISOComplexRegexView existingView = null;
        if (pos != null) {
            assertTrue(0 <= pos && pos < so.getViewCount());
            String viewKey = so.getViewKey(pos);
            assertEquals(viewName, viewKey);
            existingView = so.getView(pos);
            assertNotNull(existingView);
        } else {
            existingView = so.getView(viewName);
            assertNotNull(existingView);
        }
        if (regexName != null) {
            List<Object> viewObj = existingView.getViewObjects();
            Optional<Object> optRegex = viewObj.stream().
                    filter(obj -> (obj instanceof IRealRegex)).
                    filter(obj -> regexName.equals(((IRealRegex)obj).getRegexName())).findFirst();
            assertTrue(optRegex.isPresent());
            IRealRegex reg = (IRealRegex)optRegex.get();
            assertEquals(regexName, reg.getRegexName());
        }
    }

    public static void assertSOViewNotExists(String viewName, Integer pos, String regexName,
            IKeyedComplexSearchObject<?, ?, ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String> so) {
        ISOComplexRegexView existingView = null;
        if (pos != null) {
            if (0 <= pos && pos < so.getViewCount()) {
                String viewKey = so.getViewKey(pos);
                if (viewKey.equals(viewName)) {
                    existingView = so.getView(pos);
                }
            }
        } else {
            existingView = so.getView(viewName);
        }
        if ((regexName != null) && (existingView != null)) {
            List<Object> viewObj = existingView.getViewObjects();
            Optional<Object> optRegex = viewObj.stream().
                    filter(obj -> (obj instanceof IRealRegex)).
                    filter(obj -> regexName.equals(((IRealRegex)obj).getRegexName())).findFirst();
            // if not present then OK this view doesn't exist
            if (optRegex.isPresent()) {
                IRealRegex reg = (IRealRegex)optRegex.get();
                assertNotEquals(regexName, reg.getRegexName());
            }
        } else {
            assertNull(existingView);
        }
    }

    public static void assertSOMainRegexExists(String regexName, Integer pos, String patternString, ISOComplexRegex so) {
        IRealRegex foundRegex = null;
        if (pos != null) {
            assertTrue(0 <= pos && pos < so.getMainRegexList().size());
            foundRegex = so.getMainRegexList().get(pos);
            assertEquals(regexName, foundRegex.getRegexName());
        } else {
            Optional<IRealRegex> optRegex = so.getMainRegexList().stream().
                    filter(reg -> regexName.equals(reg.getRegexName())).findFirst();
            assertTrue(optRegex.isPresent());
            foundRegex = optRegex.get();
        }
        if ((patternString != null) && (foundRegex != null)) {
            String regStr = foundRegex.getPatternString(new HashMap<String,String>());
            assertEquals(patternString, regStr);
        }
    }

    public static void assertSOMainRegexNotExists(String regexName, Integer pos, String patternString, ISOComplexRegex so) {
        IRealRegex foundRegex = null;
        if (pos != null) {
            if (0 <= pos && pos < so.getMainRegexList().size() &&
                    regexName.equals(so.getMainRegexList().get(pos).getRegexName())) {
                foundRegex = so.getMainRegexList().get(pos);
            }
        } else {
            Optional<IRealRegex> optRegex = so.getMainRegexList().stream().
                    filter(reg -> regexName.equals(reg.getRegexName())).findFirst();
            if (optRegex.isPresent()) {
                foundRegex = optRegex.get();
            }
        }
        if (foundRegex != null) {
            if (patternString != null) {
                String regStr = foundRegex.getPatternString(new HashMap<String,String>());
                assertNotEquals(patternString, regStr);
            } else{
                assertFalse("Found the regex with name " + regexName, true);
            }
        }
    }

    public static void assertSOMainRegexExists(String regexName, Integer pos, List<IRealRegexParam<?>> paramList,
            Integer regexFlags, ISOComplexRegex so) {
        IRealRegex foundRegex = null;
        if (pos != null) {
            assertTrue(0 <= pos && pos < so.getMainRegexList().size());
            foundRegex = so.getMainRegexList().get(pos);
            assertEquals(regexName, foundRegex.getRegexName());
        } else {
            Optional<IRealRegex> optRegex = so.getMainRegexList().stream().
                    filter(reg -> regexName.equals(reg.getRegexName())).findFirst();
            assertTrue(optRegex.isPresent());
            foundRegex = optRegex.get();
        }
        if (paramList != null) {
            assertTrue(foundRegex.getParameters().containsAll(paramList));
        }
        if (regexFlags != null) {
            assertTrue((foundRegex.getRegexFlags() & regexFlags) == regexFlags);
        }
    }

    public static void assertSOMainRegexNotExists(String regexName, Integer pos, List<IRealRegexParam<?>> paramList,
            Integer regexFlags, ISOComplexRegex so) {
        IRealRegex foundRegex = null;
        if (pos != null) {
            if (0 <= pos && pos < so.getMainRegexList().size() &&
                    regexName.equals(so.getMainRegexList().get(pos).getRegexName())) {
                foundRegex = so.getMainRegexList().get(pos);
            }
        } else {
            Optional<IRealRegex> optRegex = so.getMainRegexList().stream().
                    filter(reg -> regexName.equals(reg.getRegexName())).findFirst();
            if (optRegex.isPresent()) {
                foundRegex = optRegex.get();
            }
        }
        if (foundRegex != null) {
            boolean match = true;
            if (paramList != null) {
                if (!foundRegex.getParameters().containsAll(paramList)) {
                    match = false;
                }
            }
            if (regexFlags != null) {
                if ((foundRegex.getRegexFlags() & regexFlags) == regexFlags) {
                    match = false;
                }
            }
            assertFalse(match);
        }
    }


}
