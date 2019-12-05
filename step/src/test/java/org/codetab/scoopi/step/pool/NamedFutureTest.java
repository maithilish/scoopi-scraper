package org.codetab.scoopi.step.pool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * NamedFuture test.
 * @author Maithilish
 *
 */
public class NamedFutureTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testNamedFuture() {
        Future<?> future = new CompletableFuture<>();

        NamedFuture ns = new NamedFuture("x", future);

        assertThat(ns.getPoolName()).isEqualTo("x");
        assertThat(ns.getFuture()).isEqualTo(future);
    }

    @Test
    public void testIsDone() {
        CompletableFuture<String> future = new CompletableFuture<>();

        NamedFuture ns = new NamedFuture("x", future);

        assertThat(ns.isDone()).isFalse();

        future.complete("x");

        assertThat(ns.isDone()).isTrue();
    }

}
