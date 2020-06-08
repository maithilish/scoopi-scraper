package org.codetab.scoopi.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Properties;

import org.codetab.scoopi.exception.ConfigNotFoundException;

public class ConfigProperties {

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
    public String getProperty(final String key) throws ConfigNotFoundException {
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

    /*
     * wrapper methods - return null when key not found returns, doesn't throw
     * exception
     */

    public String getProperty(final String key, final String defaultValue) {
        return properties.getProperty(key);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        Object value = properties.get(key);
        if (nonNull(value)) {
            return new Boolean((String) value);
        } else {
            return defaultValue;
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
