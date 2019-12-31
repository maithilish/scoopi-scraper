package org.codetab.scoopi.config;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
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
    private ConfigService configService;

    @Inject
    private Configs() {
    }

    public void initConfigService(final String userConfigFile,
            final String defaultConfigFile) {
        configService.init(userConfigFile, defaultConfigFile);
    }

    // general property methods

    /**
     *
     * @param key
     * @return String
     * @throws ConfigNotFoundException
     */
    public String getConfig(final String key) throws ConfigNotFoundException {
        return configService.getConfig(key);
    }

    /**
     *
     * @param key
     * @return array of String
     * @throws ConfigNotFoundException
     */
    public String[] getConfigArray(final String key)
            throws ConfigNotFoundException {
        return configService.getConfigArray(key);
    }

    /**
     * This method returns boolean for a key and if not found then false;
     * @param configKey
     * @return
     */
    public boolean isTrue(final String configKey) {
        return configService.getBoolean(configKey, false);
    }

    /**
     * This method returns boolean for a key and if not found then true;
     * @param configKey
     * @return
     */
    public boolean getBoolean(final String configKey) {
        return configService.getBoolean(configKey, true);
    }

    public Object getProperty(final String configKey) {
        return configService.getProperty(configKey);
    }

    public void setProperty(final String key, final Object value) {
        configService.setProperty(key, value);
    }

    // specific property methods

    public Date getRunDate() {
        final String key = "scoopi.parsed.runDate";
        Date runDate = (Date) configService.getProperty(key);
        if (isNull(runDate)) {
            try {
                final String dateStr = getConfig("scoopi.runDate"); //$NON-NLS-1$
                final String patterns = getConfig("scoopi.dateParsePattern"); //$NON-NLS-1$
                runDate = DateUtils.parseDate(dateStr, new String[] {patterns});
                configService.setProperty(key, runDate);
            } catch (ParseException | ConfigNotFoundException e) {
                throw new CriticalException("unable to parse runDate", e);
            }
        }
        return runDate;
    }

    public Date getRunDateTime() {
        final String key = "scoopi.parsed.runDateTime";
        Date runDateTime = (Date) configService.getProperty(key);
        if (isNull(runDateTime)) {
            try {
                final String dateTimeStr = getConfig("scoopi.runDateTime"); //$NON-NLS-1$
                final String patterns =
                        getConfig("scoopi.dateTimeParsePattern"); //$NON-NLS-1$
                runDateTime = DateUtils.parseDate(dateTimeStr,
                        new String[] {patterns});
                configService.setProperty(key, runDateTime);
            } catch (ParseException | ConfigNotFoundException e) {
                throw new CriticalException("unable to parse runDateTime", e);
            }
        }
        return runDateTime;
    }

    public Date getHighDate() {
        final String key = "scoopi.parsed.runHighDate";
        Date highDate = (Date) configService.getProperty(key);
        if (isNull(highDate)) {
            try {
                final String dateStr = getConfig("scoopi.highDate"); //$NON-NLS-1$
                final String patterns =
                        getConfig("scoopi.dateTimeParsePattern"); //$NON-NLS-1$
                highDate =
                        DateUtils.parseDate(dateStr, new String[] {patterns});
                configService.setProperty(key, highDate);
            } catch (ParseException | ConfigNotFoundException e) {
                throw new CriticalException("unable to parse highDate", e);
            }
        }
        return highDate;
    }

    public boolean isCluster() {
        return configService.getBoolean("scoopi.cluster.enable", false);
    }

    public boolean isTestMode() {
        final String eclipseTestRunner =
                "org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"; //$NON-NLS-1$
        final String mavenTestRunner =
                "org.apache.maven.surefire.booter.ForkedBooter"; //$NON-NLS-1$

        final String runnerClass = configService.getRunnerClass();
        if (runnerClass.equals(mavenTestRunner)
                || runnerClass.equals(eclipseTestRunner)) {
            return true;
        }
        return false;
    }

    public boolean isDevMode() {
        return StringUtils.equalsIgnoreCase(
                configService.getString("scoopi.mode"), //$NON-NLS-1$
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
        return configService.getBoolean(configKey, true);
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
        return configService.getBoolean(configKey, true);
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
            timeout = Integer.parseInt(configService.getConfig(key));
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
            userAgent = configService.getConfig(key);
        } catch (final ConfigNotFoundException e) {
            final String message = spaceit("config not found:", key,
                    ", defaults to: ", userAgent);
            LOGGER.debug(marker, "{}, {}", e, message);
        }
        return userAgent;
    }

}
