package org.codetab.scoopi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class StepRunExceptionTest {

    private StepRunException ex;

    @Test
    public void testException() {
        String message = "xyz";
        String expected = "[" + message + "]";

        ex = new StepRunException(message);

        assertThat(ex.getMessage()).isEqualTo(expected);
    }

    @Test
    public void testCriticalExceptionWithCause() {
        Throwable cause = new Throwable("x");
        ex = new StepRunException(cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isNull();
    }

    @Test
    public void testCriticalExceptionWithMessageAndCause() {
        String message = "xyz";
        Throwable cause = new Throwable("x");
        String expected = "[" + message + "]";

        ex = new StepRunException(message, cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isEqualTo(expected);
    }
}
