package org.codetab.scoopi.config;

import static java.util.Objects.isNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;

@Singleton
public class Configs {

    @Inject
    private ConfigProperties configProperties;

    /**
     *
     * @param key
     * @return String
     * @throws ConfigNotFoundException
     */
    public String getConfig(final String key) throws ConfigNotFoundException {
        return configProperties.getConfig(key);
    }

    public String getConfig(final String key, final String defaultValue) {
        try {
            return configProperties.getConfig(key);
        } catch (ConfigNotFoundException e) {
            return defaultValue;
        }
    }

    /**
     *
     * @param key
     * @return array of String
     * @throws ConfigNotFoundException
     */
    public String[] getConfigArray(final String key)
            throws ConfigNotFoundException {
        return configProperties.getStringArray(key);
    }

    public boolean getBoolean(final String configKey,
            final boolean defaultValue) {
        return configProperties.getBoolean(configKey, defaultValue);
    }

    public int getInt(final String configKey, final int defaultValue) {
        return configProperties.getInt(configKey, defaultValue);
    }

    public int getInt(final String configKey, final String defaultValue) {
        return configProperties.getInt(configKey,
                Integer.parseInt(defaultValue));
    }

    public Object getProperty(final String configKey) {
        return configProperties.get(configKey);
    }

    public void setProperty(final String key, final Object value) {
        configProperties.put(key, value);
    }

    // specific property methods
    public ZonedDateTime getRunDate() {
        ZonedDateTime runDate =
                (ZonedDateTime) configProperties.get("scoopi.runDate");
        if (isNull(runDate)) {
            throw new CriticalException("unable to get runDate");
        }
        return runDate;
    }

    public String getRunDateText() {
        final String key = "scoopi.runDateText";
        try {
            return configProperties.getConfig(key);
        } catch (ConfigNotFoundException e) {
            throw new CriticalException("unable to get runDateText", e);
        }
    }

    public ZonedDateTime getRunDateTime() {
        ZonedDateTime runDateTime =
                (ZonedDateTime) configProperties.get("scoopi.runDateTime");

        if (isNull(runDateTime)) {
            throw new CriticalException("unable to get runDateTime");
        }
        return runDateTime;
    }

    public String getRunDateTimeTextg() {
        final String key = "scoopi.runDateTimeText";
        try {
            return configProperties.getConfig(key);
        } catch (ConfigNotFoundException e) {
            throw new CriticalException("unable to get runDateTimeText", e);
        }
    }

    public DateTimeFormatter getDateTimeFormatter() {
        DateTimeFormatter formatter;
        String pattern;
        try {
            final String key = "scoopi.dateTimePattern";
            pattern = configProperties.getConfig(key);
            formatter = DateTimeFormatter.ofPattern(pattern);

            // FIXME - datefix, what is outcome
            if (isNull(formatter.getZone())) {
                formatter = formatter.withZone(ZoneId.systemDefault());
            }
        } catch (ConfigNotFoundException e) {
            formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        }
        return formatter;
    }

    public ZonedDateTime getHighDate() {
        return (ZonedDateTime) configProperties.get("scoopi.highDate");
    }

    public boolean isCluster() {
        return configProperties.getBoolean("scoopi.cluster.enable", false);
    }

    public boolean isTestMode() {
        final String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"; //$NON-NLS-1$
        final String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter"; //$NON-NLS-1$
        try {
            String runnerClass =
                    configProperties.getConfig("scoopi.runnerClass");
            if (runnerClass.equals(mavenTestRunner)
                    || runnerClass.equals(eclipseTestRunner)) {
                return true;
            }
        } catch (ConfigNotFoundException e) {
        }
        return false;
    }

    public boolean isDevMode() {
        try {
            String mode = configProperties.getConfig("scoopi.mode");
            return StringUtils.equalsIgnoreCase(mode, "dev");
        } catch (ConfigNotFoundException e) {
            return false;
        }
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
     *            scoopi.persist.locator|data|datadef
     * @return value of key and if not found then true
     */
    public boolean isPersist(final String configKey) {
        return configProperties.getBoolean(configKey, true);
    }

    /**
     *
     * <p>
     * Return value for the key scoopi.useDatastore
     * </p>
     * <p>
     * This method returns for the key scoopi.useDatastore and if not found then
     * true;
     * </p>
     * @param key
     *            scoopi.useDatastore
     * @return value of key and if not found then true
     */
    public boolean useDataStore() {
        final String configKey = "scoopi.useDatastore";
        return configProperties.getBoolean(configKey, true);
    }

    public String getStage() {
        String modeInfo = "stage: production";
        if (isTestMode()) {
            modeInfo = "stage: test";
        }
        if (isDevMode()) {
            modeInfo = "stage: dev";
        }
        return modeInfo;
    }

    public int getWebClientTimeout() {
        final int defaultTimeout = 120000;
        return configProperties.getInt("scoopi.webClient.timeout",
                defaultTimeout);
    }

    /**
     * <p>
     * User Agent string used for request.
     * <p>
     * default value
     * <p>
     * Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0
     * <p>
     * configurable using config key - scoopi.webClient.userAgent
     * @return user agent string
     */
    public String getUserAgent() {
        String defaultUserAgent =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0"; //$NON-NLS-1$
        String key = "scoopi.webClient.userAgent";
        try {
            return configProperties.getConfig(key);
        } catch (ConfigNotFoundException e) {
            return defaultUserAgent;
        }

    }

    public boolean isMetricsServerEnabled() {
        boolean enabled = Boolean.parseBoolean(
                System.getProperty("scoopi.metrics.server.enable", "false"));

        if (configProperties.getBoolean("scoopi.cluster.enable", false)) {
            // by default metrics server is disabled in cluster and enabled only
            // through system property
            enabled = Boolean.parseBoolean(System
                    .getProperty("scoopi.metrics.server.enable", "false"));
        }
        return enabled;
    }
}
