package org.eclipselabs.real.gui.e4swt.startup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.config.spring.IConfigurationSpringConsts;
import org.eclipselabs.real.core.config.spring.SpringConfigReader;
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
        initSpringConfig(plugBundle);
    }

    private void initSpringConfig(Bundle plugBundle) {

        String configName = System.getProperty(IConfigurationSpringConsts.CONFIG_SYSPROP_PRODUCT);
        if ((configName == null) || (configName.isEmpty())) {
            log.error("The value for the product config not found " + IConfigurationSpringConsts.CONFIG_SYSPROP_PRODUCT);
            return;
        }

        IConfigurationSpringConsts allPaths = new IConfigurationSpringConsts() {
        };

        /*
         * Initialize the performance values first
         */
        try (InputStream perfIS = FileLocator.openStream(
                plugBundle, new Path(IConfigurationSpringConsts.CONFIG_PATH_PERFORMANCE_CONFIG), false)) {
            Properties perfProp = new Properties();
            perfProp.loadFromXML(perfIS);
            perfProp.forEach((Object key, Object value) -> System.setProperty((String)key, (String)value));
            log.info("startup Performance properties loaded");
        } catch (IOException e1) {
            log.error("Init performance config error",e1);
        }

        InputStream logActivationIS = null;
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(allPaths.getSpringConfigPath(configName))) {
            String activationPath = allPaths.getActivationPath(configName);
            if (FileLocator.findEntries(plugBundle, new Path(activationPath)).length > 0) {
                logActivationIS = FileLocator.openStream(plugBundle, new Path(activationPath), false);
            }
            /*
             * If the file with active log types has been found add a bean with the input stream
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
