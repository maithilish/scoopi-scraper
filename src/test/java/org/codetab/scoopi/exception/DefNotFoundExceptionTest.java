package org.codetab.scoopi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DefNotFoundExceptionTest {

    @Test
    public void testException() {
        String message = "xyz";
        String expected = "[" + message + "]";

        DefNotFoundException ex = new DefNotFoundException(message);

        assertThat(ex.getMessage()).isEqualTo(expected);
    }
}
