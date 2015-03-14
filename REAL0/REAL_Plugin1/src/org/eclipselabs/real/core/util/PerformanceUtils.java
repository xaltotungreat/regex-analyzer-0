package org.eclipselabs.real.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PerformanceUtils {
    private static final Logger log = LogManager.getLogger(PerformanceUtils.class);

    private PerformanceUtils() {}

    public static int getIntProperty(String propertyKey, int defaultValue) {
        int result = defaultValue;
        if (System.getProperty(propertyKey) != null) {
            try {
                result = Integer.parseInt(System.getProperty(propertyKey));
            } catch (NumberFormatException nfe) {
                log.error("getIntProperty incorrect int value " + System.getProperty(propertyKey)
                        + " use default " + defaultValue);
            }
        }
        return result;
    }
}
