package org.codetab.scoopi.plugin.encoder;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.system.ErrorLogger;

public class Encoders extends HashMap<String, List<IEncoder<?>>> {

    private static final long serialVersionUID = 1L;

    @Inject
    private IPluginDefs pluginDefs;
    @Inject
    private EncoderFactory encoderFactory;
    @Inject
    private ErrorLogger errorLogger;

    public void createEncoders(final List<Plugin> plugins,
            final String stepsName, final String stepName) {
        for (Plugin plugin : plugins) {
            String appenderName =
                    String.join("-", stepsName, stepName, plugin.getName());
            Optional<List<Plugin>> encoderPlugins = null;
            try {
                encoderPlugins = pluginDefs.getPlugins(plugin);
            } catch (Exception e) {
                throw new StepRunException("unable to create appenders", e);
            }
            if (nonNull(encoderPlugins) && encoderPlugins.isPresent()) {
                List<IEncoder<?>> encoders = new ArrayList<>();
                for (Plugin encoderPlugin : encoderPlugins.get()) {
                    try {
                        IEncoder<?> encoder =
                                encoderFactory.createEncoder(encoderPlugin);
                        encoders.add(encoder);
                    } catch (ClassCastException | IllegalStateException
                            | ClassNotFoundException | DefNotFoundException e) {
                        String message = String.join(" ",
                                "unable to create appender from plugin:",
                                plugin.toString());
                        errorLogger.log(CAT.ERROR, message, e);
                    }
                }
                put(appenderName, encoders);
            }
        }
    }
}
