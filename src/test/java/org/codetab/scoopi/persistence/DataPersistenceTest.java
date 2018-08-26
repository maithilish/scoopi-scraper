package org.codetab.scoopi.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.dao.jdo.JdoDaoFactory;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.shared.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataPersistenceTest {

    @Mock
    private ConfigService configService;
    @Mock
    private DaoFactoryProvider daoFactoryProvider;
    @Mock
    private JdoDaoFactory jdoDaoFactory;
    @Mock
    private IDataDao dataDao;

    @InjectMocks
    private DataPersistence dataPersistence;

    private ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        factory = new ObjectFactory();
    }

    @Test
    public void testLoadDataByDataDefAndDocumentId() {
        long documentId = 1L;
        long dataDefId = 2L;
        Data data = factory.createData("price");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willReturn(jdoDaoFactory);
        given(jdoDaoFactory.getDataDao()).willReturn(dataDao);
        given(dataDao.getData(documentId, dataDefId)).willReturn(data);

        Data actual = dataPersistence.loadData(dataDefId, documentId);

        assertThat(actual).isSameAs(data);
        InOrder inOrder = inOrder(configService, daoFactoryProvider, dataDao,
                jdoDaoFactory);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDaoFactory).getDataDao();
        inOrder.verify(dataDao).getData(documentId, dataDefId);
        verifyNoMoreInteractions(configService, daoFactoryProvider, dataDao,
                jdoDaoFactory);
    }

    @Test
    public void testLoadDataByDataDefAndDocumentIdShouldThrowException() {
        long documentId = 1L;
        long dataDefId = 2L;

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willReturn(jdoDaoFactory);
        given(jdoDaoFactory.getDataDao()).willReturn(dataDao);
        given(dataDao.getData(documentId, dataDefId))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        dataPersistence.loadData(dataDefId, documentId);
    }

    @Test
    public void testLoadDataById() {
        long dataId = 1L;
        Data data = factory.createData("price");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willReturn(jdoDaoFactory);
        given(jdoDaoFactory.getDataDao()).willReturn(dataDao);
        given(dataDao.getData(dataId)).willReturn(data);

        Data actual = dataPersistence.loadData(dataId);

        assertThat(actual).isSameAs(data);
        InOrder inOrder = inOrder(configService, daoFactoryProvider, dataDao,
                jdoDaoFactory);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDaoFactory).getDataDao();
        inOrder.verify(dataDao).getData(dataId);
        verifyNoMoreInteractions(configService, daoFactoryProvider, dataDao,
                jdoDaoFactory);
    }

    @Test
    public void testLoadDataByIdShouldThrowException() {
        long dataId = 1L;

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willReturn(jdoDaoFactory);
        given(jdoDaoFactory.getDataDao()).willReturn(dataDao);
        given(dataDao.getData(dataId)).willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        dataPersistence.loadData(dataId);
    }

    @Test
    public void testStoreData() {
        Data data = factory.createData("price");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willReturn(jdoDaoFactory);
        given(jdoDaoFactory.getDataDao()).willReturn(dataDao);

        boolean actual = dataPersistence.storeData(data);

        assertThat(actual).isTrue();
        InOrder inOrder = inOrder(configService, daoFactoryProvider, dataDao,
                jdoDaoFactory);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDaoFactory).getDataDao();
        inOrder.verify(dataDao).storeData(data);
        verifyNoMoreInteractions(configService, daoFactoryProvider, dataDao,
                jdoDaoFactory);
    }

    @Test
    public void testStoreDataShouldThrowException() {
        Data data = factory.createData("price");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willReturn(jdoDaoFactory);
        given(jdoDaoFactory.getDataDao()).willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        dataPersistence.storeData(data);
    }

    @Test
    public void testPersistUseDataStoreFalse() {
        // globally disabled
        given(configService.useDataStore()).willReturn(false);

        Optional<Boolean> persistDefined = Optional.ofNullable(null);
        assertThat(dataPersistence.persist(persistDefined)).isFalse();
    }

    @Test
    public void testPersistConfigSet() {
        // enabled at global but disabled at model level
        given(configService.useDataStore()).willReturn(true);
        given(configService.isPersist("scoopi.persist.data")).willReturn(true)
                .willReturn(false);

        Optional<Boolean> persistDefined = Optional.ofNullable(null);
        assertThat(dataPersistence.persist(persistDefined)).isTrue();
        assertThat(dataPersistence.persist(persistDefined)).isFalse();
    }

    @Test
    public void testPersistTaskLevelPersistenceDefined() {
        // enabled at global and model level
        given(configService.useDataStore()).willReturn(true);
        given(configService.isPersist("scoopi.persist.data")).willReturn(true);

        // enabled at task level
        Optional<Boolean> persistDefined = Optional.ofNullable(true);
        assertThat(dataPersistence.persist(persistDefined)).isTrue();

        // disabled at task level
        persistDefined = Optional.ofNullable(false);
        assertThat(dataPersistence.persist(persistDefined)).isFalse();

        // undefined at task level
        persistDefined = Optional.ofNullable(null);
        assertThat(dataPersistence.persist(persistDefined)).isTrue();
    }

}
