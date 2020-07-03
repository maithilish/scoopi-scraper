package org.codetab.scoopi.dao.fs;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DocumentDaoTest {

    private DocumentDao dao;

    @Before
    public void setUp() throws Exception {
        dao = new DocumentDao();
        FsHelper fsHelper = new FsHelper();
        FieldUtils.writeDeclaredField(dao, "helper", fsHelper, true);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSave() throws DaoException {
        Locator locator =
                createLocator("acme", "snapshot", "http://example.com");
        // dao.delete(locator.getFingerprint().getValue());
        // dao.save(locator.getFingerprint().getValue(), locator);
        //
        // Locator actual = dao.get(locator.getFingerprint().getValue());
        // assertThat(actual).isEqualTo(locator);
    }

    private Locator createLocator(final String name, final String group,
            final String url) {
        ObjectFactory of = new ObjectFactory();
        return of.createLocator(name, group, url);
    }
}
