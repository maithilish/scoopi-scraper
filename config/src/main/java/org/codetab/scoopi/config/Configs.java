package org.codetab.scoopi.config;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

@Singleton
public class Configs {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configs.class);

    private static final int TIMEOUT_MILLIS = 120000;

    private Marker marker;

    @Inject
    private ConfigProperties configProperties;

    /**
     *
     * @param key
     * @return String
     * @throws ConfigNotFoundException
     */
    public String getConfig(final String key) throws ConfigNotFoundException {
        return configProperties.getProperty(key);
    }

    public String getConfig(final String key, final String defaultValue) {
        try {
            return configProperties.getProperty(key);
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

    /**
     * This method returns boolean for a key and if not found then false;
     * @param configKey
     * @return
     */
    public boolean isTrue(final String configKey) {
        return configProperties.getBoolean(configKey, false);
    }

    /**
     * This method returns boolean for a key and if not found then true;
     * @param configKey
     * @return
     */
    public boolean getBoolean(final String configKey) {
        return configProperties.getBoolean(configKey, true);
    }

    public Object getProperty(final String configKey) {
        return configProperties.get(configKey);
    }

    public void setProperty(final String key, final Object value) {
        configProperties.put(key, value);
    }

    // specific property methods
    public Date getRunDate() {
        Date runDate = (Date) configProperties.get("scoopi.runDate");
        if (isNull(runDate)) {
            throw new CriticalException("unable to get runDate");
        }
        return runDate;
    }

    public String getRunDateString() {
        final String key = "scoopi.runDateString";
        try {
            return configProperties.getProperty(key);
        } catch (ConfigNotFoundException e) {
            throw new CriticalException("unable to get runDateString", e);
        }
    }

    public Date getRunDateTime() {
        Date runDateTime = (Date) configProperties.get("scoopi.runDateTime");

        if (isNull(runDateTime)) {
            throw new CriticalException("unable to get runDateTime");
        }
        return runDateTime;
    }

    public String getRunDateTimeString() {
        final String key = "scoopi.runDateTimeString";
        try {
            return configProperties.getProperty(key);
        } catch (ConfigNotFoundException e) {
            throw new CriticalException("unable to get runDateTimeString", e);
        }
    }

    public Date getHighDate() {
        return (Date) configProperties.get("scoopi.highDate");
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
                    configProperties.getProperty("scoopi.runnerClass");
            if (runnerClass.equals(mavenTestRunner)
                    || runnerClass.equals(eclipseTestRunner)) {
                return true;
            }
        } catch (ConfigNotFoundException e) {
        }
        return false;
    }

    public boolean isDevMode() {
        return StringUtils.equalsIgnoreCase(
                configProperties.getProperty("scoopi.mode", ""), //$NON-NLS-1$
                "dev"); //$NON-NLS-1$
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

    // FIXME - testfix, below methods needs some cleanup, above cleaned up
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

    public int getTimeout() {
        int timeout = TIMEOUT_MILLIS;
        final String key = "scoopi.webClient.timeout";
        try {
            timeout = Integer.parseInt(configProperties.getProperty(key));
        } catch (final ConfigNotFoundException e) {
            final String message = spaceit("config not found:", key,
                    ", defaults to: ", String.valueOf(timeout), "millis");
            LOGGER.debug(marker, "{}, {}", e, message);
        } catch (final NumberFormatException e) {
            final String message = spaceit("config:", key, ", defaults to: ",
                    String.valueOf(timeout), "millis");
            LOGGER.error(marker, "{}, {}", e, message);
        }
        return timeout;
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
        String userAgent =
                "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0"; //$NON-NLS-1$
        final String key = "scoopi.webClient.userAgent";
        try {
            userAgent = configProperties.getProperty(key);
        } catch (final ConfigNotFoundException e) {
            final String message = spaceit("config not found:", key,
                    ", defaults to: ", userAgent);
            LOGGER.debug(marker, "{}, {}", e, message);
        }
        return userAgent;
    }

    public boolean isMetricsServerEnabled() {
        boolean enabled = false;
        try {
            enabled = Boolean.parseBoolean(configProperties
                    .getProperty("scoopi.metrics.server.enable"));
        } catch (final ConfigNotFoundException e1) {
        }
        if (configProperties.getBoolean("scoopi.cluster.enable", false)) {
            // by default metrics server is disabled in cluster and enabled only
            // through system property
            enabled = Boolean.parseBoolean(System
                    .getProperty("scoopi.metrics.server.enable", "false"));
        }
        return enabled;
    }
}
