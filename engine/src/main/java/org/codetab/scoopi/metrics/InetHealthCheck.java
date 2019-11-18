package org.codetab.scoopi.metrics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.codahale.metrics.health.HealthCheck;

public class InetHealthCheck extends HealthCheck {

    private String url;

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    protected Result check() throws Exception {
        try {
            final URLConnection conn = new URL(url).openConnection();
            conn.connect();
            conn.getInputStream().close();
            return Result.healthy();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return Result.unhealthy("net is down");
        }
    }
}
