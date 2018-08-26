package org.codetab.scoopi.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.defs.ITaskDefs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.URLConnectionHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.persistence.DocumentPersistence;
import org.codetab.scoopi.persistence.LocatorPersistence;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.shared.StatService;
import org.codetab.scoopi.shared.StepService;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.step.extract.URLLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;

/**
 * <p>
 * BaseLoader tests.
 * @author Maithilish
 *
 */
public class BaseLoaderTest {

    @Mock
    private ConfigService configService;
    @Mock
    private StepService stepService;
    @Mock
    private StatService activityService;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDefs taskDefs;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private LocatorPersistence locatorPersistence;
    @Mock
    private DocumentPersistence documentPersistence;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private URLConnectionHelper ucHelper;

    @InjectMocks
    private URLLoader loader;

    private String url;
    private String clzName;
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
        url = "/testdefs/page/acme-quote.html";
        clzName = "org.codetab.scoopi.step.lite.BlankStep";
    }

    @Test
    public void testInitialize() throws IllegalAccessException {
        Locator locator = factory.createLocator("acme", "quote", url);
        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);
        Payload payload = getTestPayload(stepInfo, locator);
        loader.setPayload(payload);

        boolean actual = loader.initialize();
        assertThat(actual).isTrue();

        assertThat(FieldUtils.readField(loader, "locator", true))
                .isEqualTo(locator);
    }

    @Test
    public void testInitializeInvalidState() throws IllegalAccessException {
        try {
            loader.initialize();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("payload must not be null");
        }

        Payload payload = getTestPayload(null, null);
        loader.setPayload(payload);
        try {
            loader.initialize();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("payload data must not be null");
        }
    }

    @Test
    public void testInitializeShouldThrowException()
            throws IllegalAccessException {
        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);
        Payload payload =
                getTestPayload(stepInfo, "data not instance of locator");
        loader.setPayload(payload);

        testRule.expect(StepRunException.class);
        loader.initialize();
    }

    @Test
    public void testLoadPersistIsTrue() throws IllegalAccessException {
        initializeLoader();
        Locator savedLocator = factory.createLocator("acme", "quote", null);
        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        given(locatorPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true);
        given(locatorPersistence.loadLocator("acme", "quote"))
                .willReturn(savedLocator);

        boolean actual = loader.load();
        assertThat(actual).isTrue();
        assertThat(FieldUtils.readField(loader, "locator", true))
                .isSameAs(savedLocator);
        assertThat(savedLocator.getUrl()).isEqualTo(url);
    }

    @Test
    public void testLoadPersistIsFalse() throws IllegalAccessException {
        Locator locator = initializeLoader();
        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        given(locatorPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(false);

        boolean actual = loader.load();
        assertThat(actual).isTrue();
        assertThat(FieldUtils.readField(loader, "locator", true))
                .isSameAs(locator);
    }

    @Test
    public void testLoadInvalidState() {
        try {
            loader.load();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("step input locator must not be null");
        }
    }

    /**
     * when no activeDoc is found then new document is created
     * @throws DefNotFoundException
     * @throws IOException
     */
    @Test
    public void testProcessNoActiveDocument()
            throws DefNotFoundException, IOException {
        Locator locator = initializeLoader();
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        String live = "P1D";
        Document document =
                factory.createDocument("acme", url, fromDate, toDate);
        Counter counter = Mockito.mock(Counter.class);

        given(taskDefs.getFieldValue("quote", "task1", "live"))
                .willReturn(live);
        given(ucHelper.getProtocol(url)).willReturn("resource");
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, live, loader.getJobInfo()))
                .willReturn(toDate);
        given(documentHelper.createDocument(locator.getName(), locator.getUrl(),
                fromDate, toDate)).willReturn(document);
        given(metricsHelper.getCounter(loader, "fetch", "resource"))
                .willReturn(counter);

        assertThat(loader.isConsistent()).isFalse();

        boolean actual = loader.process();

        byte[] documentObject = getTestDocumentObject();

        assertThat(actual).isTrue();
        assertThat(loader.isConsistent()).isTrue();
        assertThat(locator.getDocuments().size()).isEqualTo(1);
        assertThat(locator.getDocuments()).contains(document);

        verify(counter).inc();
        verify(documentHelper).setDocumentObject(eq(document),
                eq(documentObject));
    }

    @Test
    public void testProcessNoActiveDocumentShouldThrowException()
            throws DefNotFoundException, IOException {
        Locator locator = initializeLoader();
        String invalidUrl = "x";
        locator.setUrl(invalidUrl);

        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        String live = "P1D";

        Document document =
                factory.createDocument("acme", url, fromDate, toDate);
        Counter counter = Mockito.mock(Counter.class);

        given(taskDefs.getFieldValue("quote", "task1", "live"))
                .willReturn(live);
        given(ucHelper.getProtocol(invalidUrl)).willReturn("file");

        try {
            loader.process();
            fail("should throw StepRunException");
        } catch (StepRunException e) {
        }

        locator.setUrl(url);
        given(ucHelper.getProtocol(url)).willReturn("resource");
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, live, loader.getJobInfo()))
                .willReturn(toDate);
        given(documentHelper.createDocument(locator.getName(), locator.getUrl(),
                fromDate, toDate)).willReturn(document);
        given(documentHelper.setDocumentObject(eq(document), any(byte[].class)))
                .willThrow(IOException.class);
        given(metricsHelper.getCounter(loader, "fetch", "resource"))
                .willReturn(counter);
        testRule.expect(StepRunException.class);
        loader.process();
    }

    /**
     * when activeDoc is found then its documentObject has to be loaded from
     * datastore
     * @throws DefNotFoundException
     * @throws IOException
     */
    @Test
    public void testProcessHasActiveDocument()
            throws DefNotFoundException, IOException {
        Locator locator = initializeLoader();
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        Date newToDate = DateUtils.addDays(runDate, 2);
        String live = "PT0S";
        Document document =
                factory.createDocument("acme", url, fromDate, toDate);
        document.setId(1L);
        Document loadedDocument =
                factory.createDocument("acmex", url, fromDate, toDate);
        loadedDocument.setId(2L);

        given(documentHelper.getActiveDocument(locator.getDocuments()))
                .willReturn(document);
        given(documentHelper.getToDate(document.getFromDate(), live,
                loader.getJobInfo())).willReturn(newToDate);
        given(documentHelper.resetToDate(document, newToDate))
                .willReturn(false);
        given(documentPersistence.loadDocument(document.getId()))
                .willReturn(loadedDocument);

        assertThat(loader.isConsistent()).isFalse();

        boolean actual = loader.process();

        assertThat(actual).isTrue();
        assertThat(loader.isConsistent()).isTrue();
        assertThat(loader.getOutput()).isEqualTo(loadedDocument);
    }

    /**
     * when activeDoc toDate (after adjusting to new live value) is less than
     * rundate, activeDoc toDate is truncated and new document is created
     *
     * @throws DefNotFoundException
     * @throws IOException
     */
    @Test
    public void testProcessResetToDate()
            throws DefNotFoundException, IOException {
        Locator locator = initializeLoader();
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        Date newToDate = DateUtils.addDays(runDate, 2);
        String live = "PT0S";
        Document document =
                factory.createDocument("acme", url, fromDate, toDate);
        Counter counter = Mockito.mock(Counter.class);

        given(taskDefs.getFieldValue("quote", "task1", "live"))
                .willThrow(DefNotFoundException.class);
        given(documentHelper.getActiveDocument(locator.getDocuments()))
                .willReturn(document);
        given(documentHelper.getToDate(document.getFromDate(), live,
                loader.getJobInfo())).willReturn(newToDate);
        given(documentHelper.resetToDate(document, newToDate)).willReturn(true);
        given(ucHelper.getProtocol(url)).willReturn("resource");
        given(configService.getRunDateTime()).willReturn(fromDate);
        given(documentHelper.getToDate(fromDate, live, loader.getJobInfo()))
                .willReturn(toDate);
        given(documentHelper.createDocument(locator.getName(), locator.getUrl(),
                fromDate, toDate)).willReturn(document);
        given(metricsHelper.getCounter(loader, "fetch", "resource"))
                .willReturn(counter);

        assertThat(loader.isConsistent()).isFalse();

        boolean actual = loader.process();

        byte[] documentObject = getTestDocumentObject();

        assertThat(actual).isTrue();
        assertThat(loader.isConsistent()).isTrue();
        assertThat(locator.getDocuments().size()).isEqualTo(1);
        assertThat(locator.getDocuments()).contains(document);

        verify(counter).inc();
        verify(documentHelper).setDocumentObject(eq(document),
                eq(documentObject));
    }

    @Test
    public void testStore() throws IllegalAccessException {
        Locator locator = initializeLoader();
        locator.setId(1L);
        Document document =
                factory.createDocument("acme", url, new Date(), new Date());
        document.setId(2L);
        FieldUtils.writeField(loader, "document", document, true);

        Locator loadedLocator = factory.createLocator("acme", "quote", null);
        Document loadedDocument =
                factory.createDocument("acme", url, new Date(), new Date());

        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        given(locatorPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true);
        given(locatorPersistence.storeLocator(locator)).willReturn(true);
        given(locatorPersistence.loadLocator(locator.getId()))
                .willReturn(loadedLocator);
        given(documentPersistence.loadDocument(document.getId()))
                .willReturn(loadedDocument);

        boolean actual = loader.store();

        Document actualDoc = (Document) loader.getOutput();
        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        assertThat(actual).isEqualTo(true);

        assertThat(actualDoc).isSameAs(loadedDocument);
        assertThat(actualLocator).isSameAs(loadedLocator);
    }

    @Test
    public void testStorePersistFalse() throws IllegalAccessException {
        // persist true and store false
        Locator locator = initializeLoader();
        locator.setId(1L);
        Document document =
                factory.createDocument("acme", url, new Date(), new Date());
        document.setId(2L);
        FieldUtils.writeField(loader, "document", document, true);

        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        given(locatorPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(false);
        given(locatorPersistence.storeLocator(locator)).willReturn(false);

        boolean actual = loader.store();

        Document actualDoc =
                (Document) FieldUtils.readField(loader, "document", true);
        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(actual).isEqualTo(true);
        assertThat(actualDoc).isSameAs(document);
        assertThat(actualLocator).isSameAs(locator);
        assertThat(loader.getOutput()).isNull();

        // persist false and store false
        locator = initializeLoader();
        locator.setId(1L);
        document = factory.createDocument("acme", url, new Date(), new Date());
        document.setId(2L);
        FieldUtils.writeField(loader, "document", document, true);

        given(locatorPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true);
        given(locatorPersistence.storeLocator(locator)).willReturn(false);

        actual = loader.store();

        actualDoc = (Document) FieldUtils.readField(loader, "document", true);
        actualLocator = (Locator) FieldUtils.readField(loader, "locator", true);

        assertThat(actual).isEqualTo(true);
        assertThat(actualDoc).isSameAs(document);
        assertThat(actualLocator).isSameAs(locator);
        assertThat(loader.getOutput()).isNull();
    }

    @Test
    public void testStoreReload() throws IllegalAccessException {
        Locator locator = initializeLoader();
        locator.setId(1L);
        Document document =
                factory.createDocument("acme", url, new Date(), new Date());
        document.setId(2L);
        FieldUtils.writeField(loader, "document", document, true);

        Locator loadedLocator = null;
        Document loadedDocument = null;

        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        given(locatorPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true);
        given(locatorPersistence.storeLocator(locator)).willReturn(true);
        given(locatorPersistence.loadLocator(locator.getId()))
                .willReturn(loadedLocator);
        given(documentPersistence.loadDocument(document.getId()))
                .willReturn(loadedDocument);

        boolean actual = loader.store();

        Document actualDoc =
                (Document) FieldUtils.readField(loader, "document", true);
        Locator actualLocator =
                (Locator) FieldUtils.readField(loader, "locator", true);
        assertThat(actual).isEqualTo(true);

        assertThat(actual).isEqualTo(true);
        assertThat(actualDoc).isSameAs(document);
        assertThat(actualLocator).isSameAs(locator);
        assertThat(loader.getOutput()).isNull();
    }

    @Test
    public void testStoreShouldThrowException() throws IllegalAccessException {
        Locator locator = initializeLoader();
        locator.setId(1L);
        Document document =
                factory.createDocument("acme", url, new Date(), new Date());
        document.setId(2L);
        FieldUtils.writeField(loader, "document", document, true);

        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        given(locatorPersistence.persist(taskLevelPersistenceDefined))
                .willReturn(true).willReturn(true);
        given(locatorPersistence.storeLocator(locator)).willReturn(true)
                .willThrow(StepPersistenceException.class);
        given(locatorPersistence.loadLocator(locator.getId()))
                .willThrow(StepPersistenceException.class);
        try {
            loader.store();
            fail("should throw StepRunException");
        } catch (StepRunException e) {

        }

        testRule.expect(StepRunException.class);
        loader.store();
    }

    @Test
    public void testIsDocumentLoaded() throws IllegalAccessException {
        assertThat(loader.isDocumentLoaded()).isFalse();

        Document document =
                factory.createDocument("acme", url, new Date(), new Date());
        FieldUtils.writeField(loader, "document", document, true);

        assertThat(loader.isDocumentLoaded()).isTrue();
    }

    private Locator initializeLoader() {
        Locator locator = factory.createLocator("acme", "quote", url);
        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);
        Payload payload = getTestPayload(stepInfo, locator);
        loader.setPayload(payload);
        loader.initialize();
        return locator;
    }

    private Payload getTestPayload(final StepInfo stepInfo, final Object data) {
        JobInfo jobInfo =
                factory.createJobInfo(0, "acme", "quote", "task1", "task1");
        return factory.createPayload(jobInfo, stepInfo, data);
    }

    private byte[] getTestDocumentObject() throws IOException {
        URL fileURL = BaseLoaderIT.class.getResource(url);
        return IOUtils.toByteArray(fileURL);
    }
}
