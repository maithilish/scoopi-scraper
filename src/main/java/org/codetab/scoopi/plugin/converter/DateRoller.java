package org.codetab.scoopi.plugin.converter;

import static org.apache.commons.lang3.Validate.notNull;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.util.Util;

/**
 * <p>
 * String to Date converter.
 * @author Maithilish
 *
 */
public class DateRoller implements IConverter {

    @Inject
    private IPluginDef pluginDef;

    private Plugin plugin;

    /**
     * <p>
     * Convert input string to date and set a field to its maximum.
     * <p>
     * Fields should have following fields : inPattern - date pattern to parse
     * the input, outPattern - date pattern to format the returned date and
     * field - Calendar field which has to set to its maximum.
     * <p>
     * Date pattern is java date pattern as defined by
     * {@link java.text.SimpleDateFormat}.
     * <p>
     * example : if field is DAY_OF_MONTH then date is set as month end date.
     * @param input
     *            date string
     * @return date parsed date rounded off to month end.
     * @throws ParseException
     *             if parse error
     * @throws IllegalAccessException
     *             if no such Calendar field
     * @throws DefNotFoundException
     * @see java.text.SimpleDateFormat
     * @see Calendar
     */
    @Override
    public String convert(final String input) throws ParseException,
            IllegalAccessException, DefNotFoundException {
        notNull(input, "input must not be null");

        // TODO optimise: add PluginCache and get value from it
        String inPattern = pluginDef.getValue(plugin, "inPattern");
        String[] inPatterns = inPattern.split("\\|");
        String outPattern = pluginDef.getValue(plugin, "outPattern");

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(DateUtils.parseDate(input, inPatterns));

        // get map of calendar fields to roll
        String rollStr = pluginDef.getValue(plugin, "roll");
        Map<String, String> rollMap = Util.split(rollStr, "=", " "); //$NON-NLS-1$ //$NON-NLS-2$

        // roll calendar fields
        for (String key : rollMap.keySet()) {

            int calField = (int) FieldUtils
                    .readDeclaredStaticField(Calendar.class, key);
            String value = rollMap.get(key).toLowerCase();

            switch (value) {
            case "ceil": //$NON-NLS-1$
                cal.set(calField, cal.getActualMaximum(calField));
                break;
            case "floor": //$NON-NLS-1$
                cal.set(calField, cal.getActualMinimum(calField));
                break;
            case "round": //$NON-NLS-1$
                int max = cal.getActualMaximum(calField);
                int mid = max / 2;
                if (cal.get(calField) <= mid) {
                    cal.set(calField, cal.getActualMinimum(calField));
                } else {
                    cal.set(calField, max);
                }
                break;
            default:
                Integer amount = Integer.parseInt(value);
                cal.set(calField, amount);
                break;
            }
        }

        return DateFormatUtils.format(cal.getTime(), outPattern);
    }

    @Override
    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
    }
}
