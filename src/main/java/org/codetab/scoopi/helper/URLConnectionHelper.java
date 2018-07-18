package org.codetab.scoopi.helper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.google.common.net.UrlEscapers;

/**
 * <p>
 * Helper for URLConnection. It is not possible to mock URL for tests so
 * extracted to this wrapper.
 * @author Maithilish
 *
 */
public class URLConnectionHelper {

    /**
     * <p>
     * Get connection for URL.
     * @param urlSpec
     *            web address or file path.
     * @return connection for the URL
     * @throws IOException
     *             if IO error
     * @throws MalformedURLException
     *             if url string is invalid
     */
    public URLConnection getURLConnection(final String urlSpec)
            throws IOException, MalformedURLException {
        URL url = new URL(urlSpec);
        URLConnection uc = url.openConnection();
        return uc;
    }

    /**
     * <p>
     * Sets request property. It is not possible to get the property value back
     * after connected and for tests, this method is useful.
     * @param uc
     *            connection
     * @param key
     *            property key
     * @param value
     *            property value
     */
    public void setRequestProperty(final URLConnection uc, final String key,
            final String value) {
        uc.setRequestProperty(key, value);
    }

    /**
     * Get protocol [http,https,ftp, file or resource] from urlSpec. In case, no
     * protocol is specified or urlSpec is malformed then returns protocol name
     * as resource.
     * @param urlSpec
     * @return protocol as returned by URL class or resource when no protocol is
     *         specified or urlSpec is malformed.
     */
    public String getProtocol(final String urlSpec) {
        try {
            URL url = new URL(urlSpec);
            return url.getProtocol();
        } catch (MalformedURLException e) {
            return "resource";
        }
    }

    /**
     * Get content from the URLConnection as byte[]
     * @param uc
     *            URLConnection
     * @return content byte[]
     * @throws IOException
     *             on error
     */
    public byte[] getContent(final URLConnection uc) throws IOException {
        return IOUtils.toByteArray(uc);
    }

    /**
     * Escape URL string - space escaped as %20
     * @param urlSpec
     *            URL string
     * @return escaped URL string
     */
    public String escapeUrl(final String urlSpec) {
        String urlSpecEscaped = urlSpec;
        if (!UrlValidator.getInstance().isValid(urlSpec)) {
            urlSpecEscaped = UrlEscapers.urlFragmentEscaper().escape(urlSpec);
        }
        return urlSpecEscaped;
    }

}
