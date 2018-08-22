package org.codetab.scoopi.util;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.messages.Messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * Utility methods.
 * @author Maithilish
 *
 */
public final class Util {

    /**
     * Line separator.
     */
    public static final String LINE = System.lineSeparator();

    /**
     * <p>
     * private constructor.
     */
    private Util() {
    }

    /**
     * <p>
     * Deep clone object with serialization.
     * @param <T>
     *            type
     * @param ofClass
     *            class type, not null
     * @param obj
     *            object to clone, not null
     * @return deep clone of object
     * @throws IOException
     *             on IO error
     * @throws ClassNotFoundException
     *             on class error
     */
    public static <T> T deepClone(final Class<T> ofClass, final T obj)
            throws IOException, ClassNotFoundException {

        Validate.notNull(ofClass, Messages.getString("Util.0")); //$NON-NLS-1$
        Validate.notNull(obj, Messages.getString("Util.1")); //$NON-NLS-1$

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);

        ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ofClass.cast(ois.readObject());
    }

    /**
     * <p>
     * Whether array of objects, contains any null object.
     * @param objects
     *            array of objects
     * @return true if any object is null else false
     */
    public static boolean hasNulls(final Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Get Cartesian product of sets.
     * @param sets
     *            sets, not null or empty
     * @return set of sets
     * @throws IllegalStateException
     *             if sets is less than two or any set is empty
     */
    public static Set<Set<Object>> cartesianProduct(final Set<?>... sets) {

        if (sets.length < 2) {
            throw new IllegalStateException(join(Messages.getString("Util.2"), //$NON-NLS-1$
                    String.valueOf(sets.length), Messages.getString("Util.3"))); //$NON-NLS-1$
        }
        for (Set<?> set : sets) {
            if (set.size() == 0) {
                throw new IllegalStateException(Messages.getString("Util.4")); //$NON-NLS-1$
            }
        }
        return cartesianProduct(0, sets);
    }

    /**
     * <p>
     * Generate Cartesian product.
     * @param index
     *            start index
     * @param sets
     *            sets
     * @return set of sets
     */
    private static Set<Set<Object>> cartesianProduct(final int index,
            final Set<?>... sets) {
        Set<Set<Object>> ret = new HashSet<Set<Object>>();
        if (index == sets.length) {
            ret.add(new HashSet<Object>());
        } else {
            for (Object obj : sets[index]) {
                for (Set<Object> set : cartesianProduct(index + 1, sets)) {
                    set.add(obj);
                    ret.add(set);
                }
            }
        }
        return ret;
    }

    /**
     * <p>
     * Stripe array of lines to count. Also, append suffix and prefix.
     * @param string
     *            lines, not null
     * @param noOfLines
     *            stripe length
     * @param prefix
     *            prefix to append
     * @param suffix
     *            suffix to append
     * @return striped lines
     */
    public static String stripe(final String string, final int noOfLines,
            final String prefix, final String suffix) {

        Validate.notNull(string, Messages.getString("Util.5")); //$NON-NLS-1$

        StringBuffer sb = new StringBuffer();
        if (prefix != null) {
            sb.append(prefix);
        }
        if (StringUtils.countMatches(string, LINE) <= noOfLines) {
            sb.append(string);
        } else {
            sb.append(head(string, noOfLines));
            sb.append(LINE);
            sb.append("      ..."); //$NON-NLS-1$
            sb.append(LINE);
            sb.append(tail(string, noOfLines));
        }
        if (suffix != null) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    /**
     * <p>
     * Get head of lines.
     * @param string
     *            lines
     * @param noOfLines
     *            number of lines
     * @return head of lines
     */
    public static String head(final String string, final int noOfLines) {

        Validate.notNull(string, Messages.getString("Util.7")); //$NON-NLS-1$

        int n = noOfLines;
        if (n < 1) {
            n = 1;
        }
        return string.substring(0, StringUtils.ordinalIndexOf(string, LINE, n)); // $NON-NLS-1$
    }

    /**
     * <p>
     * Get tail of lines.
     * @param string
     *            lines
     * @param noOfLines
     *            number to lines
     * @return tail of lines
     */
    public static String tail(final String string, final int noOfLines) {

        Validate.notNull(string, Messages.getString("Util.9")); //$NON-NLS-1$

        int n = noOfLines;
        if (n < 1) {
            n = 1;
        }
        if (StringUtils.endsWith(string, LINE)) { // $NON-NLS-1$
            n++;
        }
        return string
                .substring(StringUtils.lastOrdinalIndexOf(string, LINE, n) + 1); // $NON-NLS-1$
    }

    /**
     * <p>
     * Get JSON of object.
     * @param obj
     *            object
     * @param prettyPrint
     *            whether to format
     * @return JSON
     */
    public static String getJson(final Object obj, final boolean prettyPrint) {

        Validate.notNull(obj, Messages.getString("Util.12")); //$NON-NLS-1$

        GsonBuilder gb = new GsonBuilder();
        if (prettyPrint) {
            gb.setPrettyPrinting();
            gb.serializeNulls();
            gb.disableHtmlEscaping();
        }
        Gson gson = gb.create();
        String json = gson.toJson(obj);
        return json;
    }

    /**
     * <p>
     * Get JSON of object, indented.
     * @param obj
     *            object
     * @param prettyPrint
     *            whether to format
     * @return JSON
     */
    public static String getIndentedJson(final Object obj,
            final boolean prettyPrint) {

        Validate.notNull(obj, Messages.getString("Util.13")); //$NON-NLS-1$

        String json = getJson(obj, prettyPrint);
        String indentedJson = json.replaceAll("(?m)^", Util.logIndent()); //$NON-NLS-1$
        return indentedJson;
    }

    /**
     * <p>
     * Get spacer string.
     * @return string
     */
    public static String logIndent() {
        return "\t\t\t"; //$NON-NLS-1$
    }

    /**
     * <p>
     * Concat strings delimited with space.
     * @param strings
     *            strings to merge
     * @return merged string
     */
    public static String join(final String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * <p>
     * Parse ISO-8601 duration format PnDTnHnMn.nS or period format PnYnMnWnD as
     * TemporalAmount (Duration or Period).
     * @param text
     *            text to parse
     * @return temporal amount
     * @throws DateTimeParseException
     *             on parse exception
     */
    public static TemporalAmount parseTemporalAmount(final CharSequence text)
            throws DateTimeParseException {

        Validate.notNull(text, Messages.getString("Util.14")); //$NON-NLS-1$

        TemporalAmount ta;
        try {
            ta = Duration.parse(text);
        } catch (DateTimeParseException e) {
            ta = Period.parse(text);
        }
        return ta;
    }

    /**
     * <p>
     * Get exception type and message from exception.
     * @param exception
     *            exception
     * @return string exception type and message
     */
    public static String getMessage(final Exception exception) {

        Validate.notNull(exception, Messages.getString("Util.15")); //$NON-NLS-1$

        return exception.getClass().getSimpleName() + ": " //$NON-NLS-1$
                + exception.getLocalizedMessage();
    }

    /**
     * <p>
     * Get properties as string.
     * @param properties
     *            properties to convert to string
     * @return string
     */
    public static String getPropertiesAsString(final Properties properties) {

        Validate.notNull(properties, Messages.getString("Util.10")); //$NON-NLS-1$

        String line = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        for (Entry<Object, Object> entry : properties.entrySet()) {
            sb.append(Util.logIndent());
            sb.append(entry);
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * <p>
     * Split delimited string to key/value map.
     * <p>
     * example : x=1|y=2 key value separator is = and delimiter is | if there
     * are duplicates then last item is added to map
     * @param input
     *            string to split, not null
     * @param keyValueSeparator
     *            key value separator, not null
     * @param delimiter
     *            entry delimiter, not null
     * @return map
     */
    public static Map<String, String> split(final String input,
            final String keyValueSeparator, final String delimiter) {

        Validate.notNull(input, Messages.getString("Util.11")); //$NON-NLS-1$
        Validate.notNull(keyValueSeparator, Messages.getString("Util.8")); //$NON-NLS-1$
        Validate.notNull(delimiter, Messages.getString("Util.6")); //$NON-NLS-1$

        // toMap last arg is mergeFunction which selects the second item from
        // duplicates
        Map<String, String> map = Arrays
                .stream(StringUtils.split(input, delimiter))
                .map(s -> s.split(keyValueSeparator))
                .collect(Collectors.toMap(a -> a[0], a -> a[1], (x, y) -> y));
        return map;
    }

    public static Range<Integer> getRange(final String value) {
        notNull(value, "value must not be null");

        if (value.startsWith("-")) { //$NON-NLS-1$
            throw new NumberFormatException("invalid range" + value);
        }
        String[] tokens = StringUtils.split(value, '-');
        if (tokens.length < 1 || tokens.length > 2) {
            throw new NumberFormatException("invalid range" + value);
        }
        Integer min = 0, max = 0;
        if (tokens.length == 1) {
            min = Integer.parseInt(tokens[0]);
            max = Integer.parseInt(tokens[0]);
        }
        if (tokens.length == 2) {
            min = Integer.parseInt(tokens[0]);
            max = Integer.parseInt(tokens[1]);

        }
        if (min > max) {
            throw new NumberFormatException("invalid range, min > max" + value);
        }
        return Range.between(min, max);
    }

}
