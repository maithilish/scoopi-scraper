package org.codetab.scoopi.step.extract;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.model.helper.Documents;
import org.codetab.scoopi.plugin.script.ScriptExecutor;
import org.codetab.scoopi.step.base.FetchThrottle;
import org.codetab.scoopi.step.base.PayloadFactory;
import org.codetab.scoopi.step.base.Persists;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.codetab.scoopi.step.webdriver.WebDriverPool;
import org.codetab.scoopi.step.webdriver.WebDrivers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;

public class DomLoaderTest {
    @InjectMocks
    private DomLoader domLoader;

    @Mock
    private IPluginDef pluginDef;
    @Mock
    private WebDriverPool webDriverPool;
    @Mock
    private WebDrivers webDrivers;
    @Mock
    private ScriptExecutor scriptExecutor;
    @Mock
    private IDocumentDao documentDao;
    @Mock
    private Documents documents;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private PayloadFactory payloadFactory;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private Persists persists;
    @Mock
    private FetchThrottle fetchThrottle;
    @Mock
    private Locator locator;
    @Mock
    private Document document;
    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
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
    public void testFetchDocumentObject() throws Exception {
        String url = "Foo";
        JobInfo apple = Mockito.mock(JobInfo.class);
        String taskGroup = "Bar";
        String taskName = "Baz";
        StepInfo orange = Mockito.mock(StepInfo.class);
        String kiwi = "Qux";
        String stepName = kiwi;
        Optional<List<Plugin>> plugins = Optional.of(new ArrayList<Plugin>());
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        String pageSrc = "Corge";
        byte[] cherry = {'C', 'o', 'r', 'g', 'e'};

        when(payload.getJobInfo()).thenReturn(apple);
        when(apple.getGroup()).thenReturn(taskGroup);
        when(apple.getTask()).thenReturn(taskName);
        when(payload.getStepInfo()).thenReturn(orange);
        when(orange.getStepName()).thenReturn(kiwi);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(webDriverPool.borrowObject()).thenReturn(webDriver);
        when(webDriver.getPageSource()).thenReturn(pageSrc);

        byte[] actual = domLoader.fetchDocumentObject(url);

        assertArrayEquals(cherry, actual);
        verify(webDriver).get(url);
        verify(webDrivers, times(2)).explicitlyWaitForDomReady(webDriver);
        verify(scriptExecutor).execute(plugins.get(), webDriver);
        verify(webDriverPool).returnObject(webDriver);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFetchDocumentObjectNoPlugins() throws Exception {
        String url = "Foo";
        JobInfo apple = Mockito.mock(JobInfo.class);
        String taskGroup = "Bar";
        String taskName = "Baz";
        StepInfo orange = Mockito.mock(StepInfo.class);
        String kiwi = "Qux";
        String stepName = kiwi;
        Optional<List<Plugin>> plugins = Optional.empty();
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        String pageSrc = "Corge";
        byte[] cherry = {'C', 'o', 'r', 'g', 'e'};

        when(payload.getJobInfo()).thenReturn(apple);
        when(apple.getGroup()).thenReturn(taskGroup);
        when(apple.getTask()).thenReturn(taskName);
        when(payload.getStepInfo()).thenReturn(orange);
        when(orange.getStepName()).thenReturn(kiwi);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(webDriverPool.borrowObject()).thenReturn(webDriver);
        when(webDriver.getPageSource()).thenReturn(pageSrc);

        byte[] actual = domLoader.fetchDocumentObject(url);

        assertArrayEquals(cherry, actual);
        verify(webDriver).get(url);
        verify(webDrivers, times(2)).explicitlyWaitForDomReady(webDriver);
        verify(scriptExecutor, never()).execute(any(List.class), eq(webDriver));
        verify(webDriverPool).returnObject(webDriver);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFetchDocumentObjectWebDriverIsNullException()
            throws Exception {
        String url = "Foo";
        JobInfo apple = Mockito.mock(JobInfo.class);
        String taskGroup = "Bar";
        String taskName = "Baz";
        StepInfo orange = Mockito.mock(StepInfo.class);
        String kiwi = "Qux";
        String stepName = kiwi;
        Optional<List<Plugin>> plugins = Optional.empty();
        WebDriver webDriver = null;

        when(payload.getJobInfo()).thenReturn(apple);
        when(apple.getGroup()).thenReturn(taskGroup);
        when(apple.getTask()).thenReturn(taskName);
        when(payload.getStepInfo()).thenReturn(orange);
        when(orange.getStepName()).thenReturn(kiwi);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(webDriverPool.borrowObject()).thenReturn(webDriver);

        assertThrows(StepRunException.class,
                () -> domLoader.fetchDocumentObject(url));

        verify(webDrivers, never()).explicitlyWaitForDomReady(webDriver);
        verify(scriptExecutor, never()).execute(any(List.class), eq(webDriver));
        verify(webDriverPool, never()).returnObject(webDriver);
    }
}
