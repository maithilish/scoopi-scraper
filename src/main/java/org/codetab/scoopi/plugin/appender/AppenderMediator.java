package org.codetab.scoopi.plugin.appender;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.appender.Appender.Marker;
import org.codetab.scoopi.pool.AppenderPoolService;
import org.codetab.scoopi.system.ErrorLogger;

@Singleton
public class AppenderMediator {

    @Inject
    protected AppenderFactory appenderFactory;
    @Inject
    private AppenderPoolService appenderPoolService;
    @Inject
    protected ErrorLogger errorLogger;

    private final Map<String, Appender> appenders =
            new HashMap<String, Appender>();

    public synchronized Appender getAppender(final String appenderName,
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

    public synchronized void closeAll() {
        for (String name : appenders.keySet()) {
            close(name);
        }
    }

    public void close(final String appenderName) {
        Appender appender = appenders.get(appenderName);
        if (nonNull(appender)) {
            try {
                appender.append(Marker.EOF);
            } catch (InterruptedException e) {
                String message =
                        String.join(" ", "close appender:", appenderName);
                errorLogger.log(CAT.INTERNAL, message, e);
            }
        }
    }
}
