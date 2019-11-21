package org.codetab.scoopi.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.scoopi.log.Log.CAT;
import org.junit.Test;

public class LogTest {

    @Test
    public void testLogLable() {
        Log act = new Log(CAT.FATAL, "label", "x");
        assertThat(act.getCat()).isEqualTo(CAT.FATAL);
        assertThat(act.getLabel()).isEqualTo("label");
        assertThat(act.getMessage()).isEqualTo("x");
        assertThat(act.getThrowable()).isNull();
    }

    @Test
    public void testLogLabelThrowable() {
        Throwable t = new Throwable("exception");
        Log act = new Log(CAT.FATAL, "label", "x", t);
        assertThat(act.getCat()).isEqualTo(CAT.FATAL);
        assertThat(act.getLabel()).isEqualTo("label");
        assertThat(act.getMessage()).isEqualTo("x");
        assertThat(act.getThrowable()).isSameAs(t);
    }

    @Test
    public void testCAT() {
        // for test coverage of enum, we need to run both values and valueOf
        assertThat(CAT.values()[0]).isEqualTo(CAT.ERROR);
        assertThat(CAT.values()[1]).isEqualTo(CAT.CONFIG);
        assertThat(CAT.values()[2]).isEqualTo(CAT.FATAL);
        assertThat(CAT.values()[3]).isEqualTo(CAT.INTERNAL);
        assertThat(CAT.values()[4]).isEqualTo(CAT.USER);

        assertThat(CAT.valueOf("ERROR")).isEqualTo(CAT.ERROR);
        assertThat(CAT.valueOf("CONFIG")).isEqualTo(CAT.CONFIG);
        assertThat(CAT.valueOf("FATAL")).isEqualTo(CAT.FATAL);
        assertThat(CAT.valueOf("INTERNAL")).isEqualTo(CAT.INTERNAL);
        assertThat(CAT.valueOf("USER")).isEqualTo(CAT.USER);
    }

    @Test
    public void testLogToString() {
        Log act = new Log(CAT.FATAL, "label", "test");
        String expected =
                getExprectedString(CAT.FATAL, act.getLabel(), "test", null);
        assertThat(act.toString()).isEqualTo(expected);
    }

    @Test
    public void testLogToStringWithThrowable() {
        Throwable t = new Throwable("exception");
        Log act = new Log(CAT.FATAL, "label", "test", t);
        String expected =
                getExprectedString(CAT.FATAL, act.getLabel(), "test", t);
        assertThat(act.toString()).isEqualTo(expected);
    }

    private String getExprectedString(final CAT cat, final String label,
            final String message, final Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Log [Cat=");
        sb.append(cat);
        sb.append(" label=");
        sb.append(label);
        sb.append(" message=");
        sb.append(message);
        sb.append("]");
        if (throwable != null) {
            sb.append(System.lineSeparator());
            sb.append("          throwable=");
            sb.append(throwable);
        }
        return sb.toString();
    }

}
