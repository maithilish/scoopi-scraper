package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.model.helper.DataHelper;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.codetab.scoopi.step.parse.IValueParser;
import org.codetab.scoopi.step.parse.Indexer;
import org.codetab.scoopi.step.parse.IndexerFactory;
import org.codetab.scoopi.step.parse.ValueProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;

public class BaseParserTest {
    @InjectMocks
    private TestBaseParser baseParser;

    @Mock
    private ValueProcessor valueProcessor;
    @Mock
    private DataFactory dataFactory;
    @Mock
    private IDataDefDef dataDefDef;
    @Mock
    private IDataDao dataDao;
    @Mock
    private DataHelper dataHelper;
    @Mock
    private StopWatch timer;
    @Mock
    private IndexerFactory indexerFactory;
    @Mock
    private Persists persists;
    @Mock
    private Data data;
    @Mock
    private Document document;
    @Mock
    private IValueParser valueParser;
    @Mock
    private Fingerprint dataFingerprint;
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

    static class TestBaseParser extends BaseParser {

        @Override
        protected boolean postInitialize() {
            return true;
        }

    }

    @Test
    public void testInitializeIf() {
        Object grape = Mockito.mock(Object.class);
        Document pData = Mockito.mock(Document.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String mango = "Foo";
        String banana = "Bar";
        String cherry = "Baz";
        String apricot = "Qux";
        String peach = "Quux";
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        String fig = "Corge";
        boolean persist = true;

        when(payload.getData()).thenReturn(grape).thenReturn(pData);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getName()).thenReturn(mango);
        when(jobInfo.getGroup()).thenReturn(banana);
        when(jobInfo.getTask()).thenReturn(cherry);
        when(jobInfo.getDataDef()).thenReturn(apricot).thenReturn(peach);
        when(dataDefDef.getFingerprint(peach)).thenReturn(fingerprint);
        when(fingerprint.getValue()).thenReturn(fig);
        when(persists.persistData(jobInfo)).thenReturn(persist);

        baseParser.initialize();

        verify(timer).start();
        verify(pData).decompress();
    }

    @Test
    public void testInitializeElse() {
        Object grape = Mockito.mock(Object.class);
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String mango = "Foo";
        String banana = "Bar";
        String cherry = "Baz";
        String apricot = "Qux";
        String peach = "Quux";
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        String fig = "Corge";
        boolean persist = true;

        when(payload.getData()).thenReturn(grape); // if not Document
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getName()).thenReturn(mango);
        when(jobInfo.getGroup()).thenReturn(banana);
        when(jobInfo.getTask()).thenReturn(cherry);
        when(jobInfo.getDataDef()).thenReturn(apricot).thenReturn(peach);
        when(dataDefDef.getFingerprint(peach)).thenReturn(fingerprint);
        when(fingerprint.getValue()).thenReturn(fig);
        when(persists.persistData(jobInfo)).thenReturn(persist);

        assertThrows(StepRunException.class, () -> baseParser.initialize());

