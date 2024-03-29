package org.codetab.scoopi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.junit.Test;

/**
 * <p>
 * Util tests.
 * @author Maithilish
 *
 */
public class UtilTest {

    private String newLine = System.lineSeparator();

    // @Rule
    // public ExpectedException testRule = ExpectedException.none();

    @Test
    public void testDeepClone() throws ClassNotFoundException, IOException {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");

        List<List<String>> obj = new ArrayList<>();
        obj.add(list);

        List<?> actual = Util.deepClone(List.class, obj);

        int objHash = System.identityHashCode(obj);
        int objListHash = System.identityHashCode(obj.get(0));

        int actualHash = System.identityHashCode(actual);
        int actualListHash = System.identityHashCode(actual.get(0));

        assertThat(obj).isEqualTo(actual);
        assertThat(objHash).isNotEqualTo(actualHash);
        assertThat(objListHash).isNotEqualTo(actualListHash);
    }

    @Test
    public void testDeepCloneNullParams()
            throws ClassNotFoundException, IOException {
        try {
            Util.deepClone(null, "obj");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("ofClass must not be null");
        }

        try {
            Util.deepClone(String.class, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("obj must not be null");
        }
    }

    @Test
    public void testHasNulls() {
        String x = "x";
        String y = "y";
        String z = null;
        assertThat(Util.hasNulls(x)).isFalse();
        assertThat(Util.hasNulls(x, y)).isFalse();
        assertThat(Util.hasNulls(x, z)).isTrue();
        assertThat(Util.hasNulls(z)).isTrue();
    }

    @Test
    public void testGetgetJson() {
        String expected = "{\"name\":\"x\",\"value\":\"y\"}";
        TestBean bean = new TestBean("x", "y");
        String actual = Util.getJson(bean, false);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetgetJsonPrettyPrint() {

        String line = System.lineSeparator();
        String expected = String.join("", "{", line, "  \"name\": \"x\",", line,
                "  \"value\": \"y\"", line, "}");
        TestBean bean = new TestBean("x", "y");
        String actual = Util.getJson(bean, true);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetJsonNullParams() {
        try {
            Util.getJson(null, false);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("obj must not be null");
        }
    }

    @Test
    public void testGetgetIndentedJson() {
        String expected = "\t\t\t{\"name\":\"x\",\"value\":\"y\"}";
        TestBean bean = new TestBean("x", "y");
        String actual = Util.getIndentedJson(bean, false);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetgetIndentedJsonPrettyPrint() {

        String line = System.lineSeparator();
        String indent = "\t\t\t";
        String expected =
                String.join("", indent, "{", line, indent, "  \"name\": \"x\",",
                        line, indent, "  \"value\": \"y\"", line, indent, "}");
        TestBean bean = new TestBean("x", "y");
        String actual = Util.getIndentedJson(bean, true);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    public void testGetIndentedJsonNullParams() {
        try {
            Util.getIndentedJson(null, false);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("obj must not be null");
        }
    }

    @Test
    public void testPraseTemporalAmount() {
        TemporalAmount ta = Util.parseTemporalAmount("P2M");
        assertThat(ta.get(ChronoUnit.MONTHS)).isEqualTo(2L);

        ta = Util.parseTemporalAmount("PT5S");
        assertThat(ta.get(ChronoUnit.SECONDS)).isEqualTo(5L);

        assertThrows(DateTimeParseException.class,
                () -> Util.parseTemporalAmount("X2M"));
    }

    @Test
    public void testParseTemporalAmountNullParams() {
        try {
            Util.parseTemporalAmount(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("text must not be null");
        }
    }

    @Test
    public void testStrip() {
        String str = Util.strip("test", 2);
        assertThat(StringUtils.startsWith(str, "test")).isTrue();
        assertThat(StringUtils.endsWith(str, "test")).isTrue();
        assertThat(0).isEqualTo(StringUtils.countMatches(str, newLine));

        String inStr = String.join(newLine, "line1", "line2", "line3", "line4");
        str = Util.strip(inStr, 1);
        assertThat(2).isEqualTo(StringUtils.countMatches(str, newLine)); // head

        inStr = String.join(newLine, "line1", "line2", "line3", "line4",
                "line5", "line6");
        str = Util.strip(inStr, 2);
        assertThat(4).isEqualTo(StringUtils.countMatches(str, newLine)); // head
                                                                         // 2
                                                                         // dots
                                                                         // 1
        // tail 1
    }

    @Test
    public void testStripeNullParams() {
        try {
            Util.strip(null, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("lines must not be null");
        }
    }

    @Test
    public void testHead() {
        String inStr = String.join(newLine, "line1", "line2", "line3", "line4",
                "line5", "line6");
        String expectedStr = String.join(newLine, "line1", "line2", "line3");

        String str = Util.head(inStr, 3);
        assertThat(str.equals(expectedStr)).isTrue();

        str = Util.head(String.join(newLine, "line1", "line2"), 1);
        assertThat("line1").isEqualTo(str);

        str = Util.head(String.join(newLine, "line1", "line2"), 0);
        assertThat("line1").isEqualTo(str);
    }

    @Test
    public void testHeadNullParams() {
        try {
            Util.head(null, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("lines must not be null");
        }
    }

    @Test
    public void testTail() {
        String inStr = String.join(newLine, "line1", "line2", "line3", "line4",
                "line5", "line6");
        String expectedStr = String.join(newLine, "line4", "line5", "line6");

        String str = Util.tail(inStr, 3);
        assertThat(str.equals(expectedStr)).isTrue();

        str = Util.tail(inStr + newLine, 3);
        assertThat(expectedStr + newLine).isEqualTo(str);

        str = Util.tail(String.join(newLine, "line1", "line2"), 1);
        assertThat("line2").isEqualTo(str);

        str = Util.tail(String.join(newLine, "line1", "line2"), 0);
        assertThat("line2").isEqualTo(str);
    }

    @Test
    public void testTailNullParams() {
        try {
            Util.tail(null, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("lines must not be null");
        }
    }

    @Test
    public void testGetMessage() {
        String expected = "DefNotFoundException: test";
        String actual = Util.getMessage(new DefNotFoundException("test"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetMessageNullParams() {
        try {
            Util.getMessage(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("exception must not be null");
        }
    }

    @Test
    public void testGetPropertiesAsString() {
        Properties props = new Properties();
        props.put("x", "xv");
        props.put("y", "yv");

        String actual = Util.getPropertiesAsString(props);

        String expected = System.lineSeparator() + Util.logIndent() + "x=xv"
                + System.lineSeparator() + Util.logIndent() + "y=yv"
                + System.lineSeparator();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetPropertiesAsNullParams() {
        try {
            Util.getPropertiesAsString(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("properties must not be null");
        }
    }

    @Test
    public void testWellDefinedUtilityClass()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        TestHelper.assertUtilityClassWellDefined(Util.class);
    }

    @Test
    public void testSplit() {
        // delimited with pipe
        Map<String, String> actual = Util.split("a=1|b=2", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        actual = Util.split("a=1|a=2|b=2", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "2"), entry("b", "2"));

        actual = Util.split("a=1|a=1|b=2", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        // delimited with space
        actual = Util.split("a=1 b=2", "=", " ");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        actual = Util.split("a=1 a=2 b=2", "=", " ");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "2"), entry("b", "2"));

        actual = Util.split("a=1 a=1 b=2", "=", " ");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        // key value separator :
        actual = Util.split("a:1 b:2", ":", " ");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        actual = Util.split("a:1 a:2 b:2", ":", " ");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "2"), entry("b", "2"));

        actual = Util.split("a:1 a:1 b:2", ":", " ");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        // trailing delimiter
        actual = Util.split("a=1|b=2|", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        actual = Util.split("a=1|a=2|b=2|", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "2"), entry("b", "2"));

        actual = Util.split("a=1|a=1|b=2|", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        // leading delimiter
        actual = Util.split("|a=1|b=2", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));

        actual = Util.split("|a=1|a=2|b=2", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "2"), entry("b", "2"));

        actual = Util.split("|a=1|a=1|b=2", "=", "|");
        assertThat(actual).hasSize(2);
        assertThat(actual).contains(entry("a", "1"), entry("b", "2"));
    }

    @Test
    public void testSplitNullParams() {
        try {
            Util.split(null, ":", " ");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("input must not be null");
        }

        try {
            Util.split("a:1 b:2", null, " ");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("keyValueSeparator must not be null");
        }

        try {
            Util.split("a:1 b:2", ":", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("delimiter must not be null");
        }
    }

    @Test
    public void testIndent() {
        String line = "test line";
        String indent = "  ";

        assertThat(Util.indent("test line", indent))
                .isEqualTo(String.join("", indent, line));
    }

    @Test
    public void testDashit() {
        String[] strings = {"foo", "bar", "joo"};
        assertThat(Util.dashit(strings)).isEqualTo("foo-bar-joo");
    }

    @Test
    public void testSpaceit() {
        String[] strings = {"foo", "bar", "joo"};
        assertThat(Util.spaceit(strings)).isEqualTo("foo bar joo");
    }

    @Test
    public void testAppend() {
        String[] strings = {"foo", "bar", "joo"};
        StringBuilder sb = new StringBuilder();
        Util.append(sb, strings);
        assertThat(sb.toString()).isEqualTo("foobarjoo");
    }
}

// for json tests
class TestBean {

    private String name;
    private String value;

    TestBean(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
