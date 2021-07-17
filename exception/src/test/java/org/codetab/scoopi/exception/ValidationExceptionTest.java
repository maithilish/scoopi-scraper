package org.codetab.scoopi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ValidationExceptionTest {

    @Test
    public void testException() {
        String message = "xyz";

        ValidationException ex = new ValidationException(message);

        assertThat(ex.getMessage()).isEqualTo(message);
    }
}
