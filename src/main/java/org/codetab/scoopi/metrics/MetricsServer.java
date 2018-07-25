package org.codetab.scoopi.metrics;

import javax.inject.Inject;

import org.codetab.scoopi.di.BasicFactory;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.shared.ConfigService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class MetricsServer {

    static final Logger LOGGER = LoggerFactory.getLogger(MetricsServer.class);

    @Inject
    private ConfigService configService;
    @Inject
    private IOHelper ioHelper;
    @Inject
    private BasicFactory factory;

    private Server server;

    public void start() {
        if (!isEnable()) {
            LOGGER.info(Messages.getString("MetricsServer.5")); //$NON-NLS-1$
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
            LOGGER.info(Messages.getString("MetricsServer.3"), port); //$NON-NLS-1$
            // no server.join() - don't wait
        } catch (Exception e) {
            throw new CriticalException(Messages.getString("MetricsServer.1"), //$NON-NLS-1$
                    e);
        }
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
                LOGGER.info(Messages.getString("MetricsServer.4")); //$NON-NLS-1$
            } catch (Exception e) {
                // don't throw e as stop is outside the try in ScoopiEngine
                LOGGER.error("{}", e.getMessage()); //$NON-NLS-1$
                LOGGER.warn("{}", e.getMessage()); //$NON-NLS-1$
                LOGGER.debug("{}", e); //$NON-NLS-1$
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
