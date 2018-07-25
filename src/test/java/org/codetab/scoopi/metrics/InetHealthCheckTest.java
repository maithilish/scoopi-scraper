package org.codetab.scoopi.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.codahale.metrics.health.HealthCheck.Result;

public class InetHealthCheckTest {

    private InetHealthCheck ihc;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ihc = new InetHealthCheck();
    }

    @Test
    public void testCheck() throws Exception {
        ihc.setUrl("file:///tmp");
        Result result = ihc.check();
        assertThat(result.isHealthy()).isTrue();

        ihc.setUrl("file:///tmpx");
        result = ihc.check();
        assertThat(result.isHealthy()).isFalse();
    }

    @Test
    public void testCheckShouldThrowException() throws Exception {
        ihc.setUrl("xyz");

        try {
            ihc.check();
            fail("should throw RuntimeException");
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(MalformedURLException.class);
        }
    }
}
