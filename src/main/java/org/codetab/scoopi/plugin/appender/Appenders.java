package org.codetab.scoopi.plugin.appender;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.system.ErrorLogger;

public class Appenders extends HashMap<String, Appender> {

    private static final long serialVersionUID = 1L;

    @Inject
    private AppenderMediator appenderMediator;
    @Inject
    private ErrorLogger errorLogger;

    public void createAppenders(final List<Plugin> plugins,
            final String stepsName, final String stepName) {

        for (Plugin plugin : plugins) {
            try {
                String appenderName =
                        String.join("-", stepsName, stepName, plugin.getName());
                Appender appender =
                        appenderMediator.getAppender(appenderName, plugin);
                if (isNull(appender)) {
                    appender = appenderMediator.createAppender(appenderName,
                            plugin);
                }
                put(appenderName, appender);
            } catch (ClassCastException | IllegalStateException
                    | ClassNotFoundException | DefNotFoundException e) {
                String message = String.join(" ",
                        "unable to create appender from plugin:",
                        plugin.toString());
                errorLogger.log(CAT.ERROR, message, e);
            }
        }

    }
}
