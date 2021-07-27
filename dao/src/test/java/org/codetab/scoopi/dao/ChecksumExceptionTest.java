package org.codetab.scoopi.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ChecksumExceptionTest {

    private ChecksumException ex;

    @Test
    public void testException() {
        String message = "xyz";
        String expected = message;

        ex = new ChecksumException(message);

        assertThat(ex.getMessage()).isEqualTo(expected);
    }

    @Test
    public void testCriticalExceptionWithCause() {
        Throwable cause = new Throwable("x");
        ex = new ChecksumException(cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isNull();
    }

    @Test
    public void testCriticalExceptionWithMessageAndCause() {
        String message = "xyz";
        Throwable cause = new Throwable("x");
        String expected = message;

        ex = new ChecksumException(message, cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isEqualTo(expected);
    }

}
