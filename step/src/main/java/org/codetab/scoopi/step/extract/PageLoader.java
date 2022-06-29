package org.codetab.scoopi.step.extract;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.helper.HttpHelper;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.step.base.BaseLoader;

/**
 * <p>
 * Create or loads document from persistence store and uses URL to fetch
 * document contents from web or file system.
 *
 * @author Maithilish
 *
 */
public class PageLoader extends BaseLoader {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Helper to handle URLConnection.
     */
    @Inject
    private HttpHelper httpHelper;
    @Inject
    private IOHelper ioHelper;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private Configs configs;

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

        // TODO if doc not found, show helpful log
        final String protocol = httpHelper.getProtocol(urlSpec);
        if (protocol.equals("resource")) {
            LOG.info(jobMarker, "fetch resource: {}", urlSpec);
            try {
                final URL fileURL = ioHelper.getResourceURL(urlSpec);
                bytes = ioHelper.toByteArray(fileURL);
                metricsHelper.getCounter(this, "fetch", "resource").inc();
                LOG.debug(jobMarker, "fetched resource: {}", urlSpec);
                return bytes;
            } catch (final IOException | NullPointerException e1) {
                throw new IOException(spaceit("file not found: ", urlSpec));
            }
        }

        if (protocol.equals("file")) {
            LOG.info(jobMarker, "fetch file: {}", urlSpec);
            try {
                final URL fileURL = ioHelper.getURLFromSpec(urlSpec);
                bytes = ioHelper.toByteArray(fileURL);
                metricsHelper.getCounter(this, "fetch", "file").inc();
                LOG.debug(jobMarker, "fetched file: {}", urlSpec);
                return bytes;
            } catch (IOException | NullPointerException e) {
                throw new IOException(spaceit("file not found: ", urlSpec));
            }
        }

        if (protocol.equals("http") || protocol.equals("https")) {

            final int timeout = configs.getWebClientTimeout();
            final String userAgent = configs.getUserAgent();

            final String urlSpecEscaped = httpHelper.escapeUrl(urlSpec);
            LOG.info(jobMarker, "fetch web resource: {}", urlSpecEscaped);

            bytes = httpHelper.getContent(urlSpecEscaped, userAgent, timeout);

            metricsHelper.getCounter(this, "fetch", "web").inc();
            LOG.debug(jobMarker, "fetched: {}, length: {}", urlSpecEscaped,
                    bytes.length);
            return bytes;
        }

        throw new IOException(spaceit("unknown protocol:", urlSpec));
    }

}
