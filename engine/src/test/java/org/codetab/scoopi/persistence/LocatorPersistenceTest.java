package org.codetab.scoopi.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.codetab.scoopi.dao.ConfigHelper;
import org.codetab.scoopi.dao.DaoFactoryProvider;
import org.codetab.scoopi.dao.ILocatorDao;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.dao.jdo.JdoDaoFactory;
import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.model.Locator;
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
 * LocatorPersistence tests.
 * @author Maithilish
 *
 */
public class LocatorPersistenceTest {

    @Mock
    private ConfigHelper configHelper;

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

        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);
        given(locatorDao.getLocator("n", "g")).willReturn(locator);

        Locator actual = locatorPersistence.loadLocator("n", "g");

        assertThat(actual).isSameAs(locator);
        InOrder inOrder =
                inOrder(configHelper, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configHelper).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).getLocator("n", "g");
        verifyNoMoreInteractions(configHelper, daoFactoryProvider, locatorDao,
                jdoDao);
    }

    @Test
    public void testLoadLocatorByNameGroupShouldThrowException() {
        given(configHelper.getOrmType()).willReturn(ORM.JDO);
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

        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);
        given(locatorDao.getLocator(1L)).willReturn(locator);

        Locator actual = locatorPersistence.loadLocator(1L);

        assertThat(actual).isSameAs(locator);
        InOrder inOrder =
                inOrder(configHelper, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configHelper).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).getLocator(1L);
        verifyNoMoreInteractions(configHelper, daoFactoryProvider, locatorDao,
                jdoDao);
    }

    @Test
    public void testLoadLocatorByIdShouldThrowException() {
        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO))
                .willThrow(RuntimeException.class);

        testRule.expect(StepPersistenceException.class);
        locatorPersistence.loadLocator(1L);
    }

    @Test
    public void testStoreLocator() {
        Locator locator = objectFactory.createLocator("name", "group", "url");

        given(configHelper.getOrmType()).willReturn(ORM.JDO);
        given(daoFactoryProvider.getDaoFactory(ORM.JDO)).willReturn(jdoDao);
        given(jdoDao.getLocatorDao()).willReturn(locatorDao);

        locatorPersistence.storeLocator(locator);

        InOrder inOrder =
                inOrder(configHelper, daoFactoryProvider, locatorDao, jdoDao);
        inOrder.verify(configHelper).getOrmType();
        inOrder.verify(daoFactoryProvider).getDaoFactory(ORM.JDO);
        inOrder.verify(jdoDao).getLocatorDao();
        inOrder.verify(locatorDao).storeLocator(locator);
        verifyNoMoreInteractions(configHelper, daoFactoryProvider, locatorDao,
                jdoDao);
    }

    @Test
    public void testStoreLocatorShouldThrowException() {
        Locator locator = objectFactory.createLocator("name", "group", "url");

        given(configHelper.getOrmType()).willReturn(ORM.JDO);
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
    public void testPersistUseDataStoreFalse() {
        // globally disabled
        given(configHelper.useDataStore()).willReturn(false);

        Optional<Boolean> persistDefined = Optional.ofNullable(null);
        assertThat(locatorPersistence.persist(persistDefined)).isFalse();
    }

    @Test
    public void testPersistConfigSet() {
        // enabled at global but disabled at model level
        given(configHelper.useDataStore()).willReturn(true);
        given(configHelper.isPersist("scoopi.persist.locator")).willReturn(true)
                .willReturn(false);

        Optional<Boolean> persistDefined = Optional.ofNullable(null);
        assertThat(locatorPersistence.persist(persistDefined)).isTrue();
        assertThat(locatorPersistence.persist(persistDefined)).isFalse();
    }

    @Test
    public void testPersistTaskLevelPersistenceDefined() {
        // enabled at global and model level
        given(configHelper.useDataStore()).willReturn(true);
        given(configHelper.isPersist("scoopi.persist.locator"))
                .willReturn(true);

        // enabled at task level
        Optional<Boolean> persistDefined = Optional.ofNullable(true);
        assertThat(locatorPersistence.persist(persistDefined)).isTrue();

        // disabled at task level
        persistDefined = Optional.ofNullable(false);
        assertThat(locatorPersistence.persist(persistDefined)).isFalse();

        // undefined at task level
        persistDefined = Optional.ofNullable(null);
        assertThat(locatorPersistence.persist(persistDefined)).isTrue();
    }
}
