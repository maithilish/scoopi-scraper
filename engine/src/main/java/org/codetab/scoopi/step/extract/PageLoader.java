package org.codetab.scoopi.step.extract;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.codetab.scoopi.helper.HttpHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.step.base.BaseLoader;
import org.codetab.scoopi.system.ConfigHelper;
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
public class PageLoader extends BaseLoader {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PageLoader.class);

    /**
     * Helper to handle URLConnection.
     */
    @Inject
    private HttpHelper httpHelper;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private ConfigHelper configHelper;

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

        String protocol = httpHelper.getProtocol(urlSpec);
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

            int timeout = configHelper.getTimeout();
            String userAgent = configHelper.getUserAgent();

            String urlSpecEscaped = httpHelper.escapeUrl(urlSpec);
            LOGGER.info(marker, "fetch web resource: {}", urlSpecEscaped);

            bytes = httpHelper.getContent(urlSpecEscaped, userAgent, timeout);

            metricsHelper.getCounter(this, "fetch", "web").inc();
            LOGGER.debug(marker, "fetched: {}, length: {}", urlSpecEscaped,
                    bytes.length);
            return bytes;
        }

        throw new IOException(spaceit("unknown protocol:", urlSpec));
    }

}
