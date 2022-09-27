package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BaseScripterTest {
    @InjectMocks
    private TestBaseScripter baseScripter;

    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private ObjectFactory factory;
    @Mock
    private Object output;
    @Mock
    private Payload payload;
    @Mock
    private Marker jobMarker;
    @Mock
    private Marker jobAbortedMarker;

    static class TestBaseScripter extends BaseScripter {
        @Override
        public void process() {
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitialize() throws Exception {
        Object grape = Mockito.mock(Object.class);
        Object input = Mockito.mock(Object.class);

        when(payload.getData()).thenReturn(grape).thenReturn(input);
        baseScripter.initialize();

        assertEquals(input, FieldUtils.readField(baseScripter, "input", true));
    }

    @Test
    public void testLoad() {
        baseScripter.load();
        verifyNoInteractions(configs, metricsHelper, taskDef, taskMediator,
                jobMediator, factory, output, payload, jobMarker,
                jobAbortedMarker);
    }

    @Test
    public void testStore() {
        baseScripter.store();
        verifyNoInteractions(configs, metricsHelper, taskDef, taskMediator,
                jobMediator, factory, output, payload, jobMarker,
                jobAbortedMarker);
    }
}
