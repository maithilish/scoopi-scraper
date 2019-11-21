package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import javax.jdo.PersistenceManagerFactory;

import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.dao.IDataDefDao;
import org.codetab.scoopi.dao.IDataSetDao;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.dao.ILocatorDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * JdoDaoFactory tests.
 * @author Maithilish
 *
 */
public class JdoDaoFactoryTest {

    @Mock
    private PersistenceManagerFactory persistenceManagerFactory;

    @Mock
    private PMF pmf;

    @InjectMocks
    private JdoDaoFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetPmfInitAlreadyOver() {
        given(pmf.getFactory()).willReturn(persistenceManagerFactory);

        factory.setPmf(pmf);

        verify(pmf).init();
    }

    @Test
    public void testSetPmfNoInit() {
        verify(pmf).init();
    }

    @Test
    public void testSetPmfNullParams() {
        try {
            factory.setPmf(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("pmf must not be null");
        }
    }

    @Test
    public void testGetLocatorDao() {
        given(pmf.getFactory()).willReturn(persistenceManagerFactory);
        factory.setPmf(pmf);

        ILocatorDao dao = factory.getLocatorDao();
        assertThat(dao).isInstanceOf(LocatorDao.class);
    }

    @Test
    public void testGetDocumentDao() {
        given(pmf.getFactory()).willReturn(persistenceManagerFactory);
        factory.setPmf(pmf);

        IDocumentDao dao = factory.getDocumentDao();
        assertThat(dao).isInstanceOf(DocumentDao.class);
    }

    @Test
    public void testGetDataDefDao() {
        given(pmf.getFactory()).willReturn(persistenceManagerFactory);
        factory.setPmf(pmf);

        IDataDefDao dao = factory.getDataDefDao();
        assertThat(dao).isInstanceOf(DataDefDao.class);
    }

    @Test
    public void testGetDataDao() {
        given(pmf.getFactory()).willReturn(persistenceManagerFactory);
        factory.setPmf(pmf);

        IDataDao dao = factory.getDataDao();
        assertThat(dao).isInstanceOf(DataDao.class);
    }

    @Test
    public void testGetDataSetDao() {
        given(pmf.getFactory()).willReturn(persistenceManagerFactory);
        factory.setPmf(pmf);

        IDataSetDao dao = factory.getDataSetDao();
        assertThat(dao).isInstanceOf(DataSetDao.class);
    }
}
