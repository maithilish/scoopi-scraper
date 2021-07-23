package org.codetab.scoopi.config;

import static java.util.Objects.isNull;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

/**
 * Bare minimum configuration required to start Scoopi. It is used before DI
 * exists. After DI activation, ConfigService provides configurations.
 *
 * Only system and user defined properties are allowed; former takes precedence.
 * Default values are hard coded in the methods.
 *
 * @author m
 *
 */
public class BootConfigs {

    private Properties userDefinedProperties;
    private Properties systemProperties;

    public BootConfigs() {
        final String userDefinedPropertiesFile =
                new PropertyFiles().getFileName();
        try (InputStream input = BootConfigs.class.getClassLoader()
                .getResourceAsStream(userDefinedPropertiesFile)) {
            userDefinedProperties = new Properties();
            userDefinedProperties.load(input);
        } catch (final Exception ex) {
            userDefinedProperties = new Properties();
        }
        systemProperties = System.getProperties();
    }

    public void configureLogPath() {
        String logKey = "scoopi.log.dir";
        String logPath = getConfig(logKey, "logs");
        if (!isSolo()) {
            Boolean splitLogs = Boolean.valueOf(
                    getConfig("scoopi.cluster.log.path.suffixUid", "true"));
            if (splitLogs) {
                String uuid = UUID.randomUUID().toString();
                String upart = uuid.split("-")[0];
                logPath = FilenameUtils
                        .separatorsToSystem(String.join("/", logPath, upart));
            }
        }
        System.setProperty(logKey, logPath);
    }

    public String getConfig(final String key, final String defaultValue) {
        String value = systemProperties.getProperty(key);
        if (isNull(value)) {
            value = userDefinedProperties.getProperty(key);
        }
        if (isNull(value)) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Whether scoopi.scoopiMode is solo. Considers system property or user
     * property or defaults to solo (in that order).
     * @return true if solo else false
     */
    public boolean isSolo() {
        final String userDefinedClusterMode = userDefinedProperties
                .getProperty("scoopi.cluster.enable", "false");
        final String sysClusterMode =
                systemProperties.getProperty("scoopi.cluster.enable");
        if (Objects.nonNull(sysClusterMode)) {
            return !sysClusterMode.equalsIgnoreCase("true");
        } else {
            return !userDefinedClusterMode.equalsIgnoreCase("true");
        }
    }

    /**
     * Used by unit tests, package private
     * @param key
     * @param value
     */
    void setUserDefinedProperty(final String key, final String value) {
        userDefinedProperties.setProperty(key, value);
    }
}
