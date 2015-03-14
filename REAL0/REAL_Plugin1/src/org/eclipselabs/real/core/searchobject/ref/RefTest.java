package org.eclipselabs.real.core.searchobject.ref;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipselabs.real.core.config.ConfigurationController;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.core.searchobject.ISOComplexRegex;
import org.eclipselabs.real.core.searchobject.SearchObjectController;
import org.eclipselabs.real.core.searchobject.SearchObjectFactory;
import org.eclipselabs.real.core.searchobject.SearchObjectKey;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.osgi.framework.Bundle;

public class RefTest {

    private static final Logger log = LogManager.getLogger(RefTest.class);
    
    public RefTest() {
        Bundle plugBundle = Platform.getBundle(IEclipse4Constants.DEFINING_PLUGIN_NAME);
        if (plugBundle == null) {
            return;
        }

        /*try (InputStream log4jIS = FileLocator.openStream(
                plugBundle, new Path("config/log4j.properties"), false)) {
            PropertyConfigurator.configure(log4jIS);
            log.info("Logging configured");
        } catch (IOException e) {
            log.error("Init log4j error", e);
        }*/
        
        // load the log types
        try (InputStream logsIS = FileLocator.openStream(
                    plugBundle, new Path("config/log_types.xml"), false); 
                InputStream logActivationIS = FileLocator.openStream(
                        plugBundle, new Path("config/active_config.xml"), false)) {
            LogFileTypes.INSTANCE.initXml(logsIS, logActivationIS);
        } catch (IOException e) {
            log.error("Init logs error", e);
        }
        
        try (InputStream guiIS = FileLocator.openStream(
                plugBundle, new Path("config/gui_config.xml"), false)) {
            log.info("Loading the GUI configuration");
            ConfigurationController.INSTANCE.initXmlGUIConfiguration(guiIS);
        } catch (IOException e) {
            log.error("Init GUI error", e);
        }
        
        try (InputStream regexIS = FileLocator.openStream(
                plugBundle, new Path("config/regex_config.xml"), false)) {
            log.info("Loading the regex configuration");
            ConfigurationController.INSTANCE.initXmlRegexConfiguration(regexIS);
        } catch (IOException e) {
            log.error("Init regex error", e);
        }
        
        log.info("Finished Loading the config files");
        
    }

    public static void main(String[] args) {
        RefTest tmp = new RefTest();
        tmp.performTest();
        System.exit(0);
    }
    
    public void performTest() {
        Map<String,String> origSOTags = new HashMap<>();
        origSOTags.put("LogFile", "SGM_SipSp");
        origSOTags.put("Test1", "Test 1");
        origSOTags.put("Test2", "Test 2");
        origSOTags.put("Release 6.4", "TRUE");
        try {
            SearchObjectController.INSTANCE.getSearchObjectRepository().getReadLock().lock();
            ISOComplexRegex testSO = (ISOComplexRegex)SearchObjectController.INSTANCE.getSearchObjectRepository().get(
                    new SearchObjectKey("TestPlan1", SearchObjectFactory.getInstance().getSearchObjectGroup("Global.SGM_SipSp"), origSOTags));
            log.debug("ORIGINAl SO " + testSO);
            
            Map<String,String> refSOTags = new HashMap<>();
            refSOTags.put("newSOTag", "Tag is set");
            
            SearchObjectController.INSTANCE.getSearchObjectRepository().get(
                    new SearchObjectKey("RefTestPlan1", SearchObjectFactory.getInstance().getSearchObjectGroup("Global.SGM_SipSp"), refSOTags));
            
            ISOComplexRegex refResolvedSO2 = (ISOComplexRegex)SearchObjectController.INSTANCE.getSearchObjectRepository().get(
                    new SearchObjectKey("RefTestPlan_Global", SearchObjectFactory.getInstance().getSearchObjectGroup("Global"), refSOTags));
            log.debug("RESULT SO 2 " + refResolvedSO2);
        } finally {
            SearchObjectController.INSTANCE.getSearchObjectRepository().getReadLock().unlock();
        }
    }

}
