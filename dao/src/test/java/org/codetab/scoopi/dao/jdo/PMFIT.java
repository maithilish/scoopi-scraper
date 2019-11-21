package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.IOHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * <p>
 * PMF tests.
 * @author Maithilish
 *
 */
public class PMFIT {

    @Mock
    private ConfigService configService;
    @Spy
    private IOHelper ioHelper;
    @Spy
    private Properties jdoProperties;

    @InjectMocks
    private PMF pmf;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSingleton() {
        // given
        DInjector dInjector = new DInjector().instance(DInjector.class);

        // when
        PMF instanceA = dInjector.instance(PMF.class);
        PMF instanceB = dInjector.instance(PMF.class);

        // then
        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testInit() throws IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ConfigNotFoundException {

        String dsConfigFile = System.getProperty("scoopi.datastore.configFile");
        if (dsConfigFile == null) {
            dsConfigFile = "jdoconfig-dev.properties";
        }
        given(configService.getConfig("scoopi.datastore.configFile"))
                .willReturn(dsConfigFile);

        pmf.init();

        InOrder inOrder = inOrder(configService, ioHelper, jdoProperties);
        inOrder.verify(configService).getConfig("scoopi.datastore.configFile");
        inOrder.verify(ioHelper).getInputStream("/" + dsConfigFile);
        inOrder.verify(jdoProperties).load(any(InputStream.class));

        assertThat(pmf.getFactory()).isNotNull();
    }

    @Test
    public void testInitMultiple() throws IOException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ConfigNotFoundException {

        String dsConfigFile = System.getProperty("scoopi.datastore.configFile");
        if (dsConfigFile == null) {
            dsConfigFile = "jdoconfig-dev.properties";
        }

        given(configService.getConfig("scoopi.datastore.configFile"))
                .willReturn(dsConfigFile);

        pmf.init();

        InOrder inOrder = inOrder(configService, ioHelper, jdoProperties);
        inOrder.verify(configService).getConfig("scoopi.datastore.configFile");
        inOrder.verify(ioHelper).getInputStream("/" + dsConfigFile);
        inOrder.verify(jdoProperties).load(any(InputStream.class));

        assertThat(pmf.getFactory()).isNotNull();

        verifyNoMoreInteractions(configService, ioHelper);

        pmf.init();

        verifyNoMoreInteractions(configService, ioHelper);
    }

    @Test
    public void testInitThrowConfigNotFoundException()
            throws ConfigNotFoundException {
        given(configService.getConfig("scoopi.datastore.configFile"))
                .willThrow(ConfigNotFoundException.class);

        testRule.expect(CriticalException.class);
        pmf.init();
    }

    @Test
    public void testInitThrowFileNotFoundException()
            throws FileNotFoundException, ConfigNotFoundException {
        given(configService.getConfig("scoopi.datastore.configFile"))
                .willReturn("x.properties");

        testRule.expect(CriticalException.class);
        pmf.init();
    }
}
