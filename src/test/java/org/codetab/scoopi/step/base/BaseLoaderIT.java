package org.codetab.scoopi.step.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.dao.jdo.JdoDaoUtilFactory;
import org.codetab.scoopi.defs.yml.DefsProvider;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.step.Task;
import org.codetab.scoopi.step.extract.URLLoader;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.util.CompressionUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class BaseLoaderIT {

    private static DInjector di;
    private static IDaoUtil daoUtil;
    private static HashSet<String> schemaClasses;
    private static DefsProvider defsProvider;
    private static String url;
    private static ObjectFactory objectFactory;
    private static ConfigService configService;
    private static IStore store;
    private static String clzName;

    private BaseLoader loader;
    private Task task;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, IllegalAccessException, URISyntaxException {
        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.scoopi.model.Locator");
        schemaClasses.add("org.codetab.scoopi.model.Document");

        di = new DInjector();

        configService = di.instance(ConfigService.class);
        configService.init("scoopi.properties", "scoopi-default.xml");
        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.defs.dir",
                "/testdefs/test-1");

        daoUtil = new JdoDaoUtilFactory(di).getUtilDao();

        defsProvider = di.instance(DefsProvider.class);
        // IT tests against project artifact jar and fails if dir is specified
        // hence set defs files instead of dir
        // defsProvider.setDefsFiles(getTestDefsFiles());
        defsProvider.init();
        defsProvider.initProviders();

        store = di.instance(IStore.class);

        objectFactory = di.instance(ObjectFactory.class);
        url = "/testdefs/page/acme-quote.html";
        clzName = "org.codetab.scoopi.step.lite.BlankStep";
    }

    @Before
    public void setUp() throws Exception {
        loader = di.instance(URLLoader.class);
        task = di.instance(Task.class);
        task.setStep(loader);

        clearStore();

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
        Locator locator = objectFactory.createLocator("acme", "quote", url);
        locator.getDocuments(); // init array

        StepInfo stepInfo = objectFactory.createStepInfo("loader", "seeder",
                "parser", clzName);

        Payload payload = getTestPayload(stepInfo, locator);

        loader.setPayload(payload);

        task.run(); // run loader task

        // test persistence
        List<Locator> actuals = daoUtil.getObjects(Locator.class,
                Lists.newArrayList("detachDocuments", "detachDocumentObject"));

        assertThat(actuals.size()).isEqualTo(1);

        // persisted locator
        Locator actual = actuals.get(0);

        // document url is persisted and locator url is not persisted
        actual.setUrl(url);

        assertThat(actual).isEqualTo(locator);

        // persisted document
        assertThat(actual.getDocuments().size()).isEqualTo(1);

        Document actualDocument = actual.getDocuments().get(0);
        Document expectedDocument = getTestDocument();
        expectedDocument.setId(actualDocument.getId());
        assertThat(actualDocument).isEqualTo(expectedDocument);

        // test handed over payload
        Payload actualPayload = store.takePayload();

        StepInfo nextStepInfo = objectFactory.createStepInfo("parser", "loader",
                "end", clzName);
        Payload expectedPayload = getTestPayload(nextStepInfo, actualDocument);

        assertThat(actualPayload).isEqualTo(expectedPayload);
    }

    /**
     * when no activeDoc is found then new document is created
     *
     */
    @Test
    public void testNoActiveDocument()
            throws IllegalAccessException, IOException, InterruptedException {
        Locator locator = objectFactory.createLocator("acme", "quote", url);

        Date fromDate = DateUtils.addDays(configService.getRunDateTime(), -1);
        Date toDate = DateUtils.addSeconds(configService.getRunDateTime(), -1);
        Document document =
                objectFactory.createDocument("acme", clzName, fromDate, toDate);
        document.setDocumentObject("doc");
        locator.getDocuments().add(document);

        StepInfo stepInfo = objectFactory.createStepInfo("loader", "seeder",
                "parser", clzName);

        Payload inPayload = getTestPayload(stepInfo, locator);

        loader.setPayload(inPayload);

        task.run(); // run loader task

        // test persistence
        List<Locator> actuals = daoUtil.getObjects(Locator.class,
                Lists.newArrayList("detachDocuments", "detachDocumentObject"));

        assertThat(actuals.size()).isEqualTo(1);

        // persisted locator
        Locator actual = actuals.get(0);

        // document url is persisted and locator url is not persisted
        actual.setUrl(url);

        assertThat(actual).isEqualTo(locator);

        // persisted document
        assertThat(actual.getDocuments().size()).isEqualTo(2);

        // check existing document
        assertThat(actual.getDocuments().get(0)).isEqualTo(document);

        // check new document
        Document actualDocument = actual.getDocuments().get(1);
        Document expectedDocument = getTestDocument();
        expectedDocument.setId(actualDocument.getId());

        assertThat(actualDocument).isEqualTo(expectedDocument);

        // test handed over payload
        Payload outPayload = store.takePayload();

        StepInfo nextStepInfo = objectFactory.createStepInfo("parser", "loader",
                "end", clzName);
        Payload expectedPayload = getTestPayload(nextStepInfo, actualDocument);

        assertThat(outPayload).isEqualTo(expectedPayload);
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

        Locator locator = objectFactory.createLocator("acme", "quote", url);
        locator.getDocuments();

        StepInfo stepInfo = objectFactory.createStepInfo("loader", "seeder",
                "parser", clzName);
        JobInfo jobInfo = objectFactory.createJobInfo(0, "acme", "quote",
                "task2", "task2"); // live 1 day
        Payload inPayload =
                objectFactory.createPayload(jobInfo, stepInfo, locator);
        loader.setPayload(inPayload);

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
        assertThat(newDocument.getUrl()).isEqualTo(url);
        assertThat(newDocument.getFromDate())
                .isEqualTo(configService.getRunDateTime());
        assertThat(newDocument.getToDate())
                .isEqualTo(DateUtils.addDays(newDocument.getFromDate(), 1));
        assertThat(newDocument.getDocumentObject())
                .isEqualTo(getTestDocument().getDocumentObject());

        // test handed over payload
        Payload actualPayload = store.takePayload();

        StepInfo nextStepInfo = objectFactory.createStepInfo("parser", "loader",
                "end", clzName);
        Payload expectedPayload =
                objectFactory.createPayload(jobInfo, nextStepInfo, newDocument);

        assertThat(actualPayload).isEqualTo(expectedPayload);
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

        Locator locator = objectFactory.createLocator("acme", "quote", url);
        locator.getDocuments();

        StepInfo stepInfo = objectFactory.createStepInfo("loader", "seeder",
                "parser", clzName);
        JobInfo jobInfo = objectFactory.createJobInfo(0, "acme", "quote",
                "task3", "task3"); // live 3 day
        Payload inPayload =
                objectFactory.createPayload(jobInfo, stepInfo, locator);
        loader.setPayload(inPayload);

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
                DateUtils.addDays(expectedDocument.getFromDate(), 3));
        assertThat(existingDocument).isEqualTo(expectedDocument);

        // test handed over payload
        Payload actualPayload = store.takePayload();

        StepInfo nextStepInfo = objectFactory.createStepInfo("parser", "loader",
                "end", clzName);
        Payload expectedPayload = objectFactory.createPayload(jobInfo,
                nextStepInfo, existingDocument);

        assertThat(actualPayload.getData())
                .isEqualTo(expectedPayload.getData());
    }

    private Document getTestDocument() throws IOException {
        Document document = objectFactory.createDocument("acme", url,
                configService.getRunDateTime(), configService.getRunDateTime());
        URL fileURL = BaseLoaderIT.class.getResource(url);
        byte[] bytes = IOUtils.toByteArray(fileURL);
        final int bufferLength = 4086;
        byte[] compressedObject =
                CompressionUtil.compressByteArray(bytes, bufferLength);
        document.setDocumentObject(compressedObject);
        return document;
    }

    private Payload getTestPayload(final StepInfo stepInfo, final Object data) {
        JobInfo jobInfo = objectFactory.createJobInfo(0, "acme", "quote",
                "task1", "task1");
        return objectFactory.createPayload(jobInfo, stepInfo, data);
    }

    private Document insertActiveDocument(final int fromDateOffset,
            final int toDateOffset) throws IllegalAccessException {
        Locator locator = objectFactory.createLocator("acme", "quote", url);

        Date fromDate = DateUtils.addDays(new Date(), fromDateOffset);
        Date toDate = DateUtils.addDays(new Date(), toDateOffset);
        Document document =
                objectFactory.createDocument("acme", clzName, fromDate, toDate);
        document.setDocumentObject("doc");
        locator.getDocuments().add(document);

        StepInfo stepInfo = objectFactory.createStepInfo("loader", "seeder",
                "parser", clzName);

        Payload inPayload = getTestPayload(stepInfo, locator);

        URLLoader loaderStep = di.instance(URLLoader.class);
        loaderStep.setPayload(inPayload);
        loaderStep.initialize();
        FieldUtils.writeField(loaderStep, "document", document, true);
        loaderStep.store();

        return document;
    }

    private void clearStore() throws InterruptedException {
        int count = store.getPayloadsCount();
        for (int i = 0; i < count; i++) {
            store.takePayload();
        }
    }
}
