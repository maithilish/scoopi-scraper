package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;

import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.shared.ConfigService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DocumentDaoIT {

    private static DInjector di;
    private static IDaoUtil daoUtil;
    private static HashSet<String> schemaClasses;
    private static ObjectFactory factory;
    private static ConfigService configService;

    private DocumentDao dao;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    // don't move this to base class, tests fail in cli
    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, IllegalAccessException, URISyntaxException {
        di = new DInjector();

        configService = di.instance(ConfigService.class);
        configService.init("scoopi.properties", "scoopi-default.xml");
        configService.getConfigs().setProperty("scoopi.useDatastore", "true");

        daoUtil = new JdoDaoUtilFactory(di).getUtilDao();
        factory = di.instance(ObjectFactory.class);
        schemaClasses = new HashSet<>();
    }

    @Before
    public void setUp() throws Exception {
        dao = new DocumentDao(daoUtil.getPersistenceManagerFactory());

        daoUtil.clearCache();
        schemaClasses.add("org.codetab.scoopi.model.Locator");
        schemaClasses.add("org.codetab.scoopi.model.Document");
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @Test
    public void testGetDocument() {
        Locator locator = createTestLocator();
        LocatorDao locatorDao =
                new LocatorDao(daoUtil.getPersistenceManagerFactory());
        locatorDao.storeLocator(locator);

        Document actual =
                dao.getDocument(locator.getDocuments().get(0).getId());

        assertThat(actual).isEqualTo(locator.getDocuments().get(0));
        assertThat(actual.getDocumentObject()).isEqualTo("test data");
    }

    private Locator createTestLocator() {
        Document document =
                factory.createDocument("acme", "url1", new Date(), new Date());
        document.setDocumentObject("test data");
        Locator locator = factory.createLocator("acme", "group1", "url1");
        locator.getDocuments().add(document);
        return locator;
    }
}
