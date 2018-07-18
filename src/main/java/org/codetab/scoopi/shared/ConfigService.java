package org.codetab.scoopi.shared;

import java.text.ParseException;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConfigService {

    enum ConfigIndex {
        SYSTEM, PROVIDED, DEFAULTS
    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConfigService.class);

    private CompositeConfiguration configs;

    @Inject
    private ConfigService() {
    }

    public void init(final String userProvidedFile, final String defaultsFile) {
        LOGGER.info(Messages.getString("ConfigService.0")); //$NON-NLS-1$

        configs = new CompositeConfiguration();

        SystemConfiguration systemConfigs = new SystemConfiguration();
        configs.addConfiguration(systemConfigs);

        try {
            Configuration userProvided = getPropertiesConfigs(userProvidedFile);
            configs.addConfiguration(userProvided);
        } catch (ConfigurationException e) {
            configs.addConfiguration(new PropertiesConfiguration());
            LOGGER.info(e.getLocalizedMessage() + ". " //$NON-NLS-1$
                    + Messages.getString("ConfigService.2")); //$NON-NLS-1$
        }

        try {
            Configuration defaults = getXMLConfigs(defaultsFile);
            configs.addConfiguration(defaults);
        } catch (ConfigurationException e) {
            throw new CriticalException(Messages.getString("ConfigService.3"), //$NON-NLS-1$
                    e);
        }

        addRunDate();
        addRunDateTime();

        LOGGER.trace("{}", configsAsString(ConfigIndex.SYSTEM)); //$NON-NLS-1$
        LOGGER.debug("{}", configsAsString(ConfigIndex.PROVIDED)); //$NON-NLS-1$
        LOGGER.debug("{}", configsAsString(ConfigIndex.DEFAULTS)); //$NON-NLS-1$
        LOGGER.debug(Messages.getString("ConfigService.7")); //$NON-NLS-1$

        LOGGER.info(Messages.getString("ConfigService.8")); //$NON-NLS-1$
        LOGGER.info(Messages.getString("ConfigService.9")); //$NON-NLS-1$
    }

    // when config not found, default value may be used in some cases
    // otherwise usually exceptionRule is translated to higher level
    // CriticalException
    // which is unrecoverable. Hence warn is used in here instead of error
    public String getConfig(final String key) throws ConfigNotFoundException {
        String value = configs.getString(key);
        if (value == null) {
            LOGGER.warn(Messages.getString("ConfigService.10"), key); //$NON-NLS-1$
            throw new ConfigNotFoundException(key);
        }
        return value;
    }

    public String[] getConfigArray(final String key)
            throws ConfigNotFoundException {
        String[] values = configs.getStringArray(key);
        if (values.length == 0) {
            LOGGER.warn(Messages.getString("ConfigService.11"), key); //$NON-NLS-1$
            throw new ConfigNotFoundException(key);
        }
        return values;
    }

    public final CompositeConfiguration getConfigs() {
        return configs;
    }

    protected void setConfigs(final CompositeConfiguration configs) {
        this.configs = configs;
    }

    public final Configuration getConfiguration(final ConfigIndex index) {
        return configs.getConfiguration(index.ordinal());
    }

    public Date getRunDate() {
        try {
            Date runDate = null;
            String dateStr = getConfig("scoopi.runDate"); //$NON-NLS-1$
            String patterns = getConfig("scoopi.dateParsePattern"); //$NON-NLS-1$
            runDate = DateUtils.parseDate(dateStr, new String[] {patterns});
            return runDate;
        } catch (ParseException | ConfigNotFoundException e) {
            throw new CriticalException(Messages.getString("ConfigService.12"), //$NON-NLS-1$
                    e);
        }
    }

    public Date getRunDateTime() {
        try {
            Date runDateTime = null;
            String dateTimeStr = getConfig("scoopi.runDateTime"); //$NON-NLS-1$
            String patterns = getConfig("scoopi.dateTimeParsePattern"); //$NON-NLS-1$
            runDateTime =
                    DateUtils.parseDate(dateTimeStr, new String[] {patterns});
            return runDateTime;
        } catch (ParseException | ConfigNotFoundException e) {
            throw new CriticalException(Messages.getString("ConfigService.13"), //$NON-NLS-1$
                    e);
        }
    }

    public Date getHighDate() {
        try {
            Date highDate = null;
            String dateStr = getConfig("scoopi.highDate"); //$NON-NLS-1$
            String[] patterns = getConfigArray("scoopi.dateTimeParsePattern"); //$NON-NLS-1$
            highDate = DateUtils.parseDate(dateStr, patterns);
            return highDate;
        } catch (ParseException | ConfigNotFoundException e) {
            throw new CriticalException(Messages.getString("ConfigService.14"), //$NON-NLS-1$
                    e);
        }

    }

    public ORM getOrmType() {
        ORM orm = ORM.JDO;
        try {
            String ormName = getConfig("scoopi.datastore.orm"); //$NON-NLS-1$
            if (StringUtils.compareIgnoreCase(ormName, "jdo") == 0) { //$NON-NLS-1$
                orm = ORM.JDO;
            }
            if (StringUtils.compareIgnoreCase(ormName, "jpa") == 0) { //$NON-NLS-1$
                orm = ORM.JPA;
            }
        } catch (ConfigNotFoundException e) {
            LOGGER.error("{}", e.getMessage()); //$NON-NLS-1$
            LOGGER.trace("", e); //$NON-NLS-1$
        }
        return orm;
    }

    public boolean isTestMode() {
        StackTraceElement[] stackElements =
                Thread.currentThread().getStackTrace();
        StackTraceElement stackElement =
                stackElements[stackElements.length - 1];
        String mainClass = stackElement.getClassName();
        String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"; //$NON-NLS-1$
        String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter"; //$NON-NLS-1$
        if (mainClass.equals(mavenTestRunner)) {
            return true;
        }
        if (mainClass.equals(eclipseTestRunner)) {
            return true;
        }
        return false;
    }

    public boolean isDevMode() {
        return StringUtils.equalsIgnoreCase(configs.getString("scoopi.mode"), //$NON-NLS-1$
                "dev"); //$NON-NLS-1$
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

    private void addRunDate() {
        Date runDate = DateUtils.truncate(new Date(), Calendar.SECOND);
        String runDateStr = configs.getString("scoopi.runDate"); //$NON-NLS-1$
        if (runDateStr == null) {
            String dateFormat = configs.getString("scoopi.dateParsePattern"); //$NON-NLS-1$
            runDateStr = DateFormatUtils.format(runDate, dateFormat);
        }
        configs.addProperty("scoopi.runDate", runDateStr); //$NON-NLS-1$
    }

    private void addRunDateTime() {
        Date runDateTime = DateUtils.truncate(new Date(), Calendar.SECOND);
        String runDateTimeStr = configs.getString("scoopi.runDateTime"); //$NON-NLS-1$
        if (runDateTimeStr == null) {
            String dateTimeFormat =
                    configs.getString("scoopi.dateTimeParsePattern"); //$NON-NLS-1$
            runDateTimeStr =
                    DateFormatUtils.format(runDateTime, dateTimeFormat);
        }
        configs.addProperty("scoopi.runDateTime", runDateTimeStr); //$NON-NLS-1$
    }

    private String configsAsString(final ConfigIndex index) {
        Configuration config = getConfiguration(index);
        Iterator<String> keys = config.getKeys();

        StringBuilder sb = new StringBuilder();
        sb.append(index);
        sb.append(System.lineSeparator());
        while (keys.hasNext()) {
            String key = keys.next();
            sb.append(Util.logIndent());
            sb.append(key);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(configs.getProperty(key));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Return effective persist for a type based on user provided config.
     * <p>
     * User can define persist config scoopi.useDatastore=true|false or for a
     * type as scoopi.persist.locator=true|false in config file
     * scoopi.properties.
     * </p>
     * <p>
     * This method returns boolean for a key and if not found then true;
     * </p>
     * @param key
     *            scoopi.useDatastore, scoopi.persist.locator|data|datadef
     * @return
     */
    public boolean isPersist(final String configKey) {
        return configs.getBoolean(configKey, true);
    }

    public boolean useDataStore() {
        String configKey = "scoopi.useDatastore";
        return configs.getBoolean(configKey, true);
    }
}
