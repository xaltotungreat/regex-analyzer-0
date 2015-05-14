package org.eclipselabs.real.test.ref;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.config.regex.xml.RegexXmlConfigFileReader;
import org.eclipselabs.real.core.regex.IRealRegexParam;
import org.eclipselabs.real.core.searchobject.IKeyedComplexSearchObject;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.ISOComplexRegexView;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.crit.AcceptanceCriterionType;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.ISRComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISROComplexRegexView;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.searchresult.sort.SortingApplicability;
import org.eclipselabs.real.core.searchresult.sort.SortingType;
import org.eclipselabs.real.core.util.NamedThreadFactory;
import org.eclipselabs.real.test.TestUtil;
import org.junit.After;
import org.junit.Before;

public abstract class RefTestBase {
    private static final Logger log = LogManager.getLogger(RefTestBase.class);

    protected ExecutorService fixedPool = Executors.newFixedThreadPool(3, new NamedThreadFactory("RefTestConfig"));

    protected String configName;

    public RefTestBase(String cnfName) {
        configName = cnfName;
    }

    @Before
    public void setUp() {
        IConfigReader<InputStream> regexCFReader = new RegexXmlConfigFileReader(fixedPool);
        try {
            CompletableFuture<Integer> future = regexCFReader.read(Files.newInputStream(Paths.get(configName), StandardOpenOption.READ));
            // wait until the config is read
            future.get();
        } catch (IOException e) {
            log.error("standardInit", e);
        } catch (InterruptedException e) {
            log.error("setUp",e);
        } catch (ExecutionException e) {
            log.error("setUp",e);
        }
    }

    @After
    public void tearDown() {
        SearchObjectController.INSTANCE.getSearchObjectRepository().removeAll();
    }

    public IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>
            getFirstMatchingSO(String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> result = null;
        ISearchObjectGroup<String> currGroup = SearchObjectFactory.getInstance().getSearchObjectGroup(soGroup);
        List<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> resultList = SearchObjectController.INSTANCE.getSearchObjectRepository().getValues((key) ->
            key.getSOName().equals(soName)
                && key.getSOGroup().equals(currGroup)
                && key.getSOTags().equals(tags));
        if (!resultList.isEmpty()) {
            result = resultList.get(0);
        }
        return result;
    }

    protected void assertSOExists(String soName, String soGroup, Map<String,String> tags) {
        assertNotNull(getFirstMatchingSO(soName, soGroup, tags));
    }

    protected void assertSONotExists(String soName, String soGroup, Map<String,String> tags) {
        assertNull(getFirstMatchingSO(soName, soGroup, tags));
    }

    protected void assertSOStrParamExists(String paramName, String paramValue, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOStrParamExists(paramName, paramValue, so);
    }

    protected void assertSOStrParamNotExists(String paramName, String paramValue, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOStrParamNotExists(paramName, paramValue, so);
    }

    protected void assertSOACExists(String name, AcceptanceCriterionType type, Integer pos, Class<?> acClass,
            String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOACExists(name, type, pos, acClass, so);
    }

    protected void assertSOACNotExists(String name, AcceptanceCriterionType type, Integer pos, Class<?> acClass,
            String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOACNotExists(name, type, pos, acClass, so);
    }

    protected void assertSOISRExists(String name, SortingType type, Integer pos, SortingApplicability appl,
            String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOISRExists(name, type, pos, appl, so);
    }

    protected void assertSOISRNotExists(String name, SortingType type, Integer pos, SortingApplicability appl,
            String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOISRNotExists(name, type, pos, appl, so);
    }

    public void assertSODateInfoExists(String dateFormat, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSODateInfoExists(dateFormat, so);
    }

    public void assertSODateInfoNotExists(String dateFormat, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSODateInfoNotExists(dateFormat, so);
    }

    public void assertSORegexFlagExists(int flag, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSORegexFlagExists(flag, so);
    }

    public void assertSORegexFlagNotExists(int flag, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSORegexFlagNotExists(flag, so);
    }

    public void assertSOViewExists(String viewName, Integer pos, String regexName, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOViewExists(viewName, pos, regexName,
                (IKeyedComplexSearchObject<?, ?, ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>)so);
    }

    public void assertSOViewNotExists(String viewName, Integer pos, String regexName, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOViewNotExists(viewName, pos, regexName,
                (IKeyedComplexSearchObject<?, ?, ISOComplexRegexView, ISRComplexRegexView, ISROComplexRegexView, String>)so);
    }

    public void assertSOMainRegexExists(String regexName, Integer pos, String pattern, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOMainRegexExists(regexName, pos, pattern,
                (ISOComplexRegex)so);
    }

    public void assertSOMainRegexNotExists(String regexName, Integer pos, String pattern, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOMainRegexNotExists(regexName, pos, pattern,
                (ISOComplexRegex)so);
    }

    public void assertSOMainRegexExists(String regexName, Integer pos, List<IRealRegexParam<?>> paramList,
            Integer regexFlags, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOMainRegexExists(regexName, pos, paramList, regexFlags,
                (ISOComplexRegex)so);
    }

    public void assertSOMainRegexNotExists(String regexName, Integer pos, List<IRealRegexParam<?>> paramList,
            Integer regexFlags, String soName, String soGroup, Map<String,String> tags) {
        IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject> so = getFirstMatchingSO(soName, soGroup,
                (tags != null)?tags:(new HashMap<String, String>()));
        TestUtil.assertSOMainRegexNotExists(regexName, pos, paramList, regexFlags,
                (ISOComplexRegex)so);
    }

}
