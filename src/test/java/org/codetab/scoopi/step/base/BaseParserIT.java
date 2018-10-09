package org.codetab.scoopi.step.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.dao.jdo.DataDao;
import org.codetab.scoopi.dao.jdo.JdoDaoUtilFactory;
import org.codetab.scoopi.defs.ITaskDefs;
import org.codetab.scoopi.defs.yml.DataDefDefs;
import org.codetab.scoopi.defs.yml.Defs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.Task;
import org.codetab.scoopi.step.parse.jsoup.Parser;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.util.CompressionUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Test load, store and handover of data when persistence is enabled or disabled
 * @author maithilish
 *
 */
public class BaseParserIT {

    private static DInjector di;
    private static IDaoUtil daoUtil;
    private static HashSet<String> schemaClasses;
    private static Defs defs;
    private static ObjectFactory factory;
    private static ConfigService configService;

    private BaseParser parser;
    private Task task;
    private String pageUrl;

    @Rule
    public ExpectedException testRule = ExpectedException.none();
    private IStore store;

    // don't move this to base class, tests fail in cli
    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, IllegalAccessException, URISyntaxException {
        di = new DInjector();

        String defDir = "/defs/examples/jsoup/ex-1";

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
        store = di.instance(IStore.class);
        store.clear();

        parser = di.instance(Parser.class);
        task = di.instance(Task.class);
        task.setStep(parser);

        pageUrl = "/defs/examples/page/acme-quote.html";

        schemaClasses.add("org.codetab.scoopi.model.Data");
        daoUtil.clearCache();
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @Test
    public void testNoExistingDataParseData()
            throws IllegalAccessException, IOException, InterruptedException {

        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.persist.data", "true");

        String taskGroup = "quoteGroup";
        String taskName = "priceTask";
        String dataDef = "price";

        Document document = getTestDocument();
        JobInfo jobInfo = factory.createJobInfo(0, "acme", taskGroup, taskName,
                "jsoupDefault", dataDef);
        StepInfo stepInfo = factory.createStepInfo("parser", "loader",
                "process", "clzName");
        Payload payload = factory.createPayload(jobInfo, stepInfo, document);

        parser.setPayload(payload);

        task.run(); // run task

        // test persistence
        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));
        assertThat(actuals.size()).isEqualTo(1);

        Data actualData = actuals.get(0);
        assertThat(actualData.getMembers().size()).isEqualTo(1);

        Data expectedData = getTestData();

        Set<Axis> aAxes = actualData.getMembers().get(0).getAxes();
        Set<Axis> eAxes = expectedData.getMembers().get(0).getAxes();
        assertThat(aAxes).containsAll(eAxes);

        // test handover
        Payload actualPayload = store.takePayload();
        assertThat(actualPayload).isNotNull();

        assertThat(actualPayload.getData()).isEqualTo(actualData);

