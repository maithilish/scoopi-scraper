package org.codetab.scoopi.step.extract;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.helper.URLConnectionHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.step.base.BaseLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Create or loads document from persistence store and uses URL to fetch
 * document contents from web or file system.
 *
 * @author Maithilish
 *
 */
public final class PageLoader extends BaseLoader {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PageLoader.class);

    /**
     * default timeout value in ms.
     */
    private static final int TIMEOUT_MILLIS = 120000;

    /**
     * Helper to handle URLConnection.
     */
    @Inject
    private URLConnectionHelper ucHelper;
    @Inject
    private MetricsHelper metricsHelper;

    /**
     * Fetch document content from web, file system or classpath using the URL
     * and convert it to byte array. Where urlspec
     * <ul>
     * <li>starts with http or https - fetch from web</li>
     * <li>starts with file - fetch from file system, path can be abs or
     * relative</li>
     * <li>path without protocol - loads from classpath</li>
     * </ul>
     * @param urlSpec
     *            URL string
     * @return byte[] document content fetched from web, file system or
     *         classpath
     * @see org.codetab.scoopi.step.base.BaseLoader#fetchDocumentObject(String)
     */
    @Override
    public byte[] fetchDocumentObject(final String urlSpec) throws IOException {
        // TODO charset encoding
        byte[] bytes = null;

        String protocol = ucHelper.getProtocol(urlSpec);
        if (protocol.equals("resource")) {
            LOGGER.info(marker, "fetch resource: {}", urlSpec);
            try {
                URL fileURL = PageLoader.class.getResource(urlSpec);
                bytes = IOUtils.toByteArray(fileURL);
                metricsHelper.getCounter(this, "fetch", "resource").inc();
                LOGGER.debug(marker, "fetched resource: {}", urlSpec);
                return bytes;
            } catch (IOException e1) {
                throw new IOException(spaceit("file not found: ", urlSpec));
            }
        }

        if (protocol.equals("file")) {
            LOGGER.info(marker, "fetch file: {}", urlSpec);
            try {
                URL fileURL = new URL(urlSpec);
                bytes = IOUtils.toByteArray(fileURL);
                metricsHelper.getCounter(this, "fetch", "file").inc();
                LOGGER.debug(marker, "fetched file: {}", urlSpec);
                return bytes;
            } catch (IOException | NullPointerException e) {
                throw new IOException(spaceit("file not found: ", urlSpec));
            }
        }

        if (protocol.equals("http") || protocol.equals("https")) {

            String urlSpecEscaped = ucHelper.escapeUrl(urlSpec);
            LOGGER.info(marker, "fetch web resource: {}", urlSpecEscaped);

            HttpURLConnection uc = (HttpURLConnection) ucHelper
                    .getURLConnection(urlSpecEscaped);
            int timeout = getTimeout();
            uc.setConnectTimeout(timeout);
            uc.setReadTimeout(timeout);
            ucHelper.setRequestProperty(uc, "User-Agent", getUserAgent());

            uc.connect();
            int respCode = uc.getResponseCode();
            if (respCode != HttpURLConnection.HTTP_OK) {
                throw new IOException(spaceit(
                        "HTTP response:" + respCode + ", URL:", urlSpec));
            }

            bytes = ucHelper.getContent(uc);
            metricsHelper.getCounter(this, "fetch", "web").inc();
            LOGGER.debug(marker, "fetched: {}, length: {}", urlSpecEscaped,
                    bytes.length);
            return bytes;
        }

        throw new IOException(spaceit("unknown protocol:", urlSpec));
    }

    /**
     * <p>
     * Timeout value (in ms) for connection and read time out.
     * <p>
     * default value - 120000 ms
     * <p>
     * configurable using config key - scoopi.webClient.timeout
     *
     * @return timeout value
     */
    private int getTimeout() {
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
    private String getUserAgent() {
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
