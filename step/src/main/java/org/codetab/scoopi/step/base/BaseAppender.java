package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.plugin.appender.Appender;
import org.codetab.scoopi.plugin.appender.Appenders;
import org.codetab.scoopi.plugin.encoder.Encoders;
import org.codetab.scoopi.plugin.encoder.IEncoder;
import org.codetab.scoopi.step.Step;

public abstract class BaseAppender extends Step {

    @Inject
    private IPluginDef pluginDef;
    @Inject
    protected Appenders appenders;
    @Inject
    protected Encoders encoders;

    protected Data data;

    @Override
    public void initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        Object pData = getPayload().getData();
        if (pData instanceof Data) {
            data = (Data) pData;
        } else {
            String message = spaceit("payload data type is not Data but",
                    pData.getClass().getName());
            throw new StepRunException(message);
        }

        String taskGroup = getPayload().getJobInfo().getGroup();
        String taskName = getPayload().getJobInfo().getTask();
        String stepsName = getPayload().getJobInfo().getSteps();
        String stepName = getStepName();

        try {
            Optional<List<Plugin>> plugins =
                    pluginDef.getPlugins(taskGroup, taskName, stepName);
            if (plugins.isPresent()) {
                appenders.createAppenders(plugins.get(), stepsName, stepName);
                encoders.createEncoders(plugins.get(), stepsName, stepName);
            }
        } catch (Exception e) {
            throw new StepRunException("unable to create appenders", e);
        }
    }

    protected void doAppend(final Appender appender,
            final PrintPayload printPayload) throws InterruptedException {
        appender.append(printPayload);
    }

    protected Object encode(final List<IEncoder<?>> encodersList)
            throws Exception {
        Object obj = data;
        for (IEncoder<?> encoder : encodersList) {
            obj = encoder.encode(data);
        }
        return obj;
    }

    @Override
    public void load() {
    }

    @Override
    public void store() {
    }
}
