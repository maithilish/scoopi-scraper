package org.codetab.scoopi.metrics;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MetricsServer {

    static final Logger LOGGER = LoggerFactory.getLogger(MetricsServer.class);

    @Inject
    private Configs configs;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private ServerFactory factory;

    private Server server;

    public void start() {
        if (!isEnable()) {
            LOGGER.info("metrics server is disabled");
            return;
        }

        int port;
        try {
            port = Integer
                    .parseInt(configs.getConfig("scoopi.metrics.server.port"));
        } catch (ConfigNotFoundException | NumberFormatException e) {
            port = Integer.parseInt("9010");
        }

        try {
            String webappBase = metricsHelper.getURL("/webapp").toString();
            String descriptorPath =
                    String.join("/", webappBase, "WEB-INF/web.xml");

            WebAppContext webappContext = factory.createWebAppContext();
            webappContext.setContextPath("/");
            webappContext.setResourceBase(webappBase);
            webappContext.setDescriptor(descriptorPath);

            server = factory.createServer(port);
            server.setHandler(webappContext);
            server.start();

            int serverPort = ((ServerConnector) server.getConnectors()[0])
                    .getLocalPort();
            LOGGER.info("metrics server started at port: {}", serverPort);

            // no server.join() - don't wait

        } catch (Exception e) {
            throw new CriticalException("unable to start metrics server", e);
        }
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
                LOGGER.info("metrics server stopped");
            } catch (Exception e) {
                // don't throw e as stop is outside the try in ScoopiEngine
                // can't use ErrorLog (circular dependency)
                LOGGER.error("stop metrics server", e);
            }
        }
    }

    private boolean isEnable() {
        boolean enable = false;
        try {
            enable = Boolean.parseBoolean(
                    configs.getConfig("scoopi.metrics.server.enable"));
        } catch (ConfigNotFoundException e1) {
        }
        return enable;
    }
}
