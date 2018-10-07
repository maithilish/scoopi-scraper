package org.codetab.scoopi.step.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.dao.jdo.JdoDaoUtilFactory;
import org.codetab.scoopi.defs.yml.Defs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.Task;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.step.extract.URLLoader;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.util.CompressionUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class BaseLoaderIT {

    protected static DInjector di;
    protected static IDaoUtil daoUtil;
    protected static HashSet<String> schemaClasses;
    protected static Defs defs;
    protected static ObjectFactory factory;
    protected static ConfigService configService;

    private BaseLoader loader;
    private Task task;
    private IStore store;
    private TaskMediator taskMediator;
    private String pageUrl;
    private String clzName;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    // don't move this to base class, tests fail in cli
    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, IllegalAccessException, URISyntaxException {

        di = new DInjector();

        String defDir = "/testdefs/test-price";

        configService = di.instance(ConfigService.class);
        configService.init("scoopi.properties", "scoopi-default.xml");
        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.defs.dir", defDir);

        defs = di.instance(Defs.class);
        defs.init();
        defs.initDefProviders();

        schemaClasses = new HashSet<>();

        daoUtil = new JdoDaoUtilFactory(di).getUtilDao();

        factory = di.instance(ObjectFactory.class);
    }

    @Before
    public void setUp() throws Exception {
        taskMediator = di.instance(TaskMediator.class);
        store = di.instance(IStore.class);
        loader = di.instance(URLLoader.class);
        task = di.instance(Task.class);
        task.setStep(loader);

        pageUrl = "/testdefs/page/acme-quote.html";
        clzName = "org.codetab.scoopi.step.lite.BlankStep";

        schemaClasses.add("org.codetab.scoopi.model.Locator");
        schemaClasses.add("org.codetab.scoopi.model.Document");
        daoUtil.clearCache();
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    /**
     * when no document is found then new document is created
     * @throws IOException
     */
    @Test
    public void testNoDocument()
            throws IllegalAccessException, IOException, InterruptedException {
        Locator locator = factory.createLocator("acme", "quote", pageUrl);
        locator.getDocuments(); // init array

        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);

        Payload payload = getTestPayload(stepInfo, locator);

        loader.setPayload(payload);

        store.clear();
        resetJobId();

        task.run(); // run loader task

        // test persistence
        List<Locator> actuals = daoUtil.getObjects(Locator.class,
                Lists.newArrayList("detachDocuments", "detachDocumentObject"));

        assertThat(actuals.size()).isEqualTo(1);

        // persisted locator
        Locator actual = actuals.get(0);

        // document url is persisted and locator url is not persisted
        actual.setUrl(pageUrl);

        assertThat(actual).isEqualTo(locator);

        // persisted document
        assertThat(actual.getDocuments().size()).isEqualTo(1);

        Document actualDocument = actual.getDocuments().get(0);
        Document expectedDocument = getTestDocument();
        expectedDocument.setId(actualDocument.getId());
        expectedDocument.setToDate(
                DateUtils.addDays(expectedDocument.getFromDate(), 1));

        assertThat(actualDocument).isEqualTo(expectedDocument);

        // test handed over payload
        List<Payload> expectedPayloads = getTestPayloads(expectedDocument);

        assertThat(store.getPayloadsCount()).isEqualTo(3);
        Payload actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(0));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(1));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(2));
    }

    /**
     * when no activeDoc is found then new document is created
     *
     */
    @Test
    public void testNoActiveDocument()
            throws IllegalAccessException, IOException, InterruptedException {
        Locator locator = factory.createLocator("acme", "quote", pageUrl);

        Date fromDate = DateUtils.addDays(configService.getRunDateTime(), -1);
        Date toDate = DateUtils.addSeconds(configService.getRunDateTime(), -1);
        Document document =
                factory.createDocument("acme", clzName, fromDate, toDate);
        document.setDocumentObject("doc");
        locator.getDocuments().add(document);

        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);

        Payload inPayload = getTestPayload(stepInfo, locator);

        loader.setPayload(inPayload);

        store.clear();
        resetJobId();

        task.run(); // run loader task

        // test persistence
        List<Locator> actuals = daoUtil.getObjects(Locator.class,
                Lists.newArrayList("detachDocuments", "detachDocumentObject"));

        assertThat(actuals.size()).isEqualTo(1);

        // persisted locator
        Locator actual = actuals.get(0);

        // document url is persisted and locator url is not persisted
        actual.setUrl(pageUrl);

        assertThat(actual).isEqualTo(locator);

        // persisted document
        assertThat(actual.getDocuments().size()).isEqualTo(2);

        // check existing document
        assertThat(actual.getDocuments().get(0)).isEqualTo(document);

        // check new document
        Document actualDocument = actual.getDocuments().get(1);
        Document expectedDocument = getTestDocument();
        expectedDocument.setId(actualDocument.getId());
        expectedDocument.setToDate(
                DateUtils.addDays(expectedDocument.getFromDate(), 1));

        assertThat(actualDocument).isEqualTo(expectedDocument);

        // test handed over payload
        List<Payload> expectedPayloads = getTestPayloads(expectedDocument);

        assertThat(store.getPayloadsCount()).isEqualTo(3);
        Payload actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(0));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(1));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(2));
    }

    /**
     * when activeDoc toDate (after adjusting to new live value) is less than
     * rundate, activeDoc toDate is truncated and new document is created
     *
     * @throws IOException
     */
    @Test
    public void testHasActiveDocumentButLiveIsLessThanRunTime()
            throws IllegalAccessException, IOException, InterruptedException {

        Document expectedDocument = insertActiveDocument(-2, 1);

        Locator locator = factory.createLocator("acme", "quote", pageUrl);
        locator.getDocuments();

        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);
        JobInfo jobInfo = factory.createJobInfo(0, "acme", "quote", "task2",
                "steps2", "task2"); // live
        // 1 day
        Payload inPayload = factory.createPayload(jobInfo, stepInfo, locator);
        loader.setPayload(inPayload);

        store.clear();
        resetJobId();

        task.run(); // run loader task

        // test persistence
        List<Locator> actuals = daoUtil.getObjects(Locator.class,
                Lists.newArrayList("detachDocuments", "detachDocumentObject"));

        assertThat(actuals.size()).isEqualTo(1);

        // persisted locator
        Locator actual = actuals.get(0);

        // persisted document
        assertThat(actual.getDocuments().size()).isEqualTo(2);

        Document existingDocument = actual.getDocuments().get(0);
        expectedDocument.setId(existingDocument.getId());
        expectedDocument.setToDate(
                DateUtils.addSeconds(configService.getRunDateTime(), -1));
        assertThat(existingDocument).isEqualTo(expectedDocument);

        Document newDocument = actual.getDocuments().get(1);
        assertThat(newDocument.getId()).isNotNull();
        assertThat(newDocument.getId()).isNotSameAs(existingDocument.getId());
        assertThat(newDocument.getName()).isEqualTo("acme");
        assertThat(newDocument.getUrl()).isEqualTo(pageUrl);
        assertThat(newDocument.getFromDate())
                .isEqualTo(configService.getRunDateTime());
        assertThat(newDocument.getToDate())
                .isEqualTo(DateUtils.addDays(newDocument.getFromDate(), 1));
        assertThat(newDocument.getDocumentObject())
                .isEqualTo(getTestDocument().getDocumentObject());

        // test handed over payload
        List<Payload> expectedPayloads = getTestPayloads(newDocument);

        assertThat(store.getPayloadsCount()).isEqualTo(3);
        Payload actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(0));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(1));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(2));
    }

    /**
     * when activeDoc is found then its documentObject has to be loaded from
     * datastore
     * @throws IOException
     */
    @Test
    public void testHasActiveDocumentButLiveIsGreaterThanRunTime()
            throws IllegalAccessException, IOException, InterruptedException {
        Document expectedDocument = insertActiveDocument(-1, 1);

        Locator locator = factory.createLocator("acme", "quote", pageUrl);
        locator.getDocuments();

        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);
        JobInfo jobInfo = factory.createJobInfo(0, "acme", "quote", "task3",
                "steps3", "task3"); // live
        // 3 day
        Payload inPayload = factory.createPayload(jobInfo, stepInfo, locator);
        loader.setPayload(inPayload);

        store.clear();
        resetJobId();

        task.run(); // run loader task

        // test persistence
        List<Locator> actuals = daoUtil.getObjects(Locator.class,
                Lists.newArrayList("detachDocuments", "detachDocumentObject"));

        assertThat(actuals.size()).isEqualTo(1);

        // persisted locator
        Locator actual = actuals.get(0);

        // persisted document
        assertThat(actual.getDocuments().size()).isEqualTo(1);

        Document existingDocument = actual.getDocuments().get(0);
        expectedDocument.setId(existingDocument.getId());
        expectedDocument.setToDate(
                DateUtils.addDays(expectedDocument.getFromDate(), 1));
        assertThat(existingDocument).isEqualTo(expectedDocument);

        // test handed over payload
        List<Payload> expectedPayloads = getTestPayloads(expectedDocument);

        assertThat(store.getPayloadsCount()).isEqualTo(3);
        Payload actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(0));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(1));
        actualPayload = store.takePayload();
        assertThat(actualPayload).isEqualTo(expectedPayloads.get(2));
    }

    private Document getTestDocument() throws IOException {
        Document document = factory.createDocument("acme", pageUrl,
                configService.getRunDateTime(), configService.getRunDateTime());
        URL fileURL = BaseLoaderIT.class.getResource(pageUrl);
        byte[] bytes = IOUtils.toByteArray(fileURL);
        final int bufferLength = 4086;
        byte[] compressedObject =
                CompressionUtil.compressByteArray(bytes, bufferLength);
        document.setDocumentObject(compressedObject);
        return document;
    }

    private Payload getTestPayload(final StepInfo stepInfo, final Object data) {
        JobInfo jobInfo = factory.createJobInfo(0, "acme", "quote", "task1",
                "steps1", "task1");
        return factory.createPayload(jobInfo, stepInfo, data);
    }

    private List<Payload> getTestPayloads(final Object payloadData) {
        List<Payload> payloads = new ArrayList<>();
        StepInfo stepInfo =
                factory.createStepInfo("parser", "loader", "end", clzName);

        JobInfo jobInfo = factory.createJobInfo(1, "acme", "quote", "task1",
                "devSteps", "task1");
        Payload payload = factory.createPayload(jobInfo, stepInfo, payloadData);
        payloads.add(payload);

        jobInfo = factory.createJobInfo(2, "acme", "quote", "task2", "devSteps",
                "task2");
        payload = factory.createPayload(jobInfo, stepInfo, payloadData);
        payloads.add(payload);

        jobInfo = factory.createJobInfo(3, "acme", "quote", "task3", "devSteps",
                "task3");
        payload = factory.createPayload(jobInfo, stepInfo, payloadData);
        payloads.add(payload);

        return payloads;
    }

    private Document insertActiveDocument(final int fromDateOffset,
            final int toDateOffset) throws IllegalAccessException {
        Locator locator = factory.createLocator("acme", "quote", pageUrl);

        Date fromDate = DateUtils.addDays(new Date(), fromDateOffset);
        Date toDate = DateUtils.addDays(new Date(), toDateOffset);
        Document document =
                factory.createDocument("acme", clzName, fromDate, toDate);
        document.setDocumentObject("doc");
        locator.getDocuments().add(document);

        StepInfo stepInfo =
                factory.createStepInfo("loader", "seeder", "parser", clzName);

        Payload inPayload = getTestPayload(stepInfo, locator);

        URLLoader loaderStep = di.instance(URLLoader.class);
        loaderStep.setPayload(inPayload);
        loaderStep.initialize();
        FieldUtils.writeField(loaderStep, "document", document, true);
        loaderStep.store();

        return document;
    }

    private void resetJobId() throws IllegalAccessException {
        AtomicInteger counter = (AtomicInteger) FieldUtils
                .readField(taskMediator, "jobIdCounter", true);
        counter.set(0);
    }

}
