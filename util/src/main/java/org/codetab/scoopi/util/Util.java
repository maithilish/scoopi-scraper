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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * Utility methods.
 * @author Maithilish
 *
 */
public final class Util {

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

        notNull(ofClass, "ofClass must not be null");
        notNull(obj, "obj must not be null");

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
     * Stripe array of lines to count. Also, append suffix and prefix.
     * @param lines
     *            lines, not null
     * @param noOfLines
     *            stripe length
     * @param prefix
     *            prefix to append
     * @param suffix
     *            suffix to append
     * @return striped lines
     */
    public static String strip(final String lines, final int noOfLines) {

        notNull(lines, "lines must not be null");

        String newLine = LINE;

        StringBuffer sb = new StringBuffer();
        if (StringUtils.countMatches(lines, newLine) <= noOfLines) {
            sb.append(lines);
        } else {
            sb.append(head(lines, noOfLines));
            sb.append(newLine);
            sb.append("      ...");
            sb.append(newLine);
            sb.append(tail(lines, noOfLines));
        }
        return sb.toString();
    }

    /**
     * <p>
     * Get head of lines.
     * @param lines
     *            lines
     * @param noOfLines
     *            number of lines
     * @return head of lines
     */
    public static String head(final String lines, final int noOfLines) {

        notNull(lines, "lines must not be null");

        String newLine = LINE;

        int n = noOfLines;
        if (n < 1) {
            n = 1;
        }
        return lines.substring(0,
                StringUtils.ordinalIndexOf(lines, newLine, n));
    }

    /**
     * <p>
     * Get tail of lines.
     * @param lines
     *            lines
     * @param noOfLines
     *            number to lines
     * @return tail of lines
     */
    public static String tail(final String lines, final int noOfLines) {

        notNull(lines, "lines must not be null");

        String newLine = LINE;

        int n = noOfLines;
        if (n < 1) {
            n = 1;
        }
        if (StringUtils.endsWith(lines, newLine)) {
            n++;
        }
        return lines.substring(
                StringUtils.lastOrdinalIndexOf(lines, newLine, n) + 1);
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

        notNull(obj, "obj must not be null");

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

        notNull(obj, "obj must not be null");

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

    public static String indent(final String lines, final String indent) {
        return lines.replaceAll("(?m)^", indent);
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

        notNull(text, "text must not be null");

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

        notNull(exception, "exception must not be null");

        return exception.getClass().getSimpleName() + ": " //$NON-NLS-1$
                + exception.getMessage();
    }

    /**
     * <p>
     * Get properties as string.
     * @param properties
     *            properties to convert to string
     * @return string
     */
    public static String getPropertiesAsString(final Properties properties) {

        notNull(properties, "properties must not be null");

        String line = LINE;
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

        notNull(input, "input must not be null");
        notNull(keyValueSeparator, "keyValueSeparator must not be null");
        notNull(delimiter, "delimiter must not be null");

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

    public static String dashit(final String... parts) {
        return String.join("-", parts);
    }

    public static String spaceit(final String... parts) {
        return String.join(" ", parts);
    }

    public static void append(final StringBuilder sb, final String... parts) {
        for (String part : parts) {
            sb.append(part);
        }
    }

    public static String patchit(final String str, final Object... vars) {
        StringBuilder sb = new StringBuilder(str);
        for (Object var : vars) {
            int i = sb.indexOf("{}");
            if (var instanceof String) {
                sb.replace(i, i + 2, (String) var);
            } else {
                sb.replace(i, i + 2, String.valueOf(var));
            }
        }
        return sb.toString();
    }
}
