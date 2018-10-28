package org.codetab.scoopi.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Optional;
import java.util.zip.DataFormatException;

import javax.script.ScriptException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.codetab.scoopi.defs.IDataDefDefs;
import org.codetab.scoopi.defs.ITaskDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.model.factory.DataFactory;
import org.codetab.scoopi.model.helper.DataHelper;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.persistence.DataPersistence;
import org.codetab.scoopi.step.TaskFactory;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.step.parse.IValueParser;
import org.codetab.scoopi.step.parse.ItemProcessor;
import org.codetab.scoopi.step.parse.ItemStack;
import org.codetab.scoopi.step.parse.ValueProcessor;
import org.codetab.scoopi.step.parse.jsoup.Parser;
import org.codetab.scoopi.step.parse.jsoup.ValueParser;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.system.Stats;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.codahale.metrics.Counter;

public class BaseParserTest {

    @Mock
    private ConfigService configService;
    @Mock
    private TaskFactory taskFactory;
    @Mock
    private Stats activityService;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDefs taskDefs;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private DataFactory dataFactory;

    @Mock
    private ItemStack itemStack;
    @Mock
    private ValueProcessor valueProcessor;
    @Mock
    private ItemProcessor itemProcessor;
    @Mock
    private DataPersistence dataPersistence;
    @Mock
    private IDataDefDefs dataDefDefs;
    @Mock
    private DataHelper dataHelper;

    @Spy
    private ValueParser valueParser;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private StopWatch timer;

