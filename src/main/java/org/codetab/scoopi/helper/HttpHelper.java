package org.codetab.scoopi.helper;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.common.net.UrlEscapers;

public class HttpHelper {

    /**
     * Get web page content using Apache HttpClient. Takes care of page
     * redirects.
     * @param url
     * @param userAgent
     * @param timeout
     * @return bytes
     * @throws IOException
     */
    public byte[] getContent(final String url, final String userAgent,
            final int timeout) throws IOException {
        // integration test
        byte[] content = new byte[0];
        CloseableHttpClient client =
                HttpClientBuilder.create().setUserAgent(userAgent)
                        .setConnectionTimeToLive(timeout, TimeUnit.MILLISECONDS)
                        .build();
        HttpGet httpGet = new HttpGet(url);
        new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            int code = response.getStatusLine().getStatusCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException(
                        spaceit("HTTP response:" + code + ", URL:", url));
            }
            HttpEntity entity = response.getEntity();
            if (isNull(entity)) {
                throw new IOException(
                        spaceit("No response from server,", "URL:", url));
            } else {
                try (InputStream in = entity.getContent()) {
                    content = IOUtils.toByteArray(in);
                }
            }
        }
        return content;
    }

    public String escapeUrl(final String urlSpec) {
        if (UrlValidator.getInstance().isValid(urlSpec)) {
            return urlSpec;
        } else {
            return UrlEscapers.urlFragmentEscaper().escape(urlSpec);
        }
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

}
