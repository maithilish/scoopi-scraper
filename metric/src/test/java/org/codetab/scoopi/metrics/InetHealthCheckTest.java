package org.codetab.scoopi.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.health.HealthCheck.Result;

public class InetHealthCheckTest {
    @InjectMocks
    private InetHealthCheck inetHealthCheck;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetUrl() {
        String url = "file:///etc/passwd";
        inetHealthCheck.setUrl(url);
    }

    @Test
    public void testCheck() throws Exception {
        String url = "file:///etc/hostname";
        inetHealthCheck.setUrl(url);

        Result actual = inetHealthCheck.check();

        assertTrue(actual.isHealthy());
    }

    @Test
    public void testCheckUnhealthy() throws Exception {
        String url = "file:///etc/hosty";
        inetHealthCheck.setUrl(url);

        Result actual = inetHealthCheck.check();

        assertFalse(actual.isHealthy());
        assertEquals("net is down", actual.getMessage());
    }

    @Test
    public void testCheckMalformedUrl() throws Exception {
        String url = "f/etc/hosty";
        inetHealthCheck.setUrl(url);

        assertThrows(RuntimeException.class, () -> inetHealthCheck.check());
    }
}
