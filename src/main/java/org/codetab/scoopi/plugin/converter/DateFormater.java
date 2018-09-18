package org.codetab.scoopi.plugin.converter;

import static org.apache.commons.lang3.Validate.notNull;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.model.Plugin;

/**
 * <p>
 * String to Date converter.
 * @author Maithilish
 *
 */
public class DateFormater implements IConverter {

    @Inject
    private IPluginDefs pluginDefs;

    private Plugin plugin;

    /**
     * Convert input string to date and round it off to month end.
     * @param input
     *            date string
     * @return date parsed date rounded off to month end.
     * @throws ParseException
     *             if parse error
     */
    @Override
    public String convert(final String input) throws ParseException {
        notNull(input, "input must not be null");

        String patternIn = pluginDefs.getValue(plugin, "patternIn");
        String patternOut = pluginDefs.getValue(plugin, "patternOut");

        Date date = DateUtils.parseDate(input, patternIn);
        return DateFormatUtils.format(date, patternOut);
    }

    @Override
    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
    }
}
