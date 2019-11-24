package org.codetab.scoopi.step.script;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.script.ScriptExecutor;
import org.codetab.scoopi.step.base.BaseScripter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Apply converters to Data.
 * @author Maithilish
 *
 */
public final class Scripter extends BaseScripter {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(Scripter.class);

    @Inject
    private IPluginDef pluginDef;
    @Inject
    private ScriptExecutor scriptExecutor;

    /**
     * Get list of converters defined and apply it to applicable axis of Data.
     * @return true when no error
     */
    @Override
    public boolean process() {
        validState(nonNull(input), "input not set");

        LOGGER.debug(getMarker(), getLabeled("apply scripts to input"));

        try {
            String taskGroup = getPayload().getJobInfo().getGroup();
            String taskName = getPayload().getJobInfo().getTask();
            String stepName = getStepName();

            Optional<List<Plugin>> plugins =
                    pluginDef.getPlugins(taskGroup, taskName, stepName);

            Object scriptOutput = input;
            if (plugins.isPresent()) {
                scriptOutput = scriptExecutor.execute(plugins.get(), input);
            }
            setOutput(scriptOutput);
            setConsistent(true);
        } catch (Exception e) {
            throw new StepRunException("unable to execute script plugin", e);
        }
        return true;
    }
}
