package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Data;
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

public class BaseProcessorTest {
    @InjectMocks
    private TestBaseProcessor baseProcessor;

    @Mock
    private Data data;
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

    static class TestBaseProcessor extends BaseProcessor {
        @Override
        public void process() {

        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitializeIf() throws Exception {
        Object grape = Mockito.mock(Object.class);
        Data pData = Mockito.mock(Data.class);

        when(payload.getData()).thenReturn(grape).thenReturn(pData);
        baseProcessor.initialize();

        assertSame(pData, FieldUtils.readField(baseProcessor, "data", true));
    }

    @Test
    public void testInitializeElse() {
        Object grape = Mockito.mock(Object.class);

        when(payload.getData()).thenReturn(grape);
        assertThrows(StepRunException.class, () -> baseProcessor.initialize());
    }

    @Test
    public void testLoad() {
        baseProcessor.load();
    }

    @Test
    public void testStore() {
        baseProcessor.store();
    }
}
