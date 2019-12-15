package org.codetab.scoopi.config;

/**
 * Helper for User Defined Properties file.
 *
 * @author m
 *
 *
 */
public class ProvidedProperties {

    public String getFileName() {
        String fileName = null;

        String sysFileName = System.getProperty("scoopi.propertyFile"); //$NON-NLS-1$
        if (sysFileName != null) {
            fileName = sysFileName;
        }

        if (fileName == null) {
            String mode = System.getProperty("scoopi.mode", "prod");
            if (mode.equalsIgnoreCase("dev")) {
                fileName = "scoopi-dev.properties";
            }
        }

        if (fileName == null) {
            fileName = System.getenv("scoopi_property_file"); //$NON-NLS-1$
        }

        // default nothing is set then production property file
        if (fileName == null) {
            fileName = "scoopi.properties"; //$NON-NLS-1$
        }
        return fileName;
    }
}
