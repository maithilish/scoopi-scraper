package org.codetab.scoopi.dao;

import static org.assertj.core.api.Assertions.assertThat;
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
 * DaoFactory tests.
 * @author Maithilish
 *
 */
public class DaoFactoryTest {

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
    public void testGetDaoFactoryJPAShouldThrowException() {
        testRule.expect(UnsupportedOperationException.class);
        daoFactoryProvider.getDaoFactory(ORM.JPA);
    }

    @Test
    public void testGetDaoFactoryInstance() {
        IDaoFactory df1 = daoFactoryProvider.getDaoFactory(ORM.JDO);
        IDaoFactory df2 = daoFactoryProvider.getDaoFactory(ORM.JDO);

        assertThat(df1).isSameAs(df2);
    }
}
