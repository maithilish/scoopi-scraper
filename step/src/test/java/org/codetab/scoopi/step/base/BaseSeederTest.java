package org.codetab.scoopi.step.base;

import static org.mockito.Mockito.verifyNoInteractions;

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
import org.mockito.MockitoAnnotations;

public class BaseSeederTest {

    @InjectMocks
    private TestBaseSeeder testBaseSeeder;

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

    static class TestBaseSeeder extends BaseSeeder {
        @Override
        public void initialize() {
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitialize() {
        testBaseSeeder.initialize();
        verifyNoInteractions(configs, metricsHelper, taskDef, taskMediator,
                jobMediator, factory, output, payload, jobMarker,
                jobAbortedMarker);
    }

    @Test
    public void testLoad() {
        testBaseSeeder.load();
        verifyNoInteractions(configs, metricsHelper, taskDef, taskMediator,
                jobMediator, factory, output, payload, jobMarker,
                jobAbortedMarker);
    }

    @Test
    public void testStore() {
        testBaseSeeder.store();
        verifyNoInteractions(configs, metricsHelper, taskDef, taskMediator,
                jobMediator, factory, output, payload, jobMarker,
                jobAbortedMarker);
    }

    @Test
    public void testProcess() {
        testBaseSeeder.process();
        verifyNoInteractions(configs, metricsHelper, taskDef, taskMediator,
                jobMediator, factory, output, payload, jobMarker,
                jobAbortedMarker);
    }

    @Test
    public void testHandover() {
        testBaseSeeder.process();
    }
}
