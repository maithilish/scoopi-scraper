package org.codetab.scoopi.store.solo.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IJobStore.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JobStoreTest {
    @InjectMocks
    private JobStore jobStore;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOpen() {
        jobStore.open();
    }

    @Test
    public void testTakeJob() throws Exception {

        @SuppressWarnings("unchecked")
        Queue<Payload> jobs = Mockito.mock(ConcurrentLinkedQueue.class);
        Payload payload = Mockito.mock(Payload.class);

        FieldUtils.writeDeclaredField(jobStore, "jobs", jobs, true);

        when(jobs.poll()).thenReturn(payload);

        Payload actual = jobStore.takeJob();

        assertSame(payload, actual);
    }

    @Test
    public void testTakeJobPayloadIsNull() throws Exception {

        @SuppressWarnings("unchecked")
        Queue<Payload> jobs = Mockito.mock(ConcurrentLinkedQueue.class);

        FieldUtils.writeDeclaredField(jobStore, "jobs", jobs, true);

        when(jobs.poll()).thenReturn(null);

        assertThrows(NoSuchElementException.class, () -> jobStore.takeJob());
    }

    @Test
    public void testPutJob() throws Exception {
        Payload payload = Mockito.mock(Payload.class);

        boolean actual = jobStore.putJob(payload);

        assertTrue(actual);
    }

    @Test
    public void testSetState() {
        State state = IJobStore.State.INITIALIZE;
        jobStore.setState(state);
    }

    @Test
    public void testMarkFinished() {
        long id = 1L;

        boolean actual = jobStore.markFinished(id);

        assertTrue(actual);
    }

    @Test
    public void testIsDone() throws IllegalAccessException {
        int apple = 0;

        @SuppressWarnings("unchecked")
        Queue<Payload> jobs = Mockito.mock(ConcurrentLinkedQueue.class);

        FieldUtils.writeDeclaredField(jobStore, "jobs", jobs, true);

        when(jobs.size()).thenReturn(apple);

        boolean actual = jobStore.isDone();

        assertTrue(actual);
    }

    @Test
    public void testIsDoneNotDone() throws IllegalAccessException {
        int apple = 1;

        @SuppressWarnings("unchecked")
        Queue<Payload> jobs = Mockito.mock(ConcurrentLinkedQueue.class);

        FieldUtils.writeDeclaredField(jobStore, "jobs", jobs, true);

        when(jobs.size()).thenReturn(apple);

        boolean actual = jobStore.isDone();

        assertFalse(actual);
    }

    @Test
    public void testGetJobIdSeq() {
        assertEquals(0, jobStore.getJobIdSeq());
        assertEquals(1, jobStore.getJobIdSeq());
    }

    @Test
    public void testClose() {
        jobStore.close();
    }

    @Test
    public void testPutJobs() throws Exception {
        List<Payload> payloads = new ArrayList<>();
        long jobId = 1L;
        Payload payload = Mockito.mock(Payload.class);
        payloads.add(payload);

        boolean actual = jobStore.putJobs(payloads, jobId);

        assertTrue(actual);
    }

    @Test
    public void testResetCrashedJobs() {
        jobStore.resetCrashedJobs();
    }

    @Test
    public void testResetTakenJob() {
        long jobId = 1L;

        boolean actual = jobStore.resetTakenJob(jobId);

        assertTrue(actual);
    }
}
