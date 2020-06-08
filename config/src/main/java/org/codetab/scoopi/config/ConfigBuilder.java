package org.codetab.scoopi.config;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.codetab.scoopi.exception.CriticalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build composite configs from system, user provided and default configs. Also,
 * add derived configs such as runDateTime etc.,
 * <p>
 * System properties has highest priority, next is user provided and default
 * config is the lowest.
 * <p>
 * Default property, defined in scoopi-default.xml, is overridden by user
 * defined property, defined in scoopi.properties, is overridden by system
 * property defined either in env variable.
 * @author m
 *
 */
public class ConfigBuilder {

    enum ConfigIndex {
        SYSTEM, PROVIDED, DEFAULTS
    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConfigBuilder.class);

    @Inject
    private CompositeConfiguration configuration;
    @Inject
    private Configurations configurations;
    @Inject
    private SystemConfiguration systemConfigs;
    @Inject
    private DerivedConfigs derivedConfigs;

    public void build(final String userConfigFile,
            final String defaultConfigFile) {

        LOGGER.info("build config");

        configuration.addConfiguration(systemConfigs);

        try {
            Configuration userProvided =
                    configurations.properties(new File(userConfigFile));
            configuration.addConfiguration(userProvided);
        } catch (ConfigurationException e) {
            configuration.addConfiguration(new PropertiesConfiguration());
            LOGGER.info("{}, {}", e.getMessage(), "use default configs");
        }

        try {
            Configuration defaults =
                    configurations.xml(new File(defaultConfigFile));
            configuration.addConfiguration(defaults);
        } catch (ConfigurationException e) {
            throw new CriticalException("unable to create config service", e);
        }

        derivedConfigs.addRunDates(configuration);
        derivedConfigs.replaceHighDate(configuration);
        derivedConfigs.addRunnerClass(configuration);
    }

    public Properties getEffectiveProperties() {
        Properties properties = new Properties();
        Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            properties.put(key, configuration.getProperty(key));
        }
        return properties;
    }

    public void logConfigs(final Properties properties) {
        TreeMap<Object, Object> sortedMap = new TreeMap<>(properties);
        sortedMap.remove("java.class.path");

        String newline = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sortedMap.forEach((k, v) -> {
            sb.append(k);
            sb.append("=");
            sb.append(v);
            sb.append(newline);
        });
        LOGGER.info("rundate {}",
                properties.getProperty("scoopi.runDateTimeString"));
        LOGGER.debug(
                "{}---- effective configuration ----{}{}-------------------",
                newline, newline, sb.toString());
    }
}
