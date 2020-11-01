package org.codetab.scoopi.metrics.server;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.metrics.IMetricsServer;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

@Singleton
public class MetricsServer implements IMetricsServer {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private MetricsHelper metricsHelper;
    @Inject
    private ServerFactory factory;

    private Server server;

    private Map<String, byte[]> metricsJsonData;

    @Override
    public void start() {
        int port;
        try {
            port = Integer
                    .parseInt(configs.getConfig("scoopi.metrics.server.port"));
        } catch (ConfigNotFoundException | NumberFormatException e) {
            port = Integer.parseInt("9010");
        }

        try {
            final String webappBase =
                    metricsHelper.getURL("/webapp").toString();
            final String descriptorPath =
                    String.join("/", webappBase, "WEB-INF/web.xml");

            final WebAppContext webappContext = factory.createWebAppContext();
            webappContext.setContextPath("/");
            webappContext.setResourceBase(webappBase);
            webappContext.setDescriptor(descriptorPath);
            webappContext.setAttribute("scoopi.metricsJsonData",
                    metricsJsonData);

            server = factory.createServer(port);
            server.setHandler(webappContext);
            server.start();

            final int serverPort = ((ServerConnector) server.getConnectors()[0])
                    .getLocalPort();
            LOG.info("metrics server started at port: {}", serverPort);

            // no server.join() - don't wait

        } catch (final Exception e) {
            throw new CriticalException("unable to start metrics server", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.stop();
                LOG.info("metrics server stopped");
            } catch (final Exception e) {
                // don't throw e as stop is outside the try in ScoopiEngine
                // can't use ErrorLog (circular dependency)
                LOG.error("stop metrics server", e);
            }
        }
    }

    @Override
    public void setMetricsJsonData(final Map<String, byte[]> metricsJsonData) {
        this.metricsJsonData = metricsJsonData;
    }
}
