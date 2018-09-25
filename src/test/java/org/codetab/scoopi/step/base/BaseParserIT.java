package org.codetab.scoopi.step.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.dao.jdo.DataDao;
import org.codetab.scoopi.dao.jdo.JdoDaoUtilFactory;
import org.codetab.scoopi.defs.yml.DataDefDefs;
import org.codetab.scoopi.defs.yml.Defs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DataDefNotFoundException;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BaseParserIT {

    private static DInjector di;
    private static IDaoUtil daoUtil;
    private static HashSet<String> schemaClasses;
    private static Defs defs;
    private static ObjectFactory factory;
    private static ConfigService configService;

    private BaseParser parser;
    private Task task;
    private IStore store;
    private String pageUrl;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    // don't move this to base class, tests fail in cli
    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, IllegalAccessException, URISyntaxException {
        di = new DInjector();

        String defDir = "/testdefs/test-bs-short";

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
        parser = di.instance(Parser.class);
        task = di.instance(Task.class);
        task.setStep(parser);

        pageUrl = "/testdefs/page/acme-bs.html";

        schemaClasses.add("org.codetab.scoopi.model.Data");
        daoUtil.clearCache();
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @Test
    public void testNoExistingDataParseData()
            throws IllegalAccessException, IOException, InterruptedException {

        Document document = getTestDocument();
        StepInfo stepInfo = factory.createStepInfo("parser", "loader",
                "process", "clzName");
        Payload payload = getTestPayload(stepInfo, document);

        parser.setPayload(payload);

        task.run(); // run task

        // test persistence
        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));
        assertThat(actuals.size()).isEqualTo(1);
        Data aData = actuals.get(0); // persisted data

        Data eData = getTestData();

        assertThat(aData.getMembers().size()).isEqualTo(6);

        assertThat(aData.getMembers().size())
                .isEqualTo(eData.getMembers().size());

        Set<Axis> aAxes = aData.getMembers().get(0).getAxes();
        Set<Axis> eAxes = eData.getMembers().get(0).getAxes();
        assertThat(aAxes).containsAll(eAxes);

        aAxes = aData.getMembers().get(1).getAxes();
        eAxes = eData.getMembers().get(1).getAxes();
        assertThat(aAxes).containsAll(eAxes);

        aAxes = aData.getMembers().get(2).getAxes();
        eAxes = eData.getMembers().get(2).getAxes();
        assertThat(aAxes).containsAll(eAxes);

        aAxes = aData.getMembers().get(3).getAxes();
        eAxes = eData.getMembers().get(3).getAxes();
        assertThat(aAxes).containsAll(eAxes);

        aAxes = aData.getMembers().get(4).getAxes();
        eAxes = eData.getMembers().get(4).getAxes();
        assertThat(aAxes).containsAll(eAxes);

        aAxes = aData.getMembers().get(5).getAxes();
        eAxes = eData.getMembers().get(5).getAxes();
        assertThat(aAxes).containsAll(eAxes);

        // test handover
        Payload actualPayload = store.takePayload();
        assertThat(actualPayload).isNotNull();

        assertThat(actualPayload.getData()).isEqualTo(aData);
    }

    @Test
    public void testDataExistsReuseData()
            throws DataDefNotFoundException, IOException, InterruptedException {
        DataDefDefs dataDefDefs = di.instance(DataDefDefs.class);
        DataDef dataDef = dataDefDefs.getDataDef("bs");

        Document document = getTestDocument();
        document.setId(10L);

        Data existingData = insertTestData(dataDef.getId(), document.getId());

        StepInfo stepInfo = factory.createStepInfo("parser", "loader",
                "process", "clzName");
        Payload payload = getTestPayload(stepInfo, document);

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

    private Payload getTestPayload(final StepInfo stepInfo, final Object data) {
        JobInfo jobInfo =
                factory.createJobInfo(0, "acme", "bs", "bs", "steps", "bs");
        return factory.createPayload(jobInfo, stepInfo, data);
    }

    private Data getTestData() {
        Data data = factory.createData("bs");
        Axis col16 =
                factory.createAxis(AxisName.COL, "year", "Dec '16", null, 2, 0);
        Axis col15 =
                factory.createAxis(AxisName.COL, "year", "Dec '15", null, 3, 1);
        Axis row1 = factory.createAxis(AxisName.ROW, "item", "Reserves", null,
                8, 0);
        Axis row2 = factory.createAxis(AxisName.ROW, "item", "Networth", null,
                9, 1);
        Axis row3 = factory.createAxis(AxisName.ROW, "item", "Secured Loans",
                null, 10, 2);
        Axis fact1 = factory.createAxis(AxisName.FACT, "fact", "32,071.87",
                null, 0, 0);
        Axis fact2 = factory.createAxis(AxisName.FACT, "fact", "32,876.59",
                null, 0, 0);
        Axis fact3 =
                factory.createAxis(AxisName.FACT, "fact", "3.60", null, 0, 0);
        Axis fact4 =
                factory.createAxis(AxisName.FACT, "fact", "0.02", null, 0, 0);
        Axis fact5 = factory.createAxis(AxisName.FACT, "fact", "30,683.28",
                null, 0, 0);
        Axis fact6 = factory.createAxis(AxisName.FACT, "fact", "29,881.73",
                null, 0, 0);

        Member member1 = factory.createMember();
        member1.setAxes(Sets.newHashSet(fact1, col16, row1));

        Member member2 = factory.createMember();
        member2.setAxes(Sets.newHashSet(col16, row2, fact2));

        Member member3 = factory.createMember();
        member3.setAxes(Sets.newHashSet(col16, row3, fact3));

        Member member4 = factory.createMember();
        member4.setAxes(Sets.newHashSet(col15, row3, fact4));

        Member member5 = factory.createMember();
        member5.setAxes(Sets.newHashSet(col15, row2, fact5));

        Member member6 = factory.createMember();
        member6.setAxes(Sets.newHashSet(col15, row1, fact6));

        data.addMember(member1);
        data.addMember(member2);
        data.addMember(member3);
        data.addMember(member4);
        data.addMember(member5);
        data.addMember(member6);

        return data;
    }

    private Data insertTestData(final Long dataDefId, final Long documentId) {
        Data data = getTestData();
        data.setDataDefId(dataDefId);
        data.setDocumentId(documentId);

        DataDao dataDao = new DataDao(daoUtil.getPersistenceManagerFactory());
        dataDao.storeData(data);
        return data;
    }
}
