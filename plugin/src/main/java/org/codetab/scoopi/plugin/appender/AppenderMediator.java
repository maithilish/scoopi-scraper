package org.codetab.scoopi.plugin.appender;

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.plugin.appender.Appender.Marker;

@Singleton
public class AppenderMediator {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    protected AppenderFactory appenderFactory;
    @Inject
    private AppenderPoolService appenderPoolService;
    @Inject
    private ObjectFactory objectFactory;
    @Inject
    protected Errors errors;

    private final Map<String, Appender> appenders =
            new ConcurrentHashMap<String, Appender>();

    public Appender getAppender(final String appenderName) {
        return appenders.get(appenderName);
    }

    @GuardedBy("this")
    public synchronized Appender createAppender(final String appenderName,
            final Plugin plugin) throws ClassCastException,
            ClassNotFoundException, DefNotFoundException {
        Appender appender = appenders.get(appenderName);
        if (isNull(appender)) {
            appender = appenderFactory.createAppender(appenderName, plugin);
            appenderPoolService.submit("appender", appender); //$NON-NLS-1$
            appenders.put(appenderName, appender);
        }
        return appender;
    }

    @GuardedBy("this")
    public synchronized void closeAll() {
        for (String appenderName : appenders.keySet()) {
            Appender appender = appenders.get(appenderName);
            try {
                PrintPayload eosPayload = objectFactory.createPrintPayload(null,
                        Marker.END_OF_STREAM);
                appender.append(eosPayload);
            } catch (InterruptedException e) {
                errors.inc();
                LOG.error("close appender: {} [{}]", appenderName,
                        ERROR.INTERNAL);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void waitForFinish() {
        appenderPoolService.waitForFinish();
    }
}