        actualData = (Data) FieldUtils.readField(parser, "data", true);
        assertThat(actualPayload.getData()).isSameAs(actualData);
    }

    @Test
    public void testDataExistsReuseData() throws IllegalAccessException,
            IOException, InterruptedException, DataDefNotFoundException {

        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.persist.data", "true");

        String taskGroup = "quoteGroup";
        String taskName = "priceTask";

        DataDefDefs dataDefDefs = di.instance(DataDefDefs.class);
        DataDef dataDef = dataDefDefs.getDataDef("price");

        Document document = getTestDocument();
        document.setId(10L);

        Data existingData = insertTestData(dataDef.getId(), document.getId());

        JobInfo jobInfo = factory.createJobInfo(0, "acme", taskGroup, taskName,
                "jsoupDefault", dataDef.getName());
        StepInfo stepInfo = factory.createStepInfo("parser", "loader",
                "process", "clzName");
        Payload payload = factory.createPayload(jobInfo, stepInfo, document);

        parser.setPayload(payload);

        task.run(); // run task

        // test persistence
        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));
        assertThat(actuals.size()).isEqualTo(1);
        assertThat(actuals.get(0)).isEqualTo(existingData);

        Payload actualPayload = store.takePayload();
        assertThat(actualPayload).isNotNull();
        assertThat(actualPayload.getData()).isEqualTo(existingData);

        Data actualData = (Data) FieldUtils.readField(parser, "data", true);
        assertThat(actualPayload.getData()).isSameAs(actualData);
    }

    @Test
    public void testUseDataStoreFalse()
            throws IllegalAccessException, IOException, InterruptedException {

        configService.getConfigs().setProperty("scoopi.useDatastore", "false");
        configService.getConfigs().setProperty("scoopi.persist.data", "true");

        String taskGroup = "quoteGroup";
        String taskName = "priceTask";
        String dataDef = "price";

        Document document = getTestDocument();
        JobInfo jobInfo = factory.createJobInfo(0, "acme", taskGroup, taskName,
                "jsoupDefault", dataDef);
        StepInfo stepInfo = factory.createStepInfo("parser", "loader",
                "process", "clzName");
        Payload payload = factory.createPayload(jobInfo, stepInfo, document);
        parser.setPayload(payload);

        task.run(); // run task

        // test persistence
        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));
        assertThat(actuals.size()).isEqualTo(0);
    }

    @Test
    public void testDataPersistFalse()
            throws IllegalAccessException, IOException, InterruptedException {

        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.persist.data", "false");

        String taskGroup = "quoteGroup";
        String taskName = "priceTask";
        String dataDef = "price";

        Document document = getTestDocument();
        JobInfo jobInfo = factory.createJobInfo(0, "acme", taskGroup, taskName,
                "jsoupDefault", dataDef);
        StepInfo stepInfo = factory.createStepInfo("parser", "loader",
                "process", "clzName");
        Payload payload = factory.createPayload(jobInfo, stepInfo, document);
        parser.setPayload(payload);

        task.run(); // run task

        // test persistence
        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));
        assertThat(actuals.size()).isEqualTo(0);
    }

    @Test
    public void testTaskLevelPersistDataTrue() throws IllegalAccessException,
            IOException, InterruptedException, DefNotFoundException {

        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.persist.data", "true");

        String taskGroup = "quoteGroup";
        String taskName = "priceTask";
        String dataDef = "price";

        Document document = getTestDocument();
        JobInfo jobInfo = factory.createJobInfo(0, "acme", taskGroup, taskName,
                "jsoupDefault", dataDef);
        StepInfo stepInfo =
                factory.createStepInfo("parser", "loader", "end", "clzName");
        Payload payload = factory.createPayload(jobInfo, stepInfo, document);
        parser.setPayload(payload);

        ITaskDefs taskDefs = Mockito.mock(ITaskDefs.class);
        FieldUtils.writeField(parser, "taskDefs", taskDefs, true);

        given(taskDefs.getFieldValue(taskGroup, taskName, "persist", "data"))
                .willReturn("true");

        task.run(); // run task

        // test persistence
        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));
        assertThat(actuals.size()).isEqualTo(1);
    }

    @Test
    public void testTaskLevelPersistDataFalse() throws IllegalAccessException,
            IOException, InterruptedException, DefNotFoundException {

        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.persist.data", "true");

        String taskGroup = "quoteGroup";
        String taskName = "priceTask";
        String dataDef = "price";

        Document document = getTestDocument();
        JobInfo jobInfo = factory.createJobInfo(0, "acme", taskGroup, taskName,
                "jsoupDefault", dataDef);
        StepInfo stepInfo =
                factory.createStepInfo("parser", "loader", "end", "clzName");
        Payload payload = factory.createPayload(jobInfo, stepInfo, document);
        parser.setPayload(payload);

        ITaskDefs taskDefs = Mockito.mock(ITaskDefs.class);
        FieldUtils.writeField(parser, "taskDefs", taskDefs, true);

        given(taskDefs.getFieldValue(taskGroup, taskName, "persist", "data"))
                .willReturn("false");

        task.run(); // run task

        // test persistence
        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));
        assertThat(actuals.size()).isEqualTo(0);
    }

    private Document getTestDocument() throws IOException {
        Document document = factory.createDocument("acme", pageUrl,
                configService.getRunDateTime(), configService.getRunDateTime());
        URL fileURL = BaseParserIT.class.getResource(pageUrl);
        byte[] bytes = IOUtils.toByteArray(fileURL);
        final int bufferLength = 4086;
        byte[] compressedObject =
                CompressionUtil.compressByteArray(bytes, bufferLength);
        document.setDocumentObject(compressedObject);
        return document;
    }

    private Data getTestData() {
        Data data = factory.createData("price");
        data.setName("acme:priceTask:price");
        Axis col = factory.createAxis(AxisName.COL, "date",
                configService.getRunDateTime().toString(), null, 0, 0);
        Axis row =
                factory.createAxis(AxisName.ROW, "Price", "Price", null, 0, 0);
        Axis fact =
                factory.createAxis(AxisName.FACT, "fact", "315.25", null, 0, 0);

        Member member = factory.createMember();
        member.setAxes(Sets.newHashSet(fact, row, col));

        data.addMember(member);
        return data;
    }

    private Data insertTestData(final Long dataDefId, final Long documentId) {
        Data data = getTestData();
        data.setDataDefId(dataDefId);
        data.setDocumentId(documentId);

        DataDao dataDao = new DataDao(daoUtil.getPersistenceManagerFactory());
        dataDao.storeData(data);
        return dataDao.getData(data.getId());
    }
}
