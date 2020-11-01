package org.codetab.scoopi.plugin.converter;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

import com.google.common.collect.Lists;

/**
 * <p>
 * String to ZonedDateTime converter.
 * @author Maithilish
 *
 */
public class DateFormater implements IConverter {

    @Inject
    private IPluginDef pluginDef;

    private Plugin plugin;

    /**
     * Convert input string to date and round it off to month end.
     * @param input
     *            date string
     * @return date parsed date rounded off to month end.
     * @throws DefNotFoundException
     */
    @Override
    public String convert(final String input) throws DefNotFoundException {
        notNull(input, "input must not be null");

        String output = (String) plugin.get(input);
        if (nonNull(output)) {
            return output;
        }

        List<String> inPatterns = Lists.newArrayList(
                pluginDef.getValue(plugin, "inPattern").split("\\|"));

        ZonedDateTime date = null;
        Optional<DateTimeParseException> ex = Optional.empty();
        for (String inPattern : inPatterns) {
            ex = Optional.empty();
            DateTimeFormatter inFormatter =
                    DateTimeFormatter.ofPattern(inPattern);
            try {
                date = ZonedDateTime.parse(input, inFormatter);
            } catch (DateTimeParseException e) {
                ex = Optional.of(e);
            }
        }
        if (ex.isPresent()) {
            throw ex.get();
        }

        String outPattern = pluginDef.getValue(plugin, "outPattern");
        DateTimeFormatter outFormatter =
                DateTimeFormatter.ofPattern(outPattern);

        output = date.format(outFormatter);
        plugin.put(input, output);
        return output;
    }

    @Override
    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
    }
}
