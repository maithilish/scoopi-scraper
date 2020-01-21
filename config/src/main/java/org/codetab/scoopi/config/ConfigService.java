package org.codetab.scoopi.config;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.LINE;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class ConfigService {

    enum ConfigIndex {
        SYSTEM, PROVIDED, DEFAULTS
    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConfigService.class);

    private CompositeConfiguration configuration;

    @Inject
    private ConfigService() {
    }

    public void init(final String userConfigFile,
            final String defaultConfigFile) {
        LOGGER.info("initialize config service");

        configuration = new CompositeConfiguration();

        SystemConfiguration systemConfigs = new SystemConfiguration();
        configuration.addConfiguration(systemConfigs);

        try {
            Configuration userProvided = getPropertiesConfigs(userConfigFile);
            configuration.addConfiguration(userProvided);
        } catch (ConfigurationException e) {
            configuration.addConfiguration(new PropertiesConfiguration());
            LOGGER.info("{}, {}", e.getMessage(), "use default configs");
        }

        try {
            Configuration defaults = getXMLConfigs(defaultConfigFile);
            configuration.addConfiguration(defaults);
        } catch (ConfigurationException e) {
            throw new CriticalException("unable to create config service", e);
        }

        LOGGER.trace("{}", configsAsString(ConfigIndex.SYSTEM)); //$NON-NLS-1$
        LOGGER.debug("{}", configsAsString(ConfigIndex.PROVIDED)); //$NON-NLS-1$
        LOGGER.debug("{}", configsAsString(ConfigIndex.DEFAULTS)); //$NON-NLS-1$
        LOGGER.debug("config service initialized");
        LOGGER.info("config precedence: system, user defined, deaults");
        LOGGER.info(
                "use scoopi.properties or system property to override defaults");
    }

    public final CompositeConfiguration getConfigs() {
        return configuration;
    }

    public final Configuration getConfiguration(final ConfigIndex index) {
        return configuration.getConfiguration(index.ordinal());
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
        String value = configuration.getString(key);
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
    public String[] getConfigArray(final String key)
            throws ConfigNotFoundException {
        String[] values = configuration.getStringArray(key);
        if (values.length == 0) {
            throw new ConfigNotFoundException(key);
        }
        return values;
    }

    // wrapper methods - if key not found returns null and doesn't throw
    // ConfigNotFoundException
    public String getString(final String key) {
        return configuration.getString(key);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        return configuration.getBoolean(key, defaultValue);
    }

    public Object getProperty(final String key) {
        return configuration.getProperty(key);
    }

    public void setProperty(final String key, final Object value) {
        configuration.setProperty(key, value);
    }

    public void addProperty(final String key, final String value) {
        configuration.addProperty(key, value);
    }

    public String getRunnerClass() {
        StackTraceElement[] stackElements =
                Thread.currentThread().getStackTrace();
        StackTraceElement stackElement =
                stackElements[stackElements.length - 1];
        String mainClass = stackElement.getClassName();
        return mainClass;
    }

    // init methods
    public void addRunDate() {
        String runDateStr = configuration.getString("scoopi.runDate"); //$NON-NLS-1$
        if (runDateStr == null) {
            Date runDate = DateUtils.truncate(new Date(), Calendar.SECOND);
            String dateFormat =
                    configuration.getString("scoopi.dateParsePattern"); //$NON-NLS-1$
            runDateStr = DateFormatUtils.format(runDate, dateFormat);
            configuration.addProperty("scoopi.runDate", runDateStr); //$NON-NLS-1$
        }
    }

    public void addRunDateTime() {
        String runDateTimeStr = configuration.getString("scoopi.runDateTime"); //$NON-NLS-1$
        if (runDateTimeStr == null) {
            Date runDateTime = DateUtils.truncate(new Date(), Calendar.SECOND);
            String dateTimeFormat =
                    configuration.getString("scoopi.dateTimeParsePattern"); //$NON-NLS-1$
            runDateTimeStr =
                    DateFormatUtils.format(runDateTime, dateTimeFormat);
            configuration.addProperty("scoopi.runDateTime", runDateTimeStr); //$NON-NLS-1$
        }
    }

    // private methods

    private Configuration getPropertiesConfigs(final String fileName)
            throws ConfigurationException {

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class)
                                .configure(new Parameters().properties()
                                        .setFileName(fileName)
                                        .setThrowExceptionOnMissing(true)
                                        .setListDelimiterHandler(
                                                new DefaultListDelimiterHandler(
                                                        ';')));
        return builder.getConfiguration();
    }

    private Configuration getXMLConfigs(final String fileName)
            throws ConfigurationException {

        FileBasedConfigurationBuilder<XMLConfiguration> builder;
        builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
                XMLConfiguration.class).configure(
                        new Parameters().properties().setFileName(fileName)
                                .setThrowExceptionOnMissing(true)
                                .setListDelimiterHandler(
                                        new DefaultListDelimiterHandler(';')));

        return builder.getConfiguration();
    }

    private String configsAsString(final ConfigIndex index) {
        Configuration config = getConfiguration(index);
        Iterator<String> keys = config.getKeys();

        StringBuilder sb = new StringBuilder();
        sb.append(index);
        sb.append(LINE);
        while (keys.hasNext()) {
            String key = keys.next();
            sb.append(Util.logIndent());
            sb.append(key);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(configuration.getProperty(key));
            sb.append(LINE);
        }
        return sb.toString();
    }

}
