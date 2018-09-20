package org.codetab.scoopi.step.convert;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;
import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.converter.Converters;
import org.codetab.scoopi.plugin.converter.IConverter;
import org.codetab.scoopi.step.base.BaseConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Apply converters to Data.
 * @author Maithilish
 *
 */
public final class DataConverter extends BaseConverter {

    /**
     * logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(DataConverter.class);

    @Inject
    private IPluginDefs pluginDefs;
    @Inject
    private Converters converters;

    /**
     * to trace changes
     */
    private Map<String, String> convertedValues = new HashMap<>();

    /**
     * Get list of converters defined and apply it to applicable axis of Data.
     * @return true when no error
     */
    @Override
    public boolean process() {
        validState(nonNull(data), "data not set");

        LOGGER.info(getMarker(), getLabeled("convert values"));

        String taskGroup = getPayload().getJobInfo().getGroup();
        String taskName = getPayload().getJobInfo().getTask();
        String stepName = getStepName();

        try {
            Optional<List<Plugin>> plugins =
                    pluginDefs.getPlugins(taskGroup, taskName, stepName);
            if (plugins.isPresent()) {

                converters.createConverters(plugins.get());

                // apply converters
                for (Member member : data.getMembers()) {

                    String col = member.getValue(AxisName.COL);
                    String row = member.getValue(AxisName.ROW);
                    String fact = member.getValue(AxisName.FACT);

                    col = convert(col, converters.get(AxisName.COL));
                    row = convert(row, converters.get(AxisName.ROW));
                    fact = convert(fact, converters.get(AxisName.FACT));

                    member.setValue(AxisName.COL, col);
                    member.setValue(AxisName.ROW, row);
                    member.setValue(AxisName.FACT, fact);
                }
            }
        } catch (Exception e) {
            throw new StepRunException("unable to apply converters", e);
        }

        setOutput(data);
        setConsistent(true);
        trace();
        return true;
    }

    private String convert(final String value,
            final List<IConverter> axisConverters) throws Exception {
        String iValue = value;
        String rvalue = value;
        for (IConverter converter : axisConverters) {
            rvalue = converter.convert(rvalue);
        }
        if (LOGGER.isTraceEnabled() && !StringUtils.equals(rvalue, iValue)) {
            convertedValues.put(iValue, rvalue);
        }
        return rvalue;
    }

    private void trace() {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        if (convertedValues.isEmpty()) {
            LOGGER.trace(getMarker(), getLabeled("no convert value"));
        } else {
            LOGGER.trace(getMarker(), getLabeled("converted values"));
        }
        for (String key : convertedValues.keySet()) {
            LOGGER.trace(getMarker(), "  {} -> {}", key, //$NON-NLS-1$
                    convertedValues.get(key));
        }
    }
}
