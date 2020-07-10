package org.codetab.scoopi.plugin.encoder;

import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Plugin;

public class Encoders extends HashMap<String, List<IEncoder<?>>> {

    private static final long serialVersionUID = -1556127336620930856L;

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private IPluginDef pluginDef;
    @Inject
    private EncoderFactory encoderFactory;
    @Inject
    private Errors errors;

    public void createEncoders(final List<Plugin> plugins,
            final String stepsName, final String stepName) {
        for (Plugin plugin : plugins) {
            String appenderName = dashit(stepName, plugin.getName());
            Optional<List<Plugin>> encoderPlugins = null;
            try {
                encoderPlugins = pluginDef.getPlugins(plugin);
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
                        // FIXME - logfix dataerror or critical
                        errors.inc();
                        LOG.error(
                                "unable to create encoder from plugin: {} [{}]",
                                plugin, ERROR.DATAERROR, e);
                    }
                }
                put(appenderName, encoders);
            }
        }
    }
}
