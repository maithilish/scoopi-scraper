package org.codetab.scoopi.step.script;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.script.ScriptExecutor;
import org.codetab.scoopi.step.base.BaseScripter;

/**
 * <p>
 * Apply converters to Data.
 * @author Maithilish
 *
 */
public final class Scripter extends BaseScripter {

    static final Logger LOG = LogManager.getLogger();

    @Inject
    private IPluginDef pluginDef;
    @Inject
    private ScriptExecutor scriptExecutor;

    /**
     * Get list of converters defined and apply it to applicable axis of Data.
     * @return true when no error
     */
    @Override
    public void process() {
        validState(nonNull(input), "input not set");

        LOG.debug(getJobMarker(), getLabeled("apply scripts to input"));

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
        } catch (Exception e) {
            throw new StepRunException("unable to execute script plugin", e);
        }
    }
}
