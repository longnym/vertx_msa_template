package com.sk.bds.core.module.template.util;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Map propertiesMap;

    private static final String VERTX_CONFIG_PROPS = "config-template.properties";
    private static final String VERTX_CONFIG_PROPS_EXTERNAL = "../conf/config-template.properties";

    static {
        if (propertiesMap == null) {
            propertiesMap = new HashMap<String, Object>();

            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(VERTX_CONFIG_PROPS);
                if (inputStream != null) {
                    logger.info("Loading configuration from the jar file.");
                    properties.load(inputStream);
                } else {
                    // In this case, config file should be located in "{location of main worker}/../conf"
                    logger.info("Loading configuration from the classpath.");
                    inputStream = new FileInputStream(VERTX_CONFIG_PROPS_EXTERNAL);
                    properties.load(inputStream);
                }
            } catch (NullPointerException e) {
                logger.error("Error is occurred when loading properties. (input stream is null). : " + VERTX_CONFIG_PROPS, e);
                System.exit(-1);
            } catch (IOException e) {
                logger.error("Error is occurred when loading properties.  : " + VERTX_CONFIG_PROPS, e);
                System.exit(-1);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.error("Error closing input stream while reading configuration : " + VERTX_CONFIG_PROPS, e);
                    }
                }
            }

            for (String key : properties.stringPropertyNames()) {
                Object value = properties.getProperty(key);
                propertiesMap.put(key, value);
            }
        }
        logger.info("Loading Properties utils complete : " + VERTX_CONFIG_PROPS);
    }

    public static Map<String, Object> getProperties() {
        return propertiesMap;
    }

    public static JsonObject getJsonProperties() {
        JsonObject propertiesJson = new JsonObject(propertiesMap);
        return propertiesJson;
    }

    public static String getProperty(String key) {
        if (key == null) {
            return null;
        }
        String retVal = (String) propertiesMap.get(key);
        if (retVal == null) {
            retVal = "";
        }
        return retVal;
    }

    public static String getProperty(String key, String defaultValue) {
        if (key == null) {
            return null;
        }
        String retVal = (String) propertiesMap.get(key);
        if (retVal == null) {
            retVal = defaultValue;
        }
        return retVal;
    }
}