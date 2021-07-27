package org.codetab.scoopi.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DaoExceptionTest {

    private DaoException ex;

    @Test
    public void testException() {
        String message = "xyz";
        String expected = message;

        ex = new DaoException(message);

        assertThat(ex.getMessage()).isEqualTo(expected);
    }

    @Test
    public void testCriticalExceptionWithCause() {
        Throwable cause = new Throwable("x");
        ex = new DaoException(cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isNull();
    }

    @Test
    public void testCriticalExceptionWithMessageAndCause() {
        String message = "xyz";
        Throwable cause = new Throwable("x");
        String expected = message;

        ex = new DaoException(message, cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isEqualTo(expected);
    }

}
