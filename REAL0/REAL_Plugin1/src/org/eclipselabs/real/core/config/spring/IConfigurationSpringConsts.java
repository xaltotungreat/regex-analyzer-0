package org.eclipselabs.real.core.config.spring;

import java.io.File;

public interface IConfigurationSpringConsts {

    String CONFIG_FOLDER = "config";
    // the properties file in the XML format
    String CONFIG_FILE_PERFORMANCE_CONFIG = "performance_config.xml";
    String CONFIG_FILE_LOG_TYPES_ACTIVATION = "active_config.xml";

    // Spring configuration consts
    String CONFIG_SPRING_FOLDER = "spring";
    String CONFIG_SPRING_MAIN_FILE = "main_beans.xml";

    String CONFIG_PATH_PERFORMANCE_CONFIG = CONFIG_FOLDER + "/" + CONFIG_FILE_PERFORMANCE_CONFIG;

    // system properties used in the application
    String CONFIG_SYSPROP_PRODUCT = "real.config.product";

    default String getActivationPath(String productName) {
        return CONFIG_FOLDER + File.separator + productName + File.separator + CONFIG_FILE_LOG_TYPES_ACTIVATION;
    }

    default String getSpringConfigPath(String productName) {
        return CONFIG_FOLDER + File.separator + productName + File.separator
                + IConfigurationSpringConsts.CONFIG_SPRING_FOLDER + File.separator
                + IConfigurationSpringConsts.CONFIG_SPRING_MAIN_FILE;
     }
}
