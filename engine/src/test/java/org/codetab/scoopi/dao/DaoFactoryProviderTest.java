package org.codetab.scoopi.dao;

import static org.mockito.Mockito.verify;

import org.codetab.scoopi.dao.jdo.JdoDaoFactory;
import org.codetab.scoopi.di.DInjector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DaoFactoryProvider tests.
 * @author Maithilish
 *
 */
public class DaoFactoryProviderTest {

    @Mock
    private DInjector dInjector;

    @InjectMocks
    private DaoFactoryProvider daoFactoryProvider;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDaoFactoryJDO() {
        daoFactoryProvider.getDaoFactory(ORM.JDO);
        verify(dInjector).instance(JdoDaoFactory.class);
    }

    @Test
    public void testGetDaoFactoryDefault() {
        daoFactoryProvider.getDaoFactory(ORM.DEFUALT);
        verify(dInjector).instance(JdoDaoFactory.class);
    }

    @Test
    public void testGetDaoFactoryJPA() {
        testRule.expect(UnsupportedOperationException.class);
        daoFactoryProvider.getDaoFactory(ORM.JPA);
    }
}
