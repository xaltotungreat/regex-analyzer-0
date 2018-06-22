package org.eclipselabs.real.core.config;


public interface IConfigurationConsts {

    String CONFIG_FOLDER = "config";
    // the properties file in the XML format
    String CONFIG_FILE_PERFORMANCE_CONFIG = "performance_config.xml";
    // Spring configuration consts
    String CONFIG_SPRING_FOLDER = "spring";
    String CONFIG_SPRING_MAIN_FILE = "main_beans.xml";
    // manual configuration constants
    String CONFIG_FILE_LOG_TYPES = "log_types.xml";
    String CONFIG_FILE_LOG_TYPES_ACTIVATION = "active_config.xml";
    String CONFIG_FILE_REGEX_CONFIG = "regex_config.xml";
    String CONFIG_FILE_GUI_CONFIG = "gui_config.xml";

    String CONFIG_PATH_PERFORMANCE_CONFIG = CONFIG_FOLDER + "/" + CONFIG_FILE_PERFORMANCE_CONFIG;
    String CONFIG_PATH_LOG_TYPES = CONFIG_FOLDER + "/" + CONFIG_FILE_LOG_TYPES;
    String CONFIG_PATH_LOG_TYPES_ACTIVATION = CONFIG_FOLDER + "/" + CONFIG_FILE_LOG_TYPES_ACTIVATION;
    String CONFIG_PATH_REGEX_CONFIG = CONFIG_FOLDER + "/" + CONFIG_FILE_REGEX_CONFIG;
    String CONFIG_PATH_GUI_CONFIG = CONFIG_FOLDER + "/" + CONFIG_FILE_GUI_CONFIG;

    // system properties used in the application
    String CONFIG_SYSPROP_PRODUCT = "real.config.product";
}
