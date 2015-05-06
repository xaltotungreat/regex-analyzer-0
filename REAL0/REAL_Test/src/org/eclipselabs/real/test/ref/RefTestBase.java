package org.eclipselabs.real.test.ref;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObject;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.NamedThreadFactory;
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

    protected void assertSOParamExists(String paramName, ISearchObject<?, ?> so) {
        assertTrue(so.getParam(new ReplaceParamKey(paramName)).isPresent());
    }

    protected void assertSOExists(String soName, String soGroup, Map<String,String> tags) {
        ISearchObjectGroup<String> currGroup = SearchObjectFactory.getInstance().getSearchObjectGroup(soGroup);
        List<IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> result = SearchObjectController.INSTANCE.getSearchObjectRepository().getValues((key) ->
            key.getSOName().equals(soName)
                && key.getSOGroup().equals(currGroup)
                && key.getSOTags().equals(tags));
        assertFalse(result.isEmpty());
    }

}
