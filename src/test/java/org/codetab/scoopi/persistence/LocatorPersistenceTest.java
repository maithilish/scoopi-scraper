package org.codetab.scoopi.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.ILocatorDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.dao.jdo.JdoDaoFactory;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Locator;
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

/**
 * <p>
 * LocatorPersistence tests.
 * @author Maithilish
 *
 */
public class LocatorPersistenceTest {

    @Mock
    private ConfigService configService;

    @Mock
    private DaoFactoryProvider daoFactoryProvider;

    @Mock
    private JdoDaoFactory jdoDao;

    @Mock
    private ILocatorDao locatorDao;

    @InjectMocks
    private LocatorPersistence locatorPersistence;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    private ObjectFactory objectFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        objectFactory = new ObjectFactory();
    }

    @Test
    public void testLoadLocatorByNameGroup() {
        Locator locator = objectFactory.createLocator("name", "group", "url");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);
        given(locatorDao.getLocator("n", "g")).willReturn(locator);

        Locator actual = locatorPersistence.loadLocator("n", "g");

        assertThat(actual).isSameAs(locator);
        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).getLocator("n", "g");
        verifyNoMoreInteractions(configService, daoFactoryProvider, locatorDao,
                jdoDao);
    }

    @Test
    public void testLoadLocatorByNameGroupShouldThrowException() {
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        locatorPersistence.loadLocator("n", "g");
    }

    @Test
    public void testLoadByNameGroupNullParams() {
        try {
            locatorPersistence.loadLocator(null, "g");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }

        try {
            locatorPersistence.loadLocator("n", null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("group must not be null");
        }
    }

    @Test
    public void testLoadLocatorById() {
        Locator locator = objectFactory.createLocator("name", "group", "url");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);
        given(locatorDao.getLocator(1L)).willReturn(locator);

        Locator actual = locatorPersistence.loadLocator(1L);

        assertThat(actual).isSameAs(locator);
        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).getLocator(1L);
        verifyNoMoreInteractions(configService, daoFactoryProvider, locatorDao,
                jdoDao);
    }

    @Test
    public void testLoadLocatorByIdShouldThrowException() {
        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        locatorPersistence.loadLocator(1L);
    }

    @Test
    public void testStoreLocator() {
        Locator locator = objectFactory.createLocator("name", "group", "url");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);

        locatorPersistence.storeLocator(locator);

        InOrder inOrder =
                inOrder(configService, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configService).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).storeLocator(locator);
        verifyNoMoreInteractions(configService, daoFactoryProvider, locatorDao,
                jdoDao);
    }

    @Test
    public void testStoreLocatorShouldThrowException() {
        Locator locator = objectFactory.createLocator("name", "group", "url");

        given(configService.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        locatorPersistence.storeLocator(locator);
    }

    @Test
    public void testStoreNullParams() {
        try {
            locatorPersistence.storeLocator(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("locator must not be null");
        }
    }

    @Test
    public void testPersistLocatorNotUseDataStore() {
        // globally disabled
        given(configService.useDataStore()).willReturn(false);

        Optional<Boolean> persistDefined = Optional.ofNullable(null);
        assertThat(locatorPersistence.persistLocator(persistDefined)).isFalse();
    }

    @Test
    public void testPersistLocatorConfigSet() {
        // enabled at global but disabled at model level
        given(configService.useDataStore()).willReturn(true);
        given(configService.isPersist("scoopi.persist.locator"))
                .willReturn(true).willReturn(false);

        Optional<Boolean> persistDefined = Optional.ofNullable(null);
        assertThat(locatorPersistence.persistLocator(persistDefined)).isTrue();
        assertThat(locatorPersistence.persistLocator(persistDefined)).isFalse();
    }

    @Test
    public void testPersistLocatorTaskLevelPersistenceDefined() {
        // enabled at global and model level
        given(configService.useDataStore()).willReturn(true);
        given(configService.isPersist("scoopi.persist.locator"))
                .willReturn(true);

        // enabled at task level
        Optional<Boolean> persistDefined = Optional.ofNullable(true);
        assertThat(locatorPersistence.persistLocator(persistDefined)).isTrue();

        // disabled at task level
        persistDefined = Optional.ofNullable(false);
        assertThat(locatorPersistence.persistLocator(persistDefined)).isFalse();

        // undefined at task level
        persistDefined = Optional.ofNullable(null);
        assertThat(locatorPersistence.persistLocator(persistDefined)).isTrue();
    }
}
