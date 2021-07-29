package org.codetab.scoopi.helper;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

class HttpFactory {

    public CloseableHttpClient getHttpClient(final String userAgent,
            final int timeout) {
        return HttpClientBuilder.create().setUserAgent(userAgent)
                .setConnectionTimeToLive(timeout, TimeUnit.MILLISECONDS)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
    }

    public HttpGet httpGet(final String url) {
        return new HttpGet(url);
    }
}
