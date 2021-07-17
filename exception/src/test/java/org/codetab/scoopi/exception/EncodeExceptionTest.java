package org.codetab.scoopi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EncodeExceptionTest {

    private EncodeException ex;

    @Test
    public void testException() {
        String message = "xyz";

        ex = new EncodeException(message);

        assertThat(ex.getMessage()).isEqualTo(message);
    }

    @Test
    public void testCriticalExceptionWithCause() {
        Throwable cause = new Throwable("x");
        ex = new EncodeException(cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isNull();
    }

    @Test
    public void testCriticalExceptionWithMessageAndCause() {
        String message = "xyz";
        Throwable cause = new Throwable("x");

        ex = new EncodeException(message, cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isEqualTo(message);
    }

    @Test
    public void testToString() {
        Throwable cause = new Throwable("t");
        ex = new EncodeException("xyz", cause);
        String expected =
                "EncodeException [message=xyz, cause=java.lang.Throwable: t]";
        assertThat(ex.toString()).isEqualTo(expected);
    }
}