        verify(timer).start();
        verifyNoInteractions(grape);
    }

    @Test
    public void testPostInitialize() {

        boolean actual = baseParser.postInitialize();

        assertTrue(actual);
    }

    @Test
    public void testLoadTryTryIfNonNull() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        Data data1 = Mockito.mock(Data.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        Fingerprint fingerprint2 = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String barracuda = "Quux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String bionic = "Corge";

        FieldUtils.writeField(baseParser, "persist", true, true);

        when(document.getLocatorId()).thenReturn(fingerprint);
        when(dataDao.get(fingerprint, dataFingerprint)).thenReturn(data1);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);
        when(stepInfo3.getStepName()).thenReturn(barracuda);
        when(jobInfo3.getLabel()).thenReturn(bionic);

        baseParser.load();

        assertFalse(
                (Boolean) FieldUtils.readField(baseParser, "parseData", true));

        verify(document, times(1)).getLocatorId();
        verify(dataDao, never()).delete(fingerprint2, dataFingerprint);
    }

    @Test
    public void testLoadTryTryElseNonNull() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        Data data1 = null; // nonNull(data)
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        Fingerprint fingerprint2 = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String barracuda = "Quux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String bionic = "Corge";

        FieldUtils.writeField(baseParser, "persist", true, true);

        when(document.getLocatorId()).thenReturn(fingerprint);
        when(dataDao.get(fingerprint, dataFingerprint)).thenReturn(data1);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);
        when(stepInfo3.getStepName()).thenReturn(barracuda);
        when(jobInfo3.getLabel()).thenReturn(bionic);

        baseParser.load();

        assertTrue(
                (Boolean) FieldUtils.readField(baseParser, "parseData", true));

        verify(document, times(1)).getLocatorId();
        verify(dataDao, never()).delete(fingerprint2, dataFingerprint);
    }

    @Test
    public void testLoadTryTryCatchChecksumException() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        Fingerprint fingerprint2 = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String barracuda = "Quux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String bionic = "Corge";

        FieldUtils.writeField(baseParser, "persist", true, true);

        when(document.getLocatorId()).thenReturn(fingerprint)
                .thenReturn(fingerprint2);

        when(dataDao.get(fingerprint, dataFingerprint))
                .thenThrow(ChecksumException.class);

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);
        when(stepInfo3.getStepName()).thenReturn(barracuda);
        when(jobInfo3.getLabel()).thenReturn(bionic);

        baseParser.load();

        assertFalse(
                (Boolean) FieldUtils.readField(baseParser, "parseData", true));

        verify(dataDao).delete(fingerprint2, dataFingerprint);
    }

    @Test
    public void testLoadIfPersistTryCatchDaoException() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        Fingerprint fingerprint2 = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String barracuda = "Quux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String bionic = "Corge";

        FieldUtils.writeField(baseParser, "persist", true, true);

        when(document.getLocatorId()).thenReturn(fingerprint)
                .thenReturn(fingerprint2);

        when(dataDao.get(fingerprint, dataFingerprint))
                .thenThrow(DaoException.class);

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);
        when(stepInfo3.getStepName()).thenReturn(barracuda);
        when(jobInfo3.getLabel()).thenReturn(bionic);

        baseParser.load();

        assertTrue(
                (Boolean) FieldUtils.readField(baseParser, "parseData", true));

        verify(document, times(1)).getLocatorId();
        verify(dataDao, never()).delete(fingerprint2, dataFingerprint);
    }

    @Test
    public void testLoadElsePersistTryTry() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        Data data1 = Mockito.mock(Data.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        Fingerprint fingerprint2 = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String barracuda = "Quux";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String bionic = "Corge";

        when(document.getLocatorId()).thenReturn(fingerprint);
        when(dataDao.get(fingerprint, dataFingerprint)).thenReturn(data1);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);
        when(stepInfo3.getStepName()).thenReturn(barracuda);
        when(jobInfo3.getLabel()).thenReturn(bionic);
        baseParser.load();

        verify(document, never()).getLocatorId();
        verify(dataDao, never()).delete(fingerprint2, dataFingerprint);
    }

    @Test
    public void testStoreIfParseDataTry() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";

        FieldUtils.writeField(baseParser, "persist", true, true);

        when(document.getLocatorId()).thenReturn(fingerprint);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);

        baseParser.store();

        assertSame(data, baseParser.getOutput());

        verify(dataDao).save(fingerprint, dataFingerprint, data);
    }

    @Test
    public void testStoreIfParseDataTryCatchDaoException() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";

        FieldUtils.writeField(baseParser, "persist", true, true);

        when(document.getLocatorId()).thenReturn(fingerprint);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);
        doThrow(DaoException.class).when(dataDao).save(fingerprint,
                dataFingerprint, data);

        assertThrows(StepRunException.class, () -> baseParser.store());
    }

    @Test
    public void testStoreElseParseDataIfPersist() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";

        FieldUtils.writeField(baseParser, "persist", false, true);

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);

        baseParser.store();

        verify(document, never()).getLocatorId();
        verify(dataDao, never()).save(fingerprint, dataFingerprint, data);
    }

    @Test
    public void testStoreElseParseDataElsePersist() throws Exception {
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String peach = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String fig = "Qux";

        FieldUtils.writeField(baseParser, "parseData", false, true);
        FieldUtils.writeField(baseParser, "persist", true, true);

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(stepInfo2.getStepName()).thenReturn(peach);
        when(jobInfo2.getLabel()).thenReturn(fig);
        baseParser.store();

        verify(document, never()).getLocatorId();
        verify(dataDao, never()).save(fingerprint, dataFingerprint, data);
    }

    @Test
    public void testProcessIfParseDataTry() throws Exception {
        Counter dataParseCounter = Mockito.mock(Counter.class);
        Counter dataReuseCounter = Mockito.mock(Counter.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String grape = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String orange = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String dataDefName = "Baz";
        Long banana = Long.valueOf(1L);
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String cherry = "Qux";
        ZonedDateTime zonedDateTime = Mockito.mock(ZonedDateTime.class);
        Data data1 = Mockito.mock(Data.class);

        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String dataDefNameApricot = "Quux";
        Item item = Mockito.mock(Item.class);
        List<Item> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        boolean peach = true;
        Item newItem = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Corge";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String lychee = "Grault";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bionic = "Garply";
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        String bolt = "Waldo";
        String airbender = "Fred";

        List<DataComponent> newItems = new ArrayList<>();
        newItems.add(newItem);

        list.add(item);

        when(metricsHelper.getCounter(baseParser, "data", "parse"))
                .thenReturn(dataParseCounter);
        when(metricsHelper.getCounter(baseParser, "data", "reuse"))
                .thenReturn(dataReuseCounter);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(grape);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5)
                .thenReturn(jobInfo6);
        when(jobInfo.getLabel()).thenReturn(orange);
        when(jobInfo2.getDataDef()).thenReturn(dataDefName);
        when(document.getId()).thenReturn(banana);
        when(jobInfo3.getLabel()).thenReturn(cherry);
        when(configs.getRunDateTime()).thenReturn(zonedDateTime);
        when(dataFactory.createData(dataDefName, banana, cherry, zonedDateTime))
                .thenReturn(data1);
        when(jobInfo4.getDataDef()).thenReturn(dataDefNameApricot);
        when(data1.getItems()).thenReturn(list);
        when(item.getItemNames()).thenReturn(list2);
        when(indexerFactory.createIndexer(dataDefNameApricot, list2))
                .thenReturn(indexer);
        when(indexer.hasNext()).thenReturn(peach).thenReturn(false);
        when(item.copy()).thenReturn(newItem);
        when(indexer.next()).thenReturn(indexMap);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo5.getLabel()).thenReturn(lychee);
        when(stepInfo3.getStepName()).thenReturn(bionic);
        when(jobInfo6.getLabel()).thenReturn(bolt);
        when(timer.toString()).thenReturn(airbender);
        baseParser.process();

        verify(dataHelper).addPageTag(data1);
        verify(dataHelper).addItemTag(data1);
        verify(dataHelper).addAxisTags(data1, null);
        verify(valueProcessor).addScriptObject("document", document);
        verify(valueProcessor).addScriptObject("configs", configs);
        verify(newItem).setParent(data1);
        verify(valueProcessor).setAxisValues(dataDefNameApricot, newItem,
                indexMap, indexer, valueParser);
        verify(data1).setItems(newItems);
        verify(dataParseCounter).inc();
        verify(dataReuseCounter, never()).inc();
        verify(timer).stop();
    }

    @Test
    public void testProcessIfParseDataTryCatchIllegalAccessException()
            throws Exception {
        Counter dataParseCounter = Mockito.mock(Counter.class);
        Counter dataReuseCounter = Mockito.mock(Counter.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String grape = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String orange = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String dataDefName = "Baz";
        Long banana = Long.valueOf(1L);
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String cherry = "Qux";
        ZonedDateTime zonedDateTime = Mockito.mock(ZonedDateTime.class);
        Data data1 = Mockito.mock(Data.class);

        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String dataDefNameApricot = "Quux";
        Item item = Mockito.mock(Item.class);
        List<Item> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        boolean peach = true;
        Item newItem = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Corge";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String lychee = "Grault";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bionic = "Garply";
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        String bolt = "Waldo";
        String airbender = "Fred";
        list.add(item);

        List<DataComponent> newItems = new ArrayList<>();
        newItems.add(newItem);

        when(metricsHelper.getCounter(baseParser, "data", "parse"))
                .thenReturn(dataParseCounter);
        when(metricsHelper.getCounter(baseParser, "data", "reuse"))
                .thenReturn(dataReuseCounter);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(grape);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5)
                .thenReturn(jobInfo6);
        when(jobInfo.getLabel()).thenReturn(orange);
        when(jobInfo2.getDataDef()).thenReturn(dataDefName);
        when(document.getId()).thenReturn(banana);
        when(jobInfo3.getLabel()).thenReturn(cherry);
        when(configs.getRunDateTime()).thenReturn(zonedDateTime);
        when(dataFactory.createData(dataDefName, banana, cherry, zonedDateTime))
                .thenThrow(DataDefNotFoundException.class);
        when(jobInfo4.getDataDef()).thenReturn(dataDefNameApricot);
        when(data1.getItems()).thenReturn(list);
        when(item.getItemNames()).thenReturn(list2);
        when(indexerFactory.createIndexer(dataDefNameApricot, list2))
                .thenReturn(indexer);
        when(indexer.hasNext()).thenReturn(peach).thenReturn(false);
        when(item.copy()).thenReturn(newItem);
        when(indexer.next()).thenReturn(indexMap);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo5.getLabel()).thenReturn(lychee);
        when(stepInfo3.getStepName()).thenReturn(bionic);
        when(jobInfo6.getLabel()).thenReturn(bolt);
        when(timer.toString()).thenReturn(airbender);

        assertThrows(StepRunException.class, () -> baseParser.process());

        verify(dataHelper, never()).addPageTag(data1);
        verify(dataHelper, never()).addItemTag(data1);
        verify(dataHelper, never()).addAxisTags(data1, null);
        verify(valueProcessor, never()).addScriptObject("document", document);
        verify(valueProcessor, never()).addScriptObject("configs", configs);
        verify(newItem, never()).setParent(data1);
        verify(valueProcessor, never()).setAxisValues(dataDefNameApricot,
                newItem, indexMap, indexer, valueParser);
        verify(data1, never()).setItems(newItems);
        verify(dataParseCounter, never()).inc();
        verify(dataReuseCounter, never()).inc();
        verify(timer, never()).stop();
    }

    @Test
    public void testProcessElseParseData() throws Exception {
        Counter dataParseCounter = Mockito.mock(Counter.class);
        Counter dataReuseCounter = Mockito.mock(Counter.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String grape = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String orange = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String dataDefName = "Baz";
        Long banana = Long.valueOf(1L);
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String cherry = "Qux";
        ZonedDateTime zonedDateTime = Mockito.mock(ZonedDateTime.class);
        Data data1 = Mockito.mock(Data.class);
        List<DataComponent> newItems = new ArrayList<>();
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String dataDefNameApricot = "Quux";
        Item item = Mockito.mock(Item.class);
        List<Item> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        Indexer indexer = Mockito.mock(Indexer.class);
        boolean peach = true;
        Item newItem = Mockito.mock(Item.class);
        Map<String, Integer> indexMap = new HashMap<>();
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Corge";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String lychee = "Grault";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bionic = "Garply";
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        String bolt = "Waldo";
        String airbender = "Fred";
        list.add(item);

        FieldUtils.writeField(baseParser, "parseData", false, true);

        when(metricsHelper.getCounter(baseParser, "data", "parse"))
                .thenReturn(dataParseCounter);
        when(metricsHelper.getCounter(baseParser, "data", "reuse"))
                .thenReturn(dataReuseCounter);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3);
        when(stepInfo.getStepName()).thenReturn(grape);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5)
                .thenReturn(jobInfo6);
        when(jobInfo.getLabel()).thenReturn(orange);
        when(jobInfo4.getDataDef()).thenReturn(dataDefNameApricot);
        when(data1.getItems()).thenReturn(list);
        when(item.getItemNames()).thenReturn(list2);
        when(indexerFactory.createIndexer(dataDefNameApricot, list2))
                .thenReturn(indexer);
        when(indexer.hasNext()).thenReturn(peach);
        when(item.copy()).thenReturn(newItem);
        when(indexer.next()).thenReturn(indexMap);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo5.getLabel()).thenReturn(lychee);
        when(stepInfo3.getStepName()).thenReturn(bionic);
        when(jobInfo6.getLabel()).thenReturn(bolt);
        when(timer.toString()).thenReturn(airbender);

        baseParser.process();

        verify(jobInfo2, never()).getDataDef();
        verify(document, never()).getId();
        verify(jobInfo3, never()).getLabel();
        verify(configs, never()).getRunDateTime();
        verify(dataFactory, never()).createData(dataDefName, banana, cherry,
                zonedDateTime);
        verify(dataHelper, never()).addPageTag(data1);
        verify(dataHelper, never()).addItemTag(data1);
        verify(dataHelper, never()).addAxisTags(data1, null);
        verify(valueProcessor, never()).addScriptObject("document", document);
        verify(valueProcessor, never()).addScriptObject("configs", configs);
        verify(newItem, never()).setParent(data1);
        verify(valueProcessor, never()).setAxisValues(dataDefNameApricot,
                newItem, indexMap, indexer, valueParser);
        verify(data1, never()).setItems(newItems);
        verify(dataParseCounter, never()).inc();
        verify(dataReuseCounter).inc();
        verify(timer).stop();
    }

    @Test
    public void testSetValueParser() throws Exception {
        IValueParser valueParser1 = Mockito.mock(IValueParser.class);

        baseParser.setValueParser(valueParser1);

        assertSame(valueParser1,
                FieldUtils.readField(baseParser, "valueParser", true));
    }
}
