package org.codetab.scoopi.pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.TestTimedOutException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * Pool test. Covers both TaskPoolService and AppenderPoolService.
 * @author Maithilish
 *
 */
public class PoolsTest {

    @Mock
    private ConfigService configService;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private DInjector dInjector;
    @Mock
    private PoolStat poolStat;

    @InjectMocks
    private TaskPoolService pools;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubmit() {
        String poolName = "x";
        Runnable task = () -> {
        };

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        boolean actual = pools.submit(poolName, task);

        assertThat(actual).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSubmitToExistingPool() throws IllegalAccessException {

        // create and set mocks
        // we need mocks in this test to check internals of method
        Map<String, ExecutorService> executorsMap = Mockito.mock(Map.class);
        FieldUtils.writeField(pools, "executorsMap", executorsMap, true);

        ArrayList<NamedFuture> futures = Mockito.spy(ArrayList.class);
        FieldUtils.writeField(pools, "futures", futures, true);

        ExecutorService executor = Mockito.mock(ExecutorService.class);

        String poolName = "x";
        Runnable task = () -> {
        };
        @SuppressWarnings("rawtypes")
        Future future = new CompletableFuture<>();

        given(executorsMap.get(poolName)).willReturn(executor);
        given(executor.submit(task)).willReturn(future);

        // when
        boolean actual = pools.submit(poolName, task);

        assertThat(actual).isTrue();
        InOrder inOrder = inOrder(executor, futures, executorsMap);
        inOrder.verify(executorsMap).get(poolName);
        inOrder.verify(executor).submit(task);
        inOrder.verify(futures).add(any(NamedFuture.class));
        verifyNoMoreInteractions(executor, futures, executorsMap);

        // check whether future added to list as expected
        assertThat(futures.size()).isEqualTo(1);
        assertThat(futures.get(0).getPoolName()).isEqualTo(poolName);
        assertThat(futures.get(0).getFuture()).isEqualTo(future);
    }

    @Test
    public void testNewPoolCreationDefaultPoolSize()
            throws IllegalAccessException {

        String poolName = "x";
        Runnable task = () -> {
        };

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        // when
        boolean actual = pools.submit(poolName, task);

        assertThat(actual).isTrue();

        @SuppressWarnings("unchecked")
        Map<String, ExecutorService> executorsMap =
                (Map<String, ExecutorService>) FieldUtils.readField(pools,
                        "executorsMap", true);

        assertThat(executorsMap.size()).isEqualTo(1);
        ThreadPoolExecutor executor =
                (ThreadPoolExecutor) executorsMap.get(poolName);

        // no other way to assert that it is a FixedPoolExecutor
        assertThat(executor.getCorePoolSize()).isEqualTo(4);
        assertThat(executor.getMaximumPoolSize()).isEqualTo(4);
        verify(metricsHelper).registerGuage(any(PoolStat.class), eq(pools),
                eq("pool"), eq("x"));
    }

    @Test
    public void testNewPoolCreationConfiguredPoolSize()
            throws IllegalAccessException, ConfigNotFoundException {

        String poolName = "x";
        Runnable task = () -> {
        };

        given(configService.getConfig("scoopi.poolsize.x")).willReturn("11");
        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        // when
        boolean actual = pools.submit(poolName, task);

        assertThat(actual).isTrue();

        @SuppressWarnings("unchecked")
        Map<String, ExecutorService> executorsMap =
                (Map<String, ExecutorService>) FieldUtils.readField(pools,
                        "executorsMap", true);

        assertThat(executorsMap.size()).isEqualTo(1);
        ThreadPoolExecutor executor =
                (ThreadPoolExecutor) executorsMap.get(poolName);

        // no other way to assert that it is a FixedPoolExecutor
        assertThat(executor.getCorePoolSize()).isEqualTo(11);
        assertThat(executor.getMaximumPoolSize()).isEqualTo(11);
    }

    @Test
    public void testSubmitSynchornized()
            throws InterruptedException, IllegalAccessException {

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        // lockdown pools
        LockDown lockDown = new LockDown();
        lockDown.setLock(pools);
        lockDown.start();
        while (lockDown.getState() != State.RUNNABLE) {
            Thread.sleep(100);
        }

        Thread testMethodRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                Runnable task = () -> {
                };
                pools.submit("x", task);
            }
        });
        testMethodRunner.start();
        while (testMethodRunner.getState() == State.NEW
                || testMethodRunner.getState() == State.RUNNABLE) {
            Thread.sleep(100);
        }

        // test whether lockDown is active and test method is in wait
        assertThat(lockDown.isAlive()).isTrue();
        assertThat(testMethodRunner.isAlive()).isTrue();

        lockDown.setDone(true);
        testMethodRunner.join();
        lockDown.join();
    }

    @Test
    public void testSubmitNullParams() {
        Runnable task = () -> {
        };

        try {
            pools.submit(null, task);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("poolName must not be null");
        }

        try {
            pools.submit("x", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("task must not be null");
        }
    }

    @Test
    public void testIsDone() {
        assertThat(pools.isDone()).isTrue();

        Task task = new Task();

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        pools.submit("x", task);

        assertThat(pools.isDone()).isFalse();

        task.setDone(true);

        while (!pools.isDone()) {
            sleep(100);
        }

        assertThat(pools.isDone()).isTrue();
    }

    @Test
    public void testIsDoneSynchornized()
            throws InterruptedException, IllegalAccessException {

        // lockdown pools
        LockDown lockDown = new LockDown();
        lockDown.setLock(pools);
        lockDown.start();
        while (lockDown.getState() != State.RUNNABLE) {
            Thread.sleep(100);
        }

        Thread testMethodRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                pools.isDone();
            }
        });
        testMethodRunner.start();
        while (testMethodRunner.getState() == State.NEW
                || testMethodRunner.getState() == State.RUNNABLE) {
            Thread.sleep(100);
        }

        // test whether lockDown is active and test method is in wait
        assertThat(lockDown.isAlive()).isTrue();
        assertThat(testMethodRunner.isAlive()).isTrue();

        lockDown.setDone(true);
        testMethodRunner.join();
        lockDown.join();
    }

    @Test
    public void testShutdownAll() throws IllegalAccessException {

        ExecutorService e1 = Executors.newFixedThreadPool(2);
        ExecutorService e2 = Executors.newFixedThreadPool(2);

        Map<String, ExecutorService> executorsMap = new HashMap<>();
        executorsMap.put("x", e1);
        executorsMap.put("y", e2);

        FieldUtils.writeField(pools, "executorsMap", executorsMap, true);

        assertThat(e1.isShutdown()).isFalse();
        assertThat(e2.isShutdown()).isFalse();

        pools.shutdownAll();

        assertThat(e1.isShutdown()).isTrue();
        assertThat(e2.isShutdown()).isTrue();
    }

    @Test(timeout = 1000)
    public void testWaitForFinish() {
        Task task1 = new Task();

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        pools.submit("x", task1);

        exceptionRule.expect(TestTimedOutException.class);
        pools.waitForFinish();
    }

    @Test(timeout = 5000)
    public void testWaitForFinishTaskFinished() {
        Task task1 = new Task();

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        pools.submit("x", task1);

        task1.setDone(true);
        sleep(200);

        pools.waitForFinish();
        assertThat(pools.isDone()).isTrue();
    }

    @Test(timeout = 5000)
    public void testWaitForFinishEndTaskFromOtherThread() {
        Task task1 = new Task();

        given(dInjector.instance(PoolStat.class)).willReturn(poolStat);

        pools.submit("x", task1);

        assertThat(pools.isDone()).isFalse();

        Runnable task = () -> {
            sleep(500);
            task1.setDone(true);
        };
        new Thread(task).run();

        pools.waitForFinish();
        assertThat(pools.isDone()).isTrue();
    }

    @Test
    public void testShutdownAllSynchornized()
            throws InterruptedException, IllegalAccessException {

        // lockdown pools
        LockDown lockDown = new LockDown();
        lockDown.setLock(pools);
        lockDown.start();
        while (lockDown.getState() != State.RUNNABLE) {
            Thread.sleep(100);
        }

        Thread testMethodRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                pools.shutdownAll();
            }
        });
        testMethodRunner.start();
        while (testMethodRunner.getState() == State.NEW
                || testMethodRunner.getState() == State.RUNNABLE) {
            Thread.sleep(100);
        }

        // test whether lockDown is active and test method is in wait
        assertThat(lockDown.isAlive()).isTrue();
        assertThat(testMethodRunner.isAlive()).isTrue();

        lockDown.setDone(true);
        testMethodRunner.join();
        lockDown.join();
    }

    private void sleep(final int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * Simple task.
     * @author Maithilish
     *
     */
    private class Task implements Runnable {

        private volatile boolean done = false;

        @Override
        public void run() {
            while (!isDone()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }

        public boolean isDone() {
            return done;
        }

        public void setDone(final boolean done) {
            this.done = done;
        }
    }

    /**
     * <p>
     * Acquires implicit lock of an object and blocks access till done is true.
     * @author Maithilish
     *
     */
    private class LockDown extends Thread {

        private volatile boolean done = false;
        private volatile Object lock;

        @Override
        public void run() {
            synchronized (lock) {
                while (!done) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public void setDone(final boolean done) {
            this.done = done;
        }

        public void setLock(final Object lock) {
            this.lock = lock;
        }
    }

}
