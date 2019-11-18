package org.codetab.scoopi.config;

import static org.codetab.scoopi.util.Util.spaceit;

import javax.inject.Inject;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class ConfigHelper {

    @Inject
    private ConfigService configService;

    /**
     * default timeout value in ms.
     */
    private static final int TIMEOUT_MILLIS = 120000;

    private Marker marker;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConfigHelper.class);

    public int getTimeout() {
        int timeout = TIMEOUT_MILLIS;
        String key = "scoopi.webClient.timeout";
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (ConfigNotFoundException e) {
            String message = spaceit("config not found:", key,
                    ", defaults to: ", String.valueOf(timeout), "millis");
            LOGGER.debug(marker, "{}, {}", e, message);
        } catch (NumberFormatException e) {
            String message = spaceit("config:", key, ", defaults to: ",
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
        String key = "scoopi.webClient.userAgent";
        try {
            userAgent = configService.getConfig(key);
        } catch (ConfigNotFoundException e) {
            String message = spaceit("config not found:", key,
                    ", defaults to: ", userAgent);
            LOGGER.debug(marker, "{}, {}", e, message);
        }
        return userAgent;
    }

}
