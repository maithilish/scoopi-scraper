package org.codetab.scoopi.itest.book;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.dao.jdo.JdoDaoUtilFactory;
import org.codetab.scoopi.dao.jdo.LocatorDao;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.InitModule;
import org.codetab.scoopi.engine.ScoopiEngine;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Locator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

public class ITestBase {

    protected static DInjector di;
    protected static IDaoUtil daoUtil;
    protected static PersistenceManagerFactory pmf;
    protected static HashSet<String> schemaClasses;
    protected static MetricsHelper metricsHelper;
    protected static ConfigService configService;

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.clearCache();
    }

    @Before
    public void setUp() {
        di = new DInjector(new InitModule());

        schemaClasses = new HashSet<>();
        schemaClasses.add("org.codetab.scoopi.model.DataDef");
        schemaClasses.add("org.codetab.scoopi.model.Locator");
        schemaClasses.add("org.codetab.scoopi.model.Document");
        schemaClasses.add("org.codetab.scoopi.model.Data");

        configService = di.instance(ConfigService.class);
        configService.init("scoopi-test.properties", "scoopi-default.xml");

        daoUtil = new JdoDaoUtilFactory(di).getUtilDao();
        pmf = daoUtil.getPersistenceManagerFactory();

        metricsHelper = new MetricsHelper();

        // delete and create schema
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.clearCache();
        daoUtil.createSchemaForClasses(schemaClasses);
        metricsHelper.clearGuages();
    }

    @After
    public void tearDown() {
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.clearCache();
    }

    protected List<String> runScoopi(final String defDir)
            throws FileNotFoundException, IOException {

        System.setProperty("scoopi.useDatastore", "true");
        System.setProperty("scoopi.defs.dir", defDir);

        // delete the example output file
        String outputFile = "output/data.txt";
        FileUtils.deleteQuietly(new File(outputFile));

        ScoopiEngine scoopiEngine = di.instance(ScoopiEngine.class);
        scoopiEngine.start();

        return IOUtils.readLines(new FileInputStream(outputFile), "UTF-8");
    }

    protected List<String> getExpectedList(final String expectedFile) {
        List<String> expectedList = readFileAsList(expectedFile);
        expectedList = substituteVariables(expectedList);
        return expectedList;
    }

    private List<String> substituteVariables(final List<String> strings) {
        ConfigService cs = di.instance(ConfigService.class);
        Date runDateTime = cs.getRunDateTime();

        Map<String, String> map = new HashMap<>();
        map.put("runDateTime", runDateTime.toString());

        LocatorDao locatorDao = new LocatorDao(pmf);
        Locator locator = locatorDao.getLocator("books", "bookGroup");
        if (locator != null) {
            Date fromDate = locator.getDocuments().get(0).getFromDate();
            map.put("documentFromDate", fromDate.toString());
        }

        StringSubstitutor ss = new StringSubstitutor(map);
        ss.setVariablePrefix("%{"); //$NON-NLS-1$
        ss.setVariableSuffix("}"); //$NON-NLS-1$
        ss.setEscapeChar('%');

        List<String> list = new ArrayList<>();
        for (String str : strings) {
            list.add(ss.replace(str));
        }
        return list;
    }

    public String getExampleDir(final String exName, final String exBase,
            final String cat) {
        return String.join("/", exBase, cat, exName);
    }

    public String getExpectedFile(final String exName, final String exBase,
            final String cat) {
        return String.join("/", exBase, cat, exName, "expected.txt");
    }

    private static List<String> readFileAsList(final String fileName) {
        try {
            InputStream is = ITestBase.class.getResourceAsStream(fileName);
            return IOUtils.readLines(is, "UTF-8");
        } catch (IOException e) {
            return new ArrayList<String>();
        }
    }
}
