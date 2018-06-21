package org.eclipselabs.real.core.config.spring;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.springframework.context.ApplicationContext;

public class SpringConfigReader implements IConfigReader<ApplicationContext, Integer> {

    public SpringConfigReader() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public CompletableFuture<Integer> read(ApplicationContext configRI) {
        // initialize the log types
        LogFileTypes.INSTANCE.initFromApplicationContext(configRI, (InputStream)configRI.getBean("logActivationInputStream"));

        // init the
        Map<String, ISOComplexRegex> allSOBeans = configRI.getBeansOfType(ISOComplexRegex.class);
        Map<SearchObjectKey, IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> allSo = allSOBeans.entrySet().stream().collect(
                Collectors.toMap(
                        entr -> new SearchObjectKey(entr.getValue().getSearchObjectName(),
                                entr.getValue().getSearchObjectGroup(), entr.getValue().getSearchObjectTags()),
                        entr -> entr.getValue()));
        SearchObjectController.INSTANCE.getSearchObjectRepository().addAll(allSo);

        return null;
    }

}
