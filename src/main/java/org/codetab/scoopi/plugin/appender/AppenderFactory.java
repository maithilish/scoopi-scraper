package org.codetab.scoopi.plugin.appender;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

@Singleton
public class AppenderFactory {

    @Inject
    private DInjector di;

    public synchronized Appender createAppender(final String appenderName,
            final Plugin plugin) throws ClassCastException,
            ClassNotFoundException, DefNotFoundException {
        Appender appender = di.instance(plugin.getClassName(), Appender.class);
        appender.setName(appenderName);
        appender.setPlugin(plugin);
        appender.init();
        if (appender.isInitialized()) {
            appender.initializeQueue();
        } else {
            String message =
                    String.join(" ", "appender not initalized:", appenderName);
            throw new IllegalStateException(message);
        }
        return appender;
    }

}
