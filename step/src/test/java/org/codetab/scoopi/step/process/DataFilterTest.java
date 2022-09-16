package org.codetab.scoopi.step.process;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.JobInfo;
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

public class DataFilterTest {
    @InjectMocks
    private DataFilter dataFilter;

    @Mock
    private FilterHelper filterHelper;
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessIf() {
        List<DataComponent> items = new ArrayList<>();
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String dataDef = "Foo";

        Item item = Mockito.mock(Item.class);
        List<Item> list = new ArrayList<>();
        list.add(item);

        boolean apple = true;

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getDataDef()).thenReturn(dataDef);
        when(data.getItems()).thenReturn(list);
        when(filterHelper.filter(item, dataDef)).thenReturn(apple);
        dataFilter.process();

        verify(data).setItems(items);
    }

    @Test
    public void testProcessElse() {
        List<DataComponent> items = new ArrayList<>();
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String dataDef = "Foo";

        Item item = Mockito.mock(Item.class);
        List<Item> list = new ArrayList<>();
        list.add(item);

        items.add(item);

        boolean apple = false;

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getDataDef()).thenReturn(dataDef);
        when(data.getItems()).thenReturn(list);
        when(filterHelper.filter(item, dataDef)).thenReturn(apple);
        dataFilter.process();

        verify(data).setItems(eq(items));
    }
}
