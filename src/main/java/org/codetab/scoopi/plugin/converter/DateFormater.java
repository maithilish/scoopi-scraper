package org.codetab.scoopi.plugin.converter;

import static org.apache.commons.lang3.Validate.notNull;

import java.text.ParseException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

/**
 * <p>
 * String to Date converter.
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
     * @throws ParseException
     *             if parse error
     * @throws DefNotFoundException
     */
    @Override
    public String convert(final String input)
            throws ParseException, DefNotFoundException {
        notNull(input, "input must not be null");

        // TODO optimise: add PluginCache and get value from it
        String inPattern = pluginDef.getValue(plugin, "inPattern");
        String[] inPatterns = inPattern.split("\\|");
        String outPattern = pluginDef.getValue(plugin, "outPattern");

        Date date = DateUtils.parseDate(input, inPatterns);
        return DateFormatUtils.format(date, outPattern);
    }

    @Override
    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
    }
}
