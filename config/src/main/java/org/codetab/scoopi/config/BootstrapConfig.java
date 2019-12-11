package org.codetab.scoopi.config;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

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
public class BootstrapConfig {

    private Properties userDefinedProperties;
    private Properties systemProperties;

    public BootstrapConfig() {
        String userDefinedPropertiesFile =
                new ProvidedProperties().getFileName();
        try (InputStream input = BootstrapConfig.class.getClassLoader()
                .getResourceAsStream(userDefinedPropertiesFile)) {
            userDefinedProperties = new Properties();
            userDefinedProperties.load(input);
        } catch (Exception ex) {
            userDefinedProperties = new Properties();
        }
        systemProperties = System.getProperties();
    }

    /**
     * Whether scoopi.scoopiMode is solo. Considers system property or user
     * property or defaults to solo (in that order).
     * @return true if solo else false
     */
    public boolean isSolo() {
        String userDefinedClusterMode =
                userDefinedProperties.getProperty("scoopi.cluster", "false");
        String sysClusterMode = systemProperties.getProperty("scoopi.cluster");
        if (Objects.nonNull(sysClusterMode)) {
            return !sysClusterMode.equalsIgnoreCase("true");
        } else {
            return !userDefinedClusterMode.equalsIgnoreCase("true");
        }
    }

}
