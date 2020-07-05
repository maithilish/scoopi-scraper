package org.codetab.scoopi.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.spaceit;

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
        String value = properties.getProperty(configKey);
        if (isNull(value)) {
            return defaultValue;
        } else {
            if (StringUtils.equalsAnyIgnoreCase(value, "true", "false")) {
                return Boolean.valueOf(value);
            } else {
                LOG.error("config {}: {} is not boolean, using default value",
                        configKey, value);
                return defaultValue;
            }
        }
    }

    public int getInt(final String configKey, final int defaultValue) {
        String value = properties.getProperty(configKey);
        if (isNull(value)) {
            final String message = spaceit("config not found:", configKey,
                    ", defaults to:", String.valueOf(defaultValue));
            LOG.debug("{}", message);
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                final String message =
                        spaceit("parse error, config:", configKey,
                                ", defaults to:", String.valueOf(defaultValue));
                LOG.error("{}, {}", e, message);
                return defaultValue;
            }
        }
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
