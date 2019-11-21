package org.codetab.scoopi.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codetab.scoopi.dao.ConfigHelper;
import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDataDefDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.dao.jdo.JdoDaoFactory;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DataDefPersistence tests.
 * @author Maithilish
 *
 */
public class DataDefPersistenceTest {

    @Mock
    private ConfigHelper configHelper;
    @Mock
    private DaoFactoryProvider daoFactoryProvider;
    @Mock
    private JdoDaoFactory jdoDao;
    @Mock
    private IDataDefDao dataDefDao;

    @InjectMocks
    private DataDefPersistence dataDefPersistence;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    private ObjectFactory objectFactory;
    private DataDef dataDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        objectFactory = new ObjectFactory();
        dataDef = objectFactory.createDataDef("acme", new Date(), new Date(),
                "test json");
    }

    @Test
    public void testLoadDataDefs() {
        Date runDate = new Date();
        List<DataDef> dataDefs = new ArrayList<>();
        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getDataDefDao()).willReturn(dataDefDao);
        given(configHelper.getRunDateTime()).willReturn(runDate);
        given(dataDefDao.getDataDefs(runDate)).willReturn(dataDefs);

        List<DataDef> actual = dataDefPersistence.loadDataDefs();

        InOrder inOrder =
                inOrder(configHelper, daoFactoryProvider, dataDefDao, jdoDao);
        inOrder.verify(configHelper).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getDataDefDao();
        inOrder.verify(configHelper).getRunDateTime();
        inOrder.verify(dataDefDao).getDataDefs(runDate);
        assertThat(actual).isSameAs(dataDefs);
    }

    @Test
    public void testLoadDataDefsShouldThrowException() {
        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(CriticalException.class);
        dataDefPersistence.loadDataDefs();
    }

    @Test
    public void testStoreDataDef() {
        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getDataDefDao()).willReturn(dataDefDao);

        dataDefPersistence.storeDataDef(dataDef);

        InOrder inOrder =
                inOrder(configHelper, daoFactoryProvider, dataDefDao, jdoDao);
        inOrder.verify(configHelper).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getDataDefDao();
        inOrder.verify(dataDefDao).storeDataDef(dataDef);
    }

    @Test
    public void testStoreDataDefIdNotNull() {
        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getDataDefDao()).willReturn(dataDefDao);

        dataDef.setId(1L);
        dataDefPersistence.storeDataDef(dataDef);

        InOrder inOrder =
                inOrder(configHelper, daoFactoryProvider, dataDefDao, jdoDao);
        inOrder.verify(configHelper).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getDataDefDao();
        inOrder.verify(dataDefDao).storeDataDef(dataDef);
    }

    @Test
    public void testStoreDataDefShouldThrowException() {
        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(CriticalException.class);
        dataDefPersistence.storeDataDef(dataDef);
    }

    @Test
    public void testStoreNullParams() {
        try {
            dataDefPersistence.storeDataDef(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("dataDef must not be null");
        }
    }

    @Test
    public void testPersistDataDef() {
        given(configHelper.useDataStore()).willReturn(true);
        given(configHelper.isPersist("scoopi.persist.dataDef"))
                .willReturn(true);

        boolean actual = dataDefPersistence.persistDataDef();

        assertThat(actual).isTrue();

        given(configHelper.useDataStore()).willReturn(false);
        given(configHelper.isPersist("scoopi.persist.dataDef"))
                .willReturn(true);

        actual = dataDefPersistence.persistDataDef();

        assertThat(actual).isFalse();

        given(configHelper.useDataStore()).willReturn(true);
        given(configHelper.isPersist("scoopi.persist.dataDef"))
                .willReturn(false);

        actual = dataDefPersistence.persistDataDef();

        assertThat(actual).isFalse();
    }
}
