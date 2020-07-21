package org.codetab.scoopi.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.ConfigNotFoundException;

public class ConfigProperties {

    private static final Logger LOG = LogManager.getLogger();

    private Properties properties;

    public ConfigProperties(final Properties properties) {
        this.properties = properties;
    }

    /**
     * Throws ConfigNotFound if no config is defined, which is recoverable with
     * default otherwise higher level methods can throw unrecoverable
     * CriticalException.
     * @param key
     * @return
     * @throws ConfigNotFoundException
     */
    public String getConfig(final String key) throws ConfigNotFoundException {
        String value = properties.getProperty(key);
        if (isNull(value)) {
            throw new ConfigNotFoundException(key);
        }
        return value;
    }

    /**
     * Throws ConfigNotFound if no config is defined, which is recoverable with
     * default value otherwise higher level methods can throw unrecoverable
     * CriticalException.
     * @param key
     * @return
     * @throws ConfigNotFoundException
     */
    public String[] getStringArray(final String key)
            throws ConfigNotFoundException {
        String value = properties.getProperty(key);
        if (nonNull(value)) {
            String[] values = value.split(" ; ");
            if (values.length > 0) {
                return values;
            }
        }
        throw new ConfigNotFoundException(key);
    }

    public boolean getBoolean(final String configKey,
            final boolean defaultValue) {
        String v = properties.getProperty(configKey);
        boolean value = defaultValue;
        if (isNull(v)) {
            LOG.debug("config: {} not found, use default: {}", configKey,
                    defaultValue);
        } else {
            if (StringUtils.equalsAnyIgnoreCase(v, "true", "false")) {
                value = Boolean.valueOf(v);
            } else {
                LOG.error("config: {}, {} is not boolean, use default: {}",
                        configKey, v, defaultValue);
            }
        }
        return value;
    }

    public int getInt(final String configKey, final int defaultValue) {
        String v = properties.getProperty(configKey);
        int value = defaultValue;
        if (isNull(v)) {
            LOG.debug("config: {} not found, use default: {}", configKey,
                    defaultValue);
        } else {
            try {
                value = Integer.parseInt(v);
            } catch (NumberFormatException e) {
                LOG.error("config: {}, use default: {}, parse error:",
                        configKey, defaultValue, e);
            }
        }
        return value;
    }

    /*
     * get or set property object
     */
    public Object get(final String key) {
        return properties.get(key);
    }

    public void put(final String key, final Object value) {
        properties.put(key, value);
    }
}
