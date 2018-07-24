package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import javax.jdo.PersistenceManagerFactory;

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

}
