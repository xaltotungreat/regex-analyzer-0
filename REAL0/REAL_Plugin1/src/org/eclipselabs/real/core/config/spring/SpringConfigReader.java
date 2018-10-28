package org.eclipselabs.real.core.config.spring;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.ISOSearchScript;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.ExtendedMutableTreeNode;
import org.eclipselabs.real.gui.core.GUIConfigController;
import org.eclipselabs.real.gui.core.GUIConfigKey;
import org.eclipselabs.real.gui.core.GUIConfigObjectType;
import org.springframework.context.ApplicationContext;

public class SpringConfigReader implements IConfigReader<ApplicationContext, Integer> {

    @Override
    public CompletableFuture<Integer> read(ApplicationContext configRI) {
        // initialize the log types
        LogFileTypes.INSTANCE.initFromApplicationContext(configRI, (InputStream)configRI.getBean("logActivationInputStream"));

        // init the global replace params
        Map<String, IReplaceableParam> allGPBeans = configRI.getBeansOfType(IReplaceableParam.class);
        Map<ReplaceableParamKey, IReplaceableParam<?>> allGlobalparams =
                allGPBeans.entrySet().stream().collect(Collectors.toMap(
                        e1 -> (ReplaceableParamKey)e1.getValue().getKey(),
                        Map.Entry::getValue));
        SearchObjectController.INSTANCE.getReplaceableParamRepository().addAll(allGlobalparams);

        // init the search objects
        Map<String, ISOComplexRegex> allSOCRBeans = configRI.getBeansOfType(ISOComplexRegex.class);
        Map<SearchObjectKey, IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> allSoCR = allSOCRBeans.entrySet().stream().collect(
                Collectors.toMap(
                        entr -> new SearchObjectKey(entr.getValue().getSearchObjectName(),
                                entr.getValue().getSearchObjectGroup(), entr.getValue().getSearchObjectTags()),
                        Map.Entry::getValue));
        SearchObjectController.INSTANCE.getSearchObjectRepository().addAll(allSoCR);
        Map<String, ISOSearchScript> allSOSSBeans = configRI.getBeansOfType(ISOSearchScript.class);
        Map<SearchObjectKey, IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> allSoSS = allSOSSBeans.entrySet().stream().collect(
                Collectors.toMap(
                        entr -> new SearchObjectKey(entr.getValue().getSearchObjectName(),
                                entr.getValue().getSearchObjectGroup(), entr.getValue().getSearchObjectTags()),
                        Map.Entry::getValue));
        SearchObjectController.INSTANCE.getSearchObjectRepository().addAll(allSoSS);

        // init the GUI configuration
        ExtendedMutableTreeNode soGUITree = (ExtendedMutableTreeNode) configRI.getBean("searchObjectTree");
        GUIConfigKey treeKey = new GUIConfigKey(GUIConfigObjectType.SEARCH_OBJECT_TREE);
        GUIConfigController.INSTANCE.getGUIObjectRepository().add(treeKey, soGUITree);

        GUIPropertiesStore propStore = (GUIPropertiesStore) configRI.getBean("allGUIProperties");
        Map<GUIConfigKey, Object> propsConverted = propStore.getGuiProperties().stream().collect(Collectors.toMap(
                a -> new GUIConfigKey(GUIConfigObjectType.GUI_PROPERTY, a.getName()),
                b -> b));
        GUIConfigController.INSTANCE.getGUIObjectRepository().addAll(propsConverted);

        return CompletableFuture.completedFuture(1);
    }

}
