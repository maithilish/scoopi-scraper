package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Locator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DocumentDaoIT extends ITBase {

    private DocumentDao dao;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

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
        Document document = objectFactory.createDocument("acme", "url1",
                new Date(), new Date());
        document.setDocumentObject("test data");
        Locator locator = objectFactory.createLocator("acme", "group1", "url1");
        locator.getDocuments().add(document);
        return locator;
    }
}
