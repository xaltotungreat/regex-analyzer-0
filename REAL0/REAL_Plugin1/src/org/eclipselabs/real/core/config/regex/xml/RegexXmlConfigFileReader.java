package org.eclipselabs.real.core.config.regex.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.regex.RegexConfigReaderImpl;
import org.eclipselabs.real.core.config.regex.constructor.IRegexConstructorFactory;
import org.eclipselabs.real.core.config.regex.xml.constructor.RegexXmlConstructorFactoryImpl;
import org.eclipselabs.real.core.config.xml.ConfigXmlUtil;
import org.eclipselabs.real.core.config.xml.IConfigXmlConstants;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.searchobject.IKeyedSearchObject;
import org.eclipselabs.real.core.searchobject.ISORegex;
import org.eclipselabs.real.core.searchobject.ISOSearchScript;
import org.eclipselabs.real.core.searchobject.ISearchObjectGroup;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

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
    protected volatile Map<ReplaceParamKey,IReplaceParam<?>> rpMap;
    
    protected volatile List<RefKeyedSO<
        ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> refList;
    
    protected volatile List<IRefKeyedSOContainer> refContainerList;

    public RegexXmlConfigFileReader(ListeningExecutorService executor) {
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
            List<Node> crNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.COMPLEX_REGEX, XmlConfigNodeType.DISTINCT_COMPLEX_REGEX);
            for (final Node currNode : crNodes) {
                ListenableFuture<IKeyedSearchObject<?, ?>> future = submitConstructionTask(
                        constructionFactory.getSOConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();
                
                try {
                    Futures.addCallback(future, new FutureCallback<IKeyedSearchObject<?, ?>>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public void onSuccess(IKeyedSearchObject<?, ?> result) {
                            if (result != null) {
                                result.setSearchObjectGroup(configObjectGroup);
                                log.info("Future add(Distinct)ComplexRegex name=" + result.getSearchObjectName() + " group=" + configObjectGroup.getString());
                                soMap.put(new SearchObjectKey(result.getSearchObjectName(), result.getSearchObjectGroup(), result.getSearchObjectTags()),result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("ComplexRegex null object constructed " + currNode.getTextContent());
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("ComplexRegex Failure processing future ", t);
                            watcher.incrementAndGetFinished();
                            log.error("Tasks finished " + watcher.getFinished());
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure Clone not supported ", e);
                }
            }
            log.info("ComplexRegex Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);
            
            List<Node> refCRNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_COMPLEX_REGEX, XmlConfigNodeType.REF_DISTINCT_COMPLEX_REGEX);
            for (final Node currNode : refCRNodes) {
                ListenableFuture<? extends RefKeyedSO<
                    ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> future = submitConstructionTask(
                        constructionFactory.getRefConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();
                
                try {
                    Futures.addCallback(future, new FutureCallback<RefKeyedSO<
                            ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public void onSuccess(RefKeyedSO<
                                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> result) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addRef(Distinct)ComplexRegex name=" + result.getName() + " group=" + configObjectGroup.getString());
                                refList.add(result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("Ref(Distinct)ComplexRegex null object constructed " + currNode.getTextContent());
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("Ref(Distinct)ComplexRegex Failure processing future ", t);
                            watcher.incrementAndGetFinished();
                            log.error("Tasks finished " + watcher.getFinished());
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
                ListenableFuture<ISOSearchScript> future = submitConstructionTask(
                        constructionFactory.getSearchScriptConstructor(), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();
                
                try {
                    Futures.addCallback(future, new FutureCallback<ISOSearchScript>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public void onSuccess(ISOSearchScript result) {
                            if (result != null) {
                                result.setSearchObjectGroup(configObjectGroup);
                                log.info("Future addSearchScript name=" + result.getSearchObjectName() + " group=" + result.getSearchObjectGroup());
                                soMap.put(new SearchObjectKey(result.getSearchObjectName(), result.getSearchObjectGroup(), result.getSearchObjectTags()),result);
                                if (result.isContainRefs()) {
                                    refContainerList.add(result);
                                }
                                watcher.incrementAndGetFinished();
                            } else {
                                log.error("SearchScript null object constructed " + currNode.getTextContent());
                            }
                            log.info("Tasks finished " + watcher.getFinished());
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("SearchScript Failure processing future ",t);
                            watcher.incrementAndGetFinished();
                            log.error("Tasks finished " + watcher.getFinished());
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("SearchScript Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);
            
            List<Node> refCRNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_SEARCH_SCRIPT);
            for (final Node currNode : refCRNodes) {
                ListenableFuture<? extends RefKeyedSO<
                    ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> future = submitConstructionTask(
                        constructionFactory.getRefConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();
                
                try {
                    Futures.addCallback(future, new FutureCallback<RefKeyedSO<
                            ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public void onSuccess(RefKeyedSO<
                                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> result) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addRefSearchScript name=" + result.getName() + " group=" + configObjectGroup.getString());
                                refList.add(result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("addRefSearchScript null object constructed " + currNode.getTextContent());
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("addRefSearchScript Failure processing future ", t);
                            watcher.incrementAndGetFinished();
                            log.error("Tasks finished " + watcher.getFinished());
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
                ListenableFuture<ISORegex> future = submitConstructionTask(
                        constructionFactory.getRegexConstructor(), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();
                
                try {
                    Futures.addCallback(future, new FutureCallback<ISORegex>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public void onSuccess(ISORegex result) {
                            if (result != null) {
                                result.setSearchObjectGroup(configObjectGroup);
                                log.info("Future addRegex name=" + result.getSearchObjectName() + " group=" + configObjectGroup.getString());
                                soMap.put(new SearchObjectKey(result.getSearchObjectName(), result.getSearchObjectGroup(), result.getSearchObjectTags()),result);
                                watcher.incrementAndGetFinished();
                            } else {
                                log.error("Regex null object constructed " + currNode.getTextContent());
                            }
                            log.info("Tasks executed " + watcher.getFinished());
                        }
   
                        @Override
                        public void onFailure(Throwable t) {
                            log.error("Regex Failure processing future ", t);
                            watcher.incrementAndGetFinished();
                            log.error("Tasks executed " + watcher.getFinished());
                        }
                    });
                } catch (CloneNotSupportedException e) {
                    log.error("parseDomStructure ", e);
                }
            }
            log.info("Regex Tasks submitted " + watcher.getSubmitted() + " for group " + currentSOGroup);
            
            List<Node> refCRNodes = ConfigXmlUtil.collectChildNodes(elem, XmlConfigNodeType.REF_KEYED_REGEX);
            for (final Node currNode : refCRNodes) {
                ListenableFuture<? extends RefKeyedSO<
                    ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>> future = submitConstructionTask(
                        constructionFactory.getRefConstructor(currNode), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();
                
                try {
                    Futures.addCallback(future, new FutureCallback<RefKeyedSO<
                            ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public void onSuccess(RefKeyedSO<
                                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>> result) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addRefKeyedRegex name=" + result.getName() + " group=" + configObjectGroup.getString());
                                refList.add(result);
                                watcher.incrementAndGetFinished();
                                log.info("Tasks finished " + watcher.getFinished());
                            } else {
                                log.error("addRefKeyedRegex null object constructed " + currNode.getTextContent());
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("addRefKeyedRegex Failure processing future ", t);
                            watcher.incrementAndGetFinished();
                            log.error("Tasks finished " + watcher.getFinished());
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
                ListenableFuture<IReplaceParam<?>> future = submitConstructionTask(
                        constructionFactory.getReplaceParamConstructor(), new XmlDomConstructionSource(currNode));
                watcher.incrementAndGetSubmitted();
                
                try {
                    Futures.addCallback(future, new FutureCallback<IReplaceParam<?>>() {
                        protected ISearchObjectGroup<String> configObjectGroup = currentSOGroup.clone();
                        @Override
                        public void onSuccess(IReplaceParam<?> result) {
                            if (result != null) {
                                result.setGroup(configObjectGroup);
                                log.info("Future addReplaceParam key=" + result.getKey() + " value=" + result.getValue());
                                rpMap.put(result.getKey(), result);
                                watcher.incrementAndGetFinished();
                            } else {
                                log.error("ReplaceParam null object constructed " + currNode.getTextContent());
                            }
                            log.info("Tasks executed " + watcher.getFinished());
                        }
   
                        @Override
                        public void onFailure(Throwable t) {
                            log.error("ReplaceParam Failure processing future ",t);
                            watcher.incrementAndGetFinished();
                            log.error("Tasks executed " + watcher.getFinished());
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
    public ListenableFuture<Integer> read(InputStream configRI) {
        soMap = new ConcurrentHashMap<SearchObjectKey,
                IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>();
        rpMap = new ConcurrentHashMap<ReplaceParamKey,IReplaceParam<?>>();
        refList = Collections.synchronizedList(new ArrayList<RefKeyedSO<
                ? extends IKeyedSearchObject<? extends IKeyedSearchResult<?>,? extends ISearchResultObject>>>());
        refContainerList = Collections.synchronizedList(new ArrayList<IRefKeyedSOContainer>());
        final AddSOCallback completionCallback = new AddSOCallback();
        final SettableFuture<Integer> returnFuture = SettableFuture.<Integer>create();
        
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
                    completionCallback.addAllReplaceParam(rpMap);
                    completionCallback.resolveAllRefs(refList, refContainerList);
                    returnFuture.set(soMap.size());
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
