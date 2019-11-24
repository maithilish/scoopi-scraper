package org.codetab.scoopi.plugin.appender;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.appender.Appender.Marker;
import org.codetab.scoopi.step.pool.AppenderPoolService;

@Singleton
public class AppenderMediator {

    @Inject
    protected AppenderFactory appenderFactory;
    @Inject
    private AppenderPoolService appenderPoolService;
    @Inject
    protected ErrorLogger errorLogger;

    private final Map<String, Appender> appenders =
            new ConcurrentHashMap<String, Appender>();

    public Appender getAppender(final String appenderName,
            final Plugin plugin) {
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
        for (String name : appenders.keySet()) {
            close(name);
        }
    }

    @GuardedBy("this")
    private void close(final String appenderName) {
        Appender appender = appenders.get(appenderName);
        if (nonNull(appender)) {
            try {
                appender.append(Marker.EOF);
            } catch (InterruptedException e) {
                String message = spaceit("close appender:", appenderName);
                errorLogger.log(CAT.INTERNAL, message, e);
            }
        }
    }
}
