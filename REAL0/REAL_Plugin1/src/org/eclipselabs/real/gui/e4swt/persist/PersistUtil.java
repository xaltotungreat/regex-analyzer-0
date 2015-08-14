package org.eclipselabs.real.gui.e4swt.persist;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipselabs.real.core.searchobject.param.IReplaceableParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamImpl;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceableParamValueType;
import org.eclipselabs.real.core.util.IRealCoreConstants;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

public class PersistUtil {
    private static final Logger log = LogManager.getLogger(PersistUtil.class);

    private PersistUtil() {}

    public static Schema getSchema(String schemaPath) {
        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Bundle plugBundle = Platform.getBundle(IEclipse4Constants.DEFINING_PLUGIN_NAME);
        if (plugBundle != null) {
            IPath pt = new Path(schemaPath);
            try (InputStream schemaIS = FileLocator.openStream(plugBundle, pt, false)) {
                //schemaIS = FileLocator.openStream(plugBundle, pt, false);
                StreamSource newSource = new StreamSource(schemaIS);
                return sf.newSchema(newSource);
            } catch (SAXException se) {
                log.error("getSchema SAX Exception ", se);
                return null;
            } catch (IOException e) {
                log.error("getSchema IO Exception ", e);
                return null;
            }
        }
        log.error("getSchema Plugin bundle is null");
        return null;
    }

    public static Map<ReplaceableParamKey,IReplaceableParam<?>> getReplaceMap(Map<ReplaceableParamKeyPersist,ReplaceableParamPersist<?>> persistMap) {
        Map<ReplaceableParamKey,IReplaceableParam<?>> loadedMap = new HashMap<>();
        if (persistMap != null) {
            for (Map.Entry<ReplaceableParamKeyPersist,ReplaceableParamPersist<?>> currParam : persistMap.entrySet()) {
                ReplaceableParamKey key = new ReplaceableParamKey(currParam.getKey().getName());
                switch(currParam.getValue().getValueType()) {
                case BOOLEAN:
                    IReplaceableParam<Boolean> rpBoolean = new ReplaceableParamImpl<Boolean>(ReplaceableParamValueType.BOOLEAN, key, currParam.getValue().getReplaceNames(),
                            (Boolean)currParam.getValue().getValue());
                    loadedMap.put(key, rpBoolean);
                    break;
                case INTEGER:
                    IReplaceableParam<Integer> rpInteger = new ReplaceableParamImpl<Integer>(ReplaceableParamValueType.INTEGER, key, currParam.getValue().getReplaceNames(),
                            (Integer)currParam.getValue().getValue());
                    loadedMap.put(key, rpInteger);

                    break;
                case DATE:
                    /*
                     * JAXB as of 2015-04-29 doesn't work with java.time classes
                     * Therefore the datetime is converted to String and stored as String.
                     */
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(IRealCoreConstants.DEFAULT_FORMAT_DATE_LONG, IRealCoreConstants.DEFAULT_DATE_LOCALE);
                    try {
                        LocalDateTime localDT = LocalDateTime.parse((String)currParam.getValue().getValue(), fmt);
                        IReplaceableParam<LocalDateTime> rpCalendar = new ReplaceableParamImpl<LocalDateTime>(ReplaceableParamValueType.DATE, key, currParam.getValue().getReplaceNames(),
                                localDT);
                        loadedMap.put(key, rpCalendar);
                    } catch (DateTimeParseException e) {
                        log.error("getReplaceMap ", e);
                    }
                    break;
                case STRING:
                default:
                    IReplaceableParam<String> rpString = new ReplaceableParamImpl<String>(ReplaceableParamValueType.STRING, key, null,
                            (String)currParam.getValue().getValue());
                    loadedMap.put(key, rpString);
                    break;
                }
            }
        }
        return loadedMap;
    }
}
