package org.codetab.scoopi.step.process;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.converter.ConverterMap;
import org.codetab.scoopi.plugin.converter.IConverter;
import org.codetab.scoopi.step.base.BaseProcessor;

/**
 * <p>
 * Apply converters to Data.
 * @author Maithilish
 *
 */
public final class DataConverter extends BaseProcessor {

    static final Logger LOG = LogManager.getLogger();

    @Inject
    private IPluginDef pluginDef;
    @Inject
    private ConverterMap converterMap;

    /**
     * to trace changes
     */
    private Map<String, String> convertedValues = new HashMap<>();

    /**
     * Get list of converters defined and apply it to applicable axis of Data.
     * @return true when no error
     */
    @Override
    public void process() {
        validState(nonNull(data), "data not set");

        LOG.debug(getJobMarker(), getLabeled("convert values"));

        String taskGroup = getPayload().getJobInfo().getGroup();
        String taskName = getPayload().getJobInfo().getTask();
        String stepName = getStepName();

        try {
            Optional<List<Plugin>> plugins =
                    pluginDef.getPlugins(taskGroup, taskName, stepName);
            if (plugins.isPresent()) {
                converterMap.init(plugins.get());
                // apply converters
                for (Item item : data.getItems()) {
                    for (String itemName : converterMap.keySet()) {
                        Axis axis = item.getAxisByItemName(itemName);
                        String value = axis.getValue();
                        List<IConverter> converters =
                                converterMap.get(itemName);
                        String cValue = convert(value, converters);
                        axis.setValue(cValue);
                    }
                }
            }
        } catch (Exception e) {
            throw new StepRunException("unable to apply converters", e);
        }

        setOutput(data);
        trace();
    }

    private String convert(final String value,
            final List<IConverter> axisConverters) throws Exception {
        String cValue = value;
        for (IConverter converter : axisConverters) {
            cValue = converter.convert(cValue);
        }
        if (LOG.isTraceEnabled() && !StringUtils.equals(cValue, value)) {
            convertedValues.put(value, cValue);
        }
        return cValue;
    }

    private void trace() {
        if (!LOG.isTraceEnabled()) {
            return;
        }
        if (convertedValues.isEmpty()) {
            LOG.trace(getJobMarker(), getLabeled("no convert value"));
        } else {
            LOG.trace(getJobMarker(), getLabeled("converted values"));
        }
        for (String key : convertedValues.keySet()) {
            LOG.trace(getJobMarker(), "  {} -> {}", key, //$NON-NLS-1$
                    convertedValues.get(key));
        }
    }
}