    @InjectMocks
    private Parser parser;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Payload payload = getTestPayload();
        parser.setPayload(payload);
    }

    @Test
    public void testInitialize()
            throws IllegalAccessException, DataFormatException, IOException {
        Document document = (Document) parser.getPayload().getData();

        String html = "<html><body>test html</body></html>";
        byte[] bytes = html.getBytes();

        given(documentHelper.getDocumentObject(document)).willReturn(bytes);

        boolean actual = parser.initialize();

        IValueParser valParser = (IValueParser) FieldUtils.readField(parser,
                "valueParser", true);
        org.jsoup.nodes.Document page = (org.jsoup.nodes.Document) FieldUtils
                .readField(valParser, "page", true);

        assertThat(actual).isTrue();
        assertThat(valueParser).isSameAs(valParser);
        assertThat(page.text()).isEqualTo("test html");
    }

    @Test
    public void testInitializeInvalidPayloadData()
            throws IllegalAccessException, DataFormatException, IOException {
        Payload payload = factory.createPayload(null, null, "invalid document");
        parser.setPayload(payload);

        testRule.expect(StepRunException.class);
        parser.initialize();
    }

    @Test
    public void testInitializeNullParams() {
        try {
            parser.setPayload(null);
            parser.initialize();
            fail("should throw exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("payload is null");
        }

        try {
            Payload payload = factory.createPayload(null, null, null);
            parser.setPayload(payload);

            parser.initialize();
            fail("should throw exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("payload data is null");
        }
    }

    @Test
    public void testLoad()
            throws IllegalAccessException, DataDefNotFoundException {
        Document document = (Document) parser.getPayload().getData();
        document.setId(1L);
        FieldUtils.writeField(parser, "document", document, true);
        long documentId = document.getId();

        long dataDefId = 10L;
        String dataDefName = parser.getJobInfo().getDataDef();

        Data loadedData = factory.createData(dataDefName);

        given(dataDefDefs.getDataDefId(dataDefName)).willReturn(dataDefId);
        given(dataPersistence.loadData(dataDefId, documentId))
                .willReturn(loadedData);

        boolean actual = parser.load();
        Data actualData = (Data) FieldUtils.readField(parser, "data", true);

        assertThat(actual).isTrue();
        assertThat(actualData).isSameAs(loadedData);
    }

    @Test
    public void testLoadDataDefIdNull()
            throws IllegalAccessException, DataDefNotFoundException {
        Document document = (Document) parser.getPayload().getData();
        document.setId(1L);
        FieldUtils.writeField(parser, "document", document, true);

        Long dataDefId = null;
        String dataDefName = parser.getJobInfo().getDataDef();
        Date now = new Date();
        DataDef dataDef =
                factory.createDataDef(dataDefName, now, now, "defJson");
        dataDef.setId(dataDefId);

        given(dataDefDefs.getDataDefId(dataDefName)).willReturn(dataDefId);

        boolean actual = parser.load();
        Data actualData = (Data) FieldUtils.readField(parser, "data", true);

        assertThat(actual).isTrue();
        assertThat(actualData).isNull();
    }

    @Test
    public void testLoadDocAndDataDefIdNull()
            throws IllegalAccessException, DataDefNotFoundException {
        Document document = (Document) parser.getPayload().getData();
        document.setId(null);
        FieldUtils.writeField(parser, "document", document, true);

        Long dataDefId = null;
        String dataDefName = parser.getJobInfo().getDataDef();
        Date now = new Date();
        DataDef dataDef =
                factory.createDataDef(dataDefName, now, now, "defJson");
        dataDef.setId(dataDefId);

        given(dataDefDefs.getDataDefId(dataDefName)).willReturn(dataDefId);

        boolean actual = parser.load();
        Data actualData = (Data) FieldUtils.readField(parser, "data", true);

        assertThat(actual).isTrue();
        assertThat(actualData).isNull();
    }

    @Test
    public void testLoadShouldThrowException()
            throws IllegalAccessException, DataDefNotFoundException {
        String dataDefName = parser.getJobInfo().getDataDef();
        given(dataDefDefs.getDataDefId(dataDefName))
                .willThrow(DataDefNotFoundException.class);

        testRule.expect(StepRunException.class);
        parser.load();
    }

    @Test
    public void testSetValueParser() throws IllegalAccessException {
        IValueParser valParser = new ValueParser();
        parser.setValueParser(valParser);

        IValueParser actual = (IValueParser) FieldUtils.readField(parser,
                "valueParser", true);

        assertThat(actual).isSameAs(valParser);
    }

    @Test
    public void testProcess()
            throws DataDefNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException {
        Document document = (Document) parser.getPayload().getData();
        Long documentId = document.getId();
        FieldUtils.writeField(parser, "document", document, true);

        IValueParser valParser = new ValueParser();
        parser.setValueParser(valParser);

        Counter parseCounter = Mockito.mock(Counter.class);
        Counter reuseCounter = Mockito.mock(Counter.class);

        Date now = new Date();
        String dataDefName = parser.getJobInfo().getDataDef();
        Long dataDefId = 10L;
        DataDef dataDef = factory.createDataDef(dataDefName, now, now, "def");
        dataDef.setId(dataDefId);

        String label = parser.getJobInfo().getLabel();

        Item item1 = factory.createItem();
        item1.setName("m1");
        Item item2 = factory.createItem();
        item1.setName("m2");

        Data data = factory.createData(dataDefName);
        data.setDocumentId(documentId);
        data.setDataDefId(dataDefId);

        data.addItem(item1);
        data.addItem(item2);

        given(metricsHelper.getCounter(parser, "data", "parse"))
                .willReturn(parseCounter);
        given(metricsHelper.getCounter(parser, "data", "reuse"))
                .willReturn(reuseCounter);
        given(dataFactory.createData(dataDefName, documentId, label))
                .willReturn(data);
        given(dataDefDefs.getDataDef(dataDefName)).willReturn(dataDef);
        given(dataDefDefs.getDataDefId(dataDefName)).willReturn(dataDefId);
        given(itemStack.isEmpty()).willReturn(false, false, true);
        given(itemStack.popItem()).willReturn(item1, item2);

        boolean actual = parser.process();

        assertThat(actual).isTrue();
        assertThat(parser.getOutput()).isInstanceOf(Data.class);
        Data actualData = (Data) parser.getOutput();
        assertThat(actualData).isEqualTo(data);
        assertThat(actualData.getItems()).containsExactly(item1, item2);
        assertThat(parser.isConsistent()).isTrue();

        verify(valueProcessor).addScriptObject("document", document);
        verify(valueProcessor).addScriptObject("configs", configService);
        verify(itemStack).pushItems(data.getItems());
        verify(valueProcessor).setAxisValues(dataDef, item1, valParser);
        verify(itemStack).pushAdjacentItems(dataDef, item1);
        verify(valueProcessor).setAxisValues(dataDef, item2, valParser);
        verify(itemStack).pushAdjacentItems(dataDef, item2);
        verify(parseCounter).inc();

        verifyZeroInteractions(reuseCounter);
    }

    @Test
    public void testProcessReuseData()
            throws DataDefNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException {
        String dataDefName = parser.getJobInfo().getDataDef();
        Data data = factory.createData(dataDefName);
        FieldUtils.writeField(parser, "data", data, true);

        Counter parseCounter = Mockito.mock(Counter.class);
        Counter reuseCounter = Mockito.mock(Counter.class);

        given(metricsHelper.getCounter(parser, "data", "parse"))
                .willReturn(parseCounter);
        given(metricsHelper.getCounter(parser, "data", "reuse"))
                .willReturn(reuseCounter);

        boolean actual = parser.process();

        assertThat(actual).isTrue();

        assertThat(parser.getOutput()).isInstanceOf(Data.class);
        Data actualData = (Data) parser.getOutput();
        assertThat(actualData).isEqualTo(data);
        assertThat(parser.isConsistent()).isTrue();

        verify(reuseCounter).inc();

        verifyZeroInteractions(parseCounter);
    }

    @Test
    public void testProcessShouldThrowException()
            throws DataDefNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ScriptException {
        Document document = (Document) parser.getPayload().getData();
        Long documentId = document.getId();
        FieldUtils.writeField(parser, "document", document, true);

        Counter parseCounter = Mockito.mock(Counter.class);
        Counter reuseCounter = Mockito.mock(Counter.class);

        String dataDefName = parser.getJobInfo().getDataDef();
        Data data = factory.createData(dataDefName);

        String label = parser.getJobInfo().getLabel();

        given(metricsHelper.getCounter(parser, "data", "parse"))
                .willReturn(parseCounter);
        given(metricsHelper.getCounter(parser, "data", "reuse"))
                .willReturn(reuseCounter);
        given(dataFactory.createData(dataDefName, documentId, label))
                .willReturn(data);
        given(dataFactory.createData(dataDefName, documentId, label))
                .willThrow(DataDefNotFoundException.class);
        given(itemStack.isEmpty()).willReturn(false, false, true);

        testRule.expect(StepRunException.class);
        parser.process();
    }

    @Test
    public void testStore()
            throws IllegalAccessException, DefNotFoundException {
        Data data = factory.createData("price");
        data.setId(1L);
        Data loadedData = factory.createData("price");
        data.setId(2L);

        Optional<Boolean> taskLevelPersistenceDefined = Optional.of(true);

        FieldUtils.writeField(parser, "data", data, true);
        given(taskDefs.getFieldValue("quote", "price", "persist", "data"))
                .willReturn("true");
        given(dataPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true);
        given(dataPersistence.storeData(data)).willReturn(true);
        given(dataPersistence.loadData(data.getId())).willReturn(loadedData);

        boolean actual = parser.store();

        assertThat(actual).isTrue();

        Data actualData = (Data) FieldUtils.readField(parser, "data", true);
        Object actualOutput = parser.getOutput();

        assertThat(actualData).isSameAs(loadedData);
        assertThat(actualOutput).isSameAs(loadedData);
    }

    @Test
    public void testStoreStoreDataFalse()
            throws IllegalAccessException, DefNotFoundException {
        Data data = factory.createData("price");
        data.setId(1L);

        Optional<Boolean> taskLevelPersistenceDefined = Optional.of(false);
        FieldUtils.writeField(parser, "data", data, true);
        given(taskDefs.getFieldValue("quote", "price", "persist", "data"))
                .willReturn("false");
        given(dataPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true);
        given(dataPersistence.storeData(data)).willReturn(false);

        boolean actual = parser.store();

        assertThat(actual).isTrue();

        Data actualData = (Data) FieldUtils.readField(parser, "data", true);

        assertThat(actualData).isSameAs(data);
        verify(dataPersistence).persist(eq(taskLevelPersistenceDefined));
        verify(dataPersistence).storeData(data);
        verifyNoMoreInteractions(dataPersistence);
    }

    @Test
    public void testStorePersistFalse()
            throws IllegalAccessException, DefNotFoundException {
        given(taskDefs.getFieldValue("quote", "price", "persist", "data"))
                .willReturn("false");

        boolean actual = parser.store();
        assertThat(actual).isTrue();

        actual = parser.store();
        assertThat(actual).isTrue();

        verify(dataPersistence, times(2)).persist(Optional.of(false));
        verifyNoMoreInteractions(dataPersistence);
    }

    @Test
    public void testStoreTaskLevelPersistenceNotDefined()
            throws IllegalAccessException, DefNotFoundException {
        Data data = factory.createData("price");
        data.setId(1L);
        Data loadedData = factory.createData("price");
        data.setId(2L);

        Optional<Boolean> taskLevelPersistenceDefined = Optional.of(true);

        FieldUtils.writeField(parser, "data", data, true);
        given(taskDefs.getFieldValue("quote", "price", "persist", "data"))
                .willThrow(DefNotFoundException.class);
        given(dataPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true);
        given(dataPersistence.storeData(data)).willReturn(true);
        given(dataPersistence.loadData(data.getId())).willReturn(loadedData);

        boolean actual = parser.store();

        assertThat(actual).isTrue();

        Data actualData = (Data) FieldUtils.readField(parser, "data", true);
        Object actualOutput = parser.getOutput();

        assertThat(actualData).isSameAs(loadedData);
        assertThat(actualOutput).isSameAs(loadedData);
    }

    public Payload getTestPayload() {
        JobInfo jobInfo = factory.createJobInfo(0, "acme", "quote", "price",
                "steps", "price");
        StepInfo stepInfo = factory.createStepInfo("s1", "s0", "s2", "clzName");

        Document document =
                factory.createDocument("acme", "url", new Date(), new Date());
        document.setDocumentObject("test doc");

        return factory.createPayload(jobInfo, stepInfo, document);
    }
}
