package org.codetab.scoopi.metrics;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.di.BasicFactory;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.system.ErrorLogger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MetricsServer {

    static final Logger LOGGER = LoggerFactory.getLogger(MetricsServer.class);

    @Inject
    private ConfigService configService;
    @Inject
    private IOHelper ioHelper;
    @Inject
    private ErrorLogger errorLogger;
    @Inject
    private BasicFactory factory;

    private Server server;

    public void start() {
        if (!isEnable()) {
            LOGGER.info("metrics server is disabled");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(
                    configService.getConfig("scoopi.metrics.server.port"));
        } catch (ConfigNotFoundException | NumberFormatException e) {
            port = Integer.parseInt("9010");
        }

        try {
            server = factory.getServer(port);
            WebAppContext webapp = factory.getWebAppContext();
            webapp.setContextPath("/");

            String webappBase = ioHelper.getURL("/webapp").toString();
            String descriptorPath = webappBase + "WEB-INF/web.xml";
            webapp.setResourceBase(webappBase);
            webapp.setDescriptor(descriptorPath);

            server.setHandler(webapp);
            server.start();
            LOGGER.info("metrics server started at port: {}", port);
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
                errorLogger.log(CAT.ERROR, "stop metrics server", e);
            }
        }
    }

    private boolean isEnable() {
        boolean enable = false;
        try {
            enable = Boolean.parseBoolean(
                    configService.getConfig("scoopi.metrics.server.enable"));
        } catch (ConfigNotFoundException e1) {
        }
        return enable;
    }
}
