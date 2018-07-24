package org.codetab.scoopi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * <p>
 * Exception tests.
 * @author Maithilish
 *
 */
public class DataDefNotFoundExceptionTest {

    @Test
    public void testException() {
        String message = "xyz";
        String expected = "[" + message + "]";

        DataDefNotFoundException ex = new DataDefNotFoundException(message);

        assertThat(ex.getMessage()).isEqualTo(expected);
    }
}
