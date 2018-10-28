package org.eclipselabs.real.core.config.ml.regex.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.ml.regex.RegexConfigReaderImpl;
import org.eclipselabs.real.core.config.ml.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.ml.regex.xml.constructor.RegexXmlConstructorFactoryImpl;
import org.eclipselabs.real.core.config.ml.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.ml.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.ml.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.ml.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISORegex;
import org.eclipselabs.real.core.searchobject.ISOSearchScript;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.ref.IRefKeyedSOContainer;
import org.eclipselabs.real.core.searchobject.ref.RefKeyedSO;
import org.eclipselabs.real.core.searchresult.IKeyedSearchResult;
import org.eclipselabs.real.core.searchresult.resultobject.ISearchResultObject;
import org.eclipselabs.real.core.util.ITaskWatcherCallback;
import org.eclipselabs.real.core.util.TaskWatcher;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class RegexXmlConfigFileReader extends RegexConfigReaderImpl<InputStream> {
    private static final Logger log = LogManager.getLogger(RegexXmlConfigFileReader.class);

    protected IRegexConstructorFactory<Node> constructionFactory = RegexXmlConstructorFactoryImpl.getInstance();

    protected Long readXmlFileTimeout = DEFAULT_XML_READ_FILE_TIMEOUT;
    protected TimeUnit readXmlFileTimeUnit = DEFAULT_XML_READ_FILE_TIMEUNIT;
    protected Long repositoryLockTimeout = DEFAULT_REPOSITORY_LOCK_TIMEOUT;
    protected TimeUnit repositoryLockTimeUnit = DEFAULT_REPOSITORY_LOCK_TIMEUNIT;
    protected volatile ISearchObjectGroup<String> currentSOGroup;

    public static final Long DEFAULT_XML_READ_FILE_TIMEOUT = (long)10;
    public static final TimeUnit DEFAULT_XML_READ_FILE_TIMEUNIT = TimeUnit.SECONDS;
    public static final Long DEFAULT_REPOSITORY_LOCK_TIMEOUT = (long)5;
    public static final TimeUnit DEFAULT_REPOSITORY_LOCK_TIMEUNIT = TimeUnit.SECONDS;
    protected volatile Map<SearchObjectKey,
                IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> soMap;
    protected volatile Map<ReplaceableParamKey,IReplaceableParam<?>> rpMap;

    protected volatile List<RefKeyedSO<
        ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> refList;

    protected volatile List<IRefKeyedSOContainer> refContainerList;

    public RegexXmlConfigFileReader(ExecutorService executor) {
        super(executor);
    }

    protected void parseDomStructure(Node elem, final TaskWatcher watcher) {
        //log.debug("Processing node " + elem.getNodeName());
        if (XmlConfigNodeType.GROUP.equalsNode(elem)) {
            if (elem.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME) != null) {
                currentSOGroup.addGroupElement(elem.getAttributes().getNamedItem(IConfigXmlConstants.ATTRIBUTE_NAME_NAME)
                        .getNodeValue());
                log.info("SOGroup updated = " + currentSOGroup);
            }
            for (int i = 0; i < elem.getChildNodes().getLength(); i++) {
                Node childNode = elem.getChildNodes().item(i);
                parseDomStructure(childNode, watcher);
            }
            currentSOGroup.removeLastGroupElement();
            log.info("SOGroup reset = " + currentSOGroup);
        } else if ((XmlConfigNodeType.COMPLEX_REGEXES.equalsNode(elem)) && (currentSOGroup.getElementCount() > 0)) {
            List<Node> crNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.COMPLEX_REGEX);
            for (final Node currNode : crNodes) {
                CompletableFuture<IKeyedSearchObject<?, ?>> future = submitConstructionTask(
                        constructionFactory.getSOConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();

                try {
                    future.handle(new BiFunction<IKeyedSearchObject<?, ?>, Throwable, Void>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public Void apply(IKeyedSearchObject<?, ?> result, Throwable t) {
                            if (result != null) {
                                result.setSearchObjectGroup(configObjectGroup);
                                log.info("Future addComplexRegex name=" + result.getSearchObjectName() + " group=" + configObjectGroup.getString());
                                soMap.put(new SearchObjectKey(result.getSearchObjectName(), result.getSearchObjectGroup(), result.getSearchObjectTags()),result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("ComplexRegex null object constructed " + currNode.getTextContent());
                            }
                            if (t != null) {
                                log.error("ComplexRegex Failure processing future ", t);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            }
                            return null;
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure Clone not supported ", e);
                }
            }
            log.info("ComplexRegex Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);

            List<Node> refCRNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_COMPLEX_REGEX);
            for (final Node currNode : refCRNodes) {
                CompletableFuture<? extends RefKeyedSO<
                    ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> future = submitConstructionTask(
                        constructionFactory.getRefConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();

                try {
                    future.handle(new BiFunction<RefKeyedSO<
                                        ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>,
                                    Throwable, Void>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public Void apply(RefKeyedSO<
                                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> result, Throwable t) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addRefComplexRegex name=" + result.getName() + " group=" + configObjectGroup.getString());
                                refList.add(result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("Ref(Distinct)ComplexRegex null object constructed " + currNode.getTextContent());
                            }
                            if (t != null) {
                                log.error("ComplexRegex Failure processing future ", t);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            }
                            return null;
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("Ref(Distinct)ComplexRegex Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);
        } else if ((XmlConfigNodeType.SEARCH_SCRIPTS.equalsNode(elem)) && (currentSOGroup.getElementCount() > 0)) {
            List<Node> crNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.SEARCH_SCRIPT);
            for (final Node currNode : crNodes) {
                CompletableFuture<ISOSearchScript> future = submitConstructionTask(
                        constructionFactory.getSearchScriptConstructor(), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();

                try {
                    future.handle(new BiFunction<ISOSearchScript, Throwable, Void>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public Void apply(ISOSearchScript result, Throwable t) {
                            if (result != null) {
                                result.setSearchObjectGroup(configObjectGroup);
                                log.info("Future addSearchScript name=" + result.getSearchObjectName() + " group=" + result.getSearchObjectGroup());
                                soMap.put(new SearchObjectKey(result.getSearchObjectName(), result.getSearchObjectGroup(), result.getSearchObjectTags()),result);
                                if (result.isContainRefs()) {
                                    refContainerList.add(result);
                                }
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("SearchScript null object constructed " + currNode.getTextContent());
                            }
                            if (t != null) {
                                log.error("SearchScript Failure processing future ",t);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            }
                            return null;
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("SearchScript Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);

            List<Node> refCRNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_SEARCH_SCRIPT);
            for (final Node currNode : refCRNodes) {
                CompletableFuture<? extends RefKeyedSO<
                    ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> future = submitConstructionTask(
                        constructionFactory.getRefConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();

                try {
                    future.handle(new BiFunction<RefKeyedSO<
                            ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>, Throwable, Void>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public Void apply(RefKeyedSO<
                                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> result, Throwable t) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addRefSearchScript name=" + result.getName() + " group=" + configObjectGroup.getString());
                                refList.add(result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("addRefSearchScript null object constructed " + currNode.getTextContent());
                            }
                            if (t != null) {
                                log.error("addRefSearchScript Failure processing future ", t);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            }
                            return null;
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("addRefSearchScript Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);
        } else if ((XmlConfigNodeType.KEYED_REGEXES.equalsNode(elem)) && (currentSOGroup.getElementCount() > 0)) {
            List<Node> crNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.KEYED_REGEX);
            for (final Node currNode : crNodes) {
                CompletableFuture<ISORegex> future = submitConstructionTask(
                        constructionFactory.getRegexConstructor(), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();

                try {
                    future.handle(new BiFunction<ISORegex, Throwable, Void>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public Void apply(ISORegex result, Throwable t) {
                            if (result != null) {
                                result.setSearchObjectGroup(configObjectGroup);
                                log.info("Future addRegex name=" + result.getSearchObjectName() + " group=" + configObjectGroup.getString());
                                soMap.put(new SearchObjectKey(result.getSearchObjectName(), result.getSearchObjectGroup(), result.getSearchObjectTags()),result);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("Regex null object constructed " + currNode.getTextContent());
                            }
                            if (t != null) {
                                log.error("Regex Failure processing future ", t);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            }
                            return null;
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("Regex Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);

            List<Node> refCRNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_KEYED_REGEX);
            for (final Node currNode : refCRNodes) {
                CompletableFuture<? extends RefKeyedSO<
                    ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> future = submitConstructionTask(
                        constructionFactory.getRefConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();

                try {
                    future.handle(new BiFunction<RefKeyedSO<
                            ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>, Throwable, Void>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public Void apply(RefKeyedSO<
                                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> result, Throwable t) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addRefKeyedRegex name=" + result.getName() + " group=" + configObjectGroup.getString());
                                refList.add(result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("addRefKeyedRegex null object constructed " + currNode.getTextContent());
                            }
                            if (t != null) {
                                log.error("addRefKeyedRegex Failure processing future ", t);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            }
                            return null;
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("addRefKeyedRegex Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);
        } else if ((XmlConfigNodeType.REPLACE_PARAMS.equalsNode(elem)) && (currentSOGroup.getElementCount() > 0)) {
            List<Node> crNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REPLACE_PARAM);
            for (final Node currNode : crNodes) {
                CompletableFuture<IReplaceableParam<?>> future = submitConstructionTask(
                        constructionFactory.getReplaceableParamConstructor(), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();

                try {
                    future.handle(new BiFunction<IReplaceableParam<?>, Throwable, Void>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public Void apply(IReplaceableParam<?> result, Throwable t) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addReplaceParam key=" + result.getKey() + " value=" + result.getValue());
                                rpMap.put(result.getKey(), result);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("ReplaceParam null object constructed " + currNode.getTextContent());
                            }
                            if (t != null) {
                                log.error("ReplaceParam Failure processing future ",t);
                                watcher.incrementAndGetFinished();
                                log.error("Tasks executed " + watcher.getFinished());
                            }
                            return null;
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("RP Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);
        } else {
            for (int i = 0; i < elem.getChildNodes().getLength(); i++) {
                Node childNode = elem.getChildNodes().item(i);
                parseDomStructure(childNode, watcher);
            }
        }
    }

    @Override
    public CompletableFuture<Integer> read(InputStream configRI) {
        soMap = new ConcurrentHashMap<SearchObjectKey,
                IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>();
        rpMap = new ConcurrentHashMap<ReplaceableParamKey,IReplaceableParam<?>>();
        refList = Collections.synchronizedList(new ArrayList<RefKeyedSO<
                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>>());
        refContainerList = Collections.synchronizedList(new ArrayList<IRefKeyedSOContainer>());
        final AddSOCallback completionCallback = new AddSOCallback();
        final CompletableFuture<Integer> returnFuture = new CompletableFuture<Integer>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        final Document doc;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(configRI);
            currentSOGroup = SearchObjectFactory.getInstance().getSearchObjectGroup();
            doc.getDocumentElement().normalize();
            TaskWatcher newWatcher = new TaskWatcher("RegexXmlConfig", modificationLocks, new ITaskWatcherCallback() {

                @Override
                public void submitTasks(TaskWatcher watcher) {
                    parseDomStructure(doc.getDocumentElement(), watcher);
                }

                @Override
                public void executionComplete() {
                    completionCallback.addAllSearchObject(soMap);
                    completionCallback.addAllReplaceableParam(rpMap);
                    completionCallback.resolveAllRefs(refList, refContainerList);
                    returnFuture.complete(soMap.size());
                }
            });
            newWatcher.startWatch(new TimeUnitWrapper(readXmlFileTimeout, readXmlFileTimeUnit),
                    new TimeUnitWrapper(repositoryLockTimeout, repositoryLockTimeUnit));
            return returnFuture;
        } catch (ParserConfigurationException e) {
            log.error("XML Exception", e);
        } catch (SAXException e) {
            log.error("XML Exception", e);
        } catch (IOException e) {
            log.error("XML Exception", e);
        }
        return null;
    }

}
