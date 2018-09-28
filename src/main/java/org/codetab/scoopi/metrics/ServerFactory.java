package org.codetab.scoopi.metrics;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerFactory {

    public Server createServer(final int port) {
        return new Server(port);
    }

    public WebAppContext createWebAppContext() {
        return new WebAppContext();
    }
}
