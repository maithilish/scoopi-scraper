package org.codetab.scoopi.plugin.appender;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Plugin;

public class Appenders extends HashMap<String, Appender> {

    private static final long serialVersionUID = -2379962204064401767L;

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private AppenderMediator appenderMediator;
    @Inject
    private Errors errors;

    public void createAppenders(final List<Plugin> plugins,
            final String stepsName, final String stepName) {

        for (Plugin plugin : plugins) {
            try {
                String appenderName = dashit(stepName, plugin.getName());
                Appender appender =
                        appenderMediator.getAppender(appenderName, plugin);
                if (isNull(appender)) {
                    appender = appenderMediator.createAppender(appenderName,
                            plugin);
                }
                put(appenderName, appender);
            } catch (ClassCastException | IllegalStateException
                    | ClassNotFoundException | DefNotFoundException e) {
                errors.inc();
                LOG.error("unable to create appender from plugin: {} [{}]",
                        plugin, ERROR.INTERNAL, e);
            }
        }

    }
}
