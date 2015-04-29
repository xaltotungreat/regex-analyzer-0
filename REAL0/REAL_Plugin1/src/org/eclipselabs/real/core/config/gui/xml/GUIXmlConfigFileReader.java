package org.eclipselabs.real.core.config.gui.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipselabs.real.core.config.gui.GUIConfigReaderImpl;
import org.eclipselabs.real.core.config.gui.constructor.IGUIConstructorFactory;
import org.eclipselabs.real.core.config.gui.xml.constructor.GUIXmlConstructorFactory;
import org.eclipselabs.real.core.config.xml.XmlConfigNodeType;
import org.eclipselabs.real.core.config.xml.XmlDomConstructionSource;
import org.eclipselabs.real.core.util.ITaskWatcherCallback;
import org.eclipselabs.real.core.util.TaskWatcher;
import org.eclipselabs.real.core.util.TimeUnitWrapper;
import org.eclipselabs.real.gui.core.GUIConfigKey;
import org.eclipselabs.real.gui.core.GUIConfigObjectType;
import org.eclipselabs.real.gui.core.GUIProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.util.concurrent.ListeningExecutorService;

public class GUIXmlConfigFileReader extends GUIConfigReaderImpl<InputStream> {

    private static final Logger log = LogManager.getLogger(GUIXmlConfigFileReader.class);

    protected IGUIConstructorFactory<Node> constructionFactory = GUIXmlConstructorFactory.getInstance();

    protected Long readXmlFileTimeout = DEFAULT_XML_READ_FILE_TIMEOUT;
    protected TimeUnit readXmlFileTimeUnit = DEFAULT_XML_READ_FILE_TIMEUNIT;
    protected Long repositoryLockTimeout = DEFAULT_REPOSITORY_LOCK_TIMEOUT;
    protected TimeUnit repositoryLockTimeUnit = DEFAULT_REPOSITORY_LOCK_TIMEUNIT;

    public static final Long DEFAULT_XML_READ_FILE_TIMEOUT = (long)10;
    public static final TimeUnit DEFAULT_XML_READ_FILE_TIMEUNIT = TimeUnit.SECONDS;
    public static final Long DEFAULT_REPOSITORY_LOCK_TIMEOUT = (long)5;
    public static final TimeUnit DEFAULT_REPOSITORY_LOCK_TIMEUNIT = TimeUnit.SECONDS;

    protected volatile Map<GUIConfigKey, Object> guiObjMap;

    public GUIXmlConfigFileReader(ListeningExecutorService executor) {
        super(executor);
    }

    protected void parseDomStructure(Node elem, final TaskWatcher watcher) {
        if ((XmlConfigNodeType.GUI_CONFIG.equalsNode(elem)) || (XmlConfigNodeType.GUI_PROPERTIES.equalsNode(elem))) {
            NodeList nl = elem.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof Element) {
                    parseDomStructure(nl.item(i), watcher);
                }
            }
        } else if (XmlConfigNodeType.SEARCH_OBJECT_TREE.equalsNode(elem)) {
            CompletableFuture<DefaultMutableTreeNode> future = submitConstructionTask(
                    constructionFactory.getSOTReeConstructor(), new XmlDomConstructionSource(elem));
            watcher.incrementAndGetSubmitted();
            log.info("SOTree Tasks submitted " + watcher.getSubmitted());
            future.handle((DefaultMutableTreeNode result, Throwable t) ->
                {
                    if (result != null) {
                        log.info("Future addSOTree name=" + result);
                        guiObjMap.put(new GUIConfigKey(GUIConfigObjectType.SEARCH_OBJECT_TREE),result);
                        watcher.incrementAndGetFinished();
                        log.info("Tasks finished " + watcher.getFinished());
                    }
                    if (t != null) {
                        log.error("SOTree Failure processing future " + t.toString(), t);
                        watcher.incrementAndGetFinished();
                        log.info("Tasks finished " + watcher.getFinished());
                    }
                    return null;
                });
        } else if (XmlConfigNodeType.GUI_PROPERTY.equalsNode(elem)) {
            CompletableFuture<GUIProperty> future = submitConstructionTask(
                    constructionFactory.getPropertyConstructor(), new XmlDomConstructionSource(elem));
            watcher.incrementAndGetSubmitted();
            log.info("GUIProperty Tasks submitted " + watcher.getSubmitted());
            future.handle((GUIProperty result, Throwable t) ->
            {
                if (result != null) {
                    log.info("Future addGUIProperty name=" + result);
                    guiObjMap.put(new GUIConfigKey(GUIConfigObjectType.GUI_PROPERTY, result.getName()),result);
                    watcher.incrementAndGetFinished();
                    log.info("Tasks finished " + watcher.getFinished());
                }
                if (t != null) {
                    log.error("GUIProperty Failure processing future " + t.toString(), t);
                    watcher.incrementAndGetFinished();
                    log.info("Tasks finished " + watcher.getFinished());
                }
                return null;
            });
        }
    }

    @Override
    public CompletableFuture<Integer> read(InputStream configRI) {
        guiObjMap = new ConcurrentHashMap<GUIConfigKey, Object>();
        final GUICompletionCallback completionCallback = new GUICompletionCallback();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        final CompletableFuture<Integer> returnFuture = new CompletableFuture<Integer>();
        try {
            db = dbf.newDocumentBuilder();
            log.info("Reading GUI config stream available=" + configRI.available());
            final Document doc = db.parse(configRI);
            doc.getDocumentElement().normalize();
            TaskWatcher newWatcher = new TaskWatcher("GUIXmlConfig", modificationLocks, new ITaskWatcherCallback() {

                @Override
                public void submitTasks(TaskWatcher watcher) {
                    parseDomStructure(doc.getDocumentElement(), watcher);
                }

                @Override
                public void executionComplete() {
                    completionCallback.addAll(guiObjMap);
                    returnFuture.complete(guiObjMap.size());
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
