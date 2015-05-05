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
import org.eclipselabs.real.core.searchobject.param.IReplaceParam;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamImpl;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamKey;
import org.eclipselabs.real.core.searchobject.param.ReplaceParamValueType;
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

    public static Map<ReplaceParamKey,IReplaceParam<?>> getReplaceMap(Map<ReplaceParamKeyPersist,ReplaceParamPersist<?>> persistMap) {
        Map<ReplaceParamKey,IReplaceParam<?>> loadedMap = new HashMap<>();
        if (persistMap != null) {
            for (Map.Entry<ReplaceParamKeyPersist,ReplaceParamPersist<?>> currParam : persistMap.entrySet()) {
                ReplaceParamKey key = new ReplaceParamKey(currParam.getKey().getName());
                switch(currParam.getValue().getValueType()) {
                case BOOLEAN:
                    IReplaceParam<Boolean> rpBoolean = new ReplaceParamImpl<Boolean>(ReplaceParamValueType.BOOLEAN, key, currParam.getValue().getReplaceNames(),
                            (Boolean)currParam.getValue().getValue());
                    loadedMap.put(key, rpBoolean);
                    break;
                case INTEGER:
                    IReplaceParam<Integer> rpInteger = new ReplaceParamImpl<Integer>(ReplaceParamValueType.INTEGER, key, currParam.getValue().getReplaceNames(),
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
                        IReplaceParam<LocalDateTime> rpCalendar = new ReplaceParamImpl<LocalDateTime>(ReplaceParamValueType.DATE, key, currParam.getValue().getReplaceNames(),
                                localDT);
                        loadedMap.put(key, rpCalendar);
                    } catch (DateTimeParseException e) {
                        log.error("getReplaceMap ", e);
                    }
                    break;
                case STRING:
                default:
                    IReplaceParam<String> rpString = new ReplaceParamImpl<String>(ReplaceParamValueType.STRING, key, null,
                            (String)currParam.getValue().getValue());
                    loadedMap.put(key, rpString);
                    break;
                }
            }
        }
        return loadedMap;
    }
}
