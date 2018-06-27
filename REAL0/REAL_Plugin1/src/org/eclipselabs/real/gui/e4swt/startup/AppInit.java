package org.eclipselabs.real.gui.e4swt.startup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.config.ml.ConfigurationController;
import org.eclipselabs.real.core.config.ml.IConfigurationMLConsts;
import org.eclipselabs.real.core.config.spring.IConfigurationSpringConsts;
import org.eclipselabs.real.core.config.spring.SpringConfigReader;
import org.eclipselabs.real.core.logtype.LogFileTypes;
import org.eclipselabs.real.gui.e4swt.IEclipse4Constants;
import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppInit {
    private static final Logger log = LogManager.getLogger(AppInit.class);

    @PostContextCreate
    public void startup() {

        Bundle plugBundle = Platform.getBundle(IEclipse4Constants.DEFINING_PLUGIN_NAME);
        if (plugBundle == null) {
            return;
        }
        // use the old config for now
        //initFromOldXMLConfig(plugBundle);
        initSpringConfig(plugBundle);
    }

    private void initFromOldXMLConfig(Bundle plugBundle) {

        try (InputStream perfIS = FileLocator.openStream(
                plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_PERFORMANCE_CONFIG), false)) {
            Properties perfProp = new Properties();
            perfProp.loadFromXML(perfIS);
            perfProp.forEach((Object key, Object value) -> System.setProperty((String)key, (String)value));
            log.info("startup Performance properties loaded");
        } catch (IOException e1) {
            log.error("Init performance config error",e1);
        }

        // load the log types
        InputStream logActivationIS = null;
        try (InputStream logsIS = FileLocator.openStream(
                    plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_LOG_TYPES), false)) {
            if (FileLocator.findEntries(plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_LOG_TYPES_ACTIVATION)).length > 0) {
                logActivationIS = FileLocator.openStream(
                        plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_LOG_TYPES_ACTIVATION), false);
            }
            LogFileTypes.INSTANCE.initXml(logsIS, logActivationIS);
        } catch (IOException e) {
            log.error("Init logs error", e);
        } finally {
            if (logActivationIS != null) {
                try {
                    logActivationIS.close();
                } catch (IOException e) {
                    log.error("startup",e);
                }
            }
        }

        // load the regex configuration
        CompletableFuture<Integer> regexConfigFuture = null;
        try (InputStream regexIS = FileLocator.openStream(
                plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_REGEX_CONFIG), false)) {
            log.info("Loading the regex configuration");
            regexConfigFuture = ConfigurationController.INSTANCE.initXmlRegexConfiguration(regexIS);
        } catch (IOException e) {
            log.error("Init regex error", e);
        }

        // load the gui configuration
        CompletableFuture<Integer> guiConfigFuture = null;
        try (InputStream guiIS = FileLocator.openStream(
                plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_GUI_CONFIG), false)) {
            log.info("Loading the GUI configuration");
            guiConfigFuture = ConfigurationController.INSTANCE.initXmlGUIConfiguration(guiIS);
        } catch (IOException e) {
            log.error("Init GUI error", e);
        }

        /*
         * The main thread needs to be blocked until the configuration is read.
         * Otherwise the GUI may start before the regex config and Gui config is read
         */
        try {
            if (regexConfigFuture != null) {
                regexConfigFuture.get();
            }
            if (guiConfigFuture != null) {
                guiConfigFuture.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("startup",e);
        }

        log.info("Finished Loading the config files");
    }

    private void initSpringConfig(Bundle plugBundle) {
        /*
         * This is an example of loading a config from a Spring xml config instead of
         * using my own parsing. A LOOOOT of time may be saved. Need to rework the parsing mechanism.
         * TODO
         */
        String configName = System.getProperty(IConfigurationSpringConsts.CONFIG_SYSPROP_PRODUCT);
        if ((configName == null) || (configName.isEmpty())) {
            log.error("The value for the product config not found " + IConfigurationSpringConsts.CONFIG_SYSPROP_PRODUCT);
            return;
        }

        /*
         * Initialize the performance values first
         */
        try (InputStream perfIS = FileLocator.openStream(
                plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_PERFORMANCE_CONFIG), false)) {
            Properties perfProp = new Properties();
            perfProp.loadFromXML(perfIS);
            perfProp.forEach((Object key, Object value) -> System.setProperty((String)key, (String)value));
            log.info("startup Performance properties loaded");
        } catch (IOException e1) {
            log.error("Init performance config error",e1);
        }

        InputStream logActivationIS = null;
        String pathToMainBeans = IConfigurationMLConsts.CONFIG_FOLDER + File.separator
                + IConfigurationSpringConsts.CONFIG_SPRING_FOLDER + File.separator
                + configName + File.separator + IConfigurationSpringConsts.CONFIG_SPRING_MAIN_FILE;
        try (ClassPathXmlApplicationContext context
                    = new ClassPathXmlApplicationContext(pathToMainBeans)) {

            if (FileLocator.findEntries(plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_LOG_TYPES_ACTIVATION)).length > 0) {
                logActivationIS = FileLocator.openStream(
                        plugBundle, new Path(IConfigurationMLConsts.CONFIG_PATH_LOG_TYPES_ACTIVATION), false);
            }
            /*
             * If the file with active log types is present add a bean with the input stream
             * to the context. It will be used by the Spring configuration reader.
             */
            if (logActivationIS != null) {
                context.getBeanFactory().registerSingleton("logActivationInputStream", logActivationIS);
            }

            IConfigReader<ApplicationContext, Integer> springConfigReader = new SpringConfigReader();

            springConfigReader.read(context);

        } catch (IOException e) {
            log.error("Init logs error", e);
        } finally {
            if (logActivationIS != null) {
                try {
                    logActivationIS.close();
                } catch (IOException e) {
                    log.error("startup",e);
                }
            }
        }
    }
}
