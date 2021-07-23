package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.codetab.scoopi.exception.CriticalException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigBuilderTest {

    @InjectMocks
    private ConfigBuilder configBuilder;

    @Mock
    private CompositeConfiguration configuration;
    @Mock
    private Configurations configurations;
    @Mock
    private SystemConfiguration systemConfigs;
    @Mock
    private DerivedConfigs derivedConfigs;
    @Mock
    private PropertiesConfiguration userProvided;
    @Mock
    private XMLConfiguration defaults;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuild() throws ConfigurationException, ParseException {
        when(configurations.properties(new File("userDefined")))
                .thenReturn(userProvided);
        when(configurations.xml(new File("default"))).thenReturn(defaults);

        configBuilder.build("userDefined", "default");

        InOrder inOrder = inOrder(configuration, derivedConfigs);

        inOrder.verify(configuration).addConfiguration(systemConfigs);
        inOrder.verify(configuration).addConfiguration(userProvided);
        inOrder.verify(configuration).addConfiguration(defaults);
        inOrder.verify(derivedConfigs).addRunDates(configuration);
        inOrder.verify(derivedConfigs).replaceHighDate(configuration);
        inOrder.verify(derivedConfigs).addRunnerClass(configuration);
        verifyNoMoreInteractions(configuration, derivedConfigs);
    }

    @Test
    public void testBuildUserDefinedConfigException()
            throws ConfigurationException {
        when(configurations.properties(new File("userDefined")))
                .thenThrow(ConfigurationException.class);

        configBuilder.build("userDefined", "default");

        verify(configuration).addConfiguration(systemConfigs);
        verify(configuration, never()).addConfiguration(userProvided);
        verify(configuration)
                .addConfiguration(any(PropertiesConfiguration.class));
    }

    @Test
    public void testBuildDefaultConfigException()
            throws ConfigurationException, ParseException {
        when(configurations.xml(new File("default")))
                .thenThrow(ConfigurationException.class);

        assertThrows(CriticalException.class,
                () -> configBuilder.build("userDefined", "default"));
    }

    @Test
    public void testBuildAddDatesException()
            throws ConfigurationException, ParseException {
        when(configurations.properties(new File("userDefined")))
                .thenReturn(userProvided);
        when(configurations.xml(new File("default"))).thenReturn(defaults);

        doThrow(ParseException.class).when(derivedConfigs)
                .addRunDates(configuration);

        assertThrows(CriticalException.class,
                () -> configBuilder.build("userDefined", "default"));
    }

    @Test
    public void testGetEffectiveProperties() {
        String[] strs = {"key1", "key2"};
        Iterable<String> keys = () -> Arrays.stream(strs).iterator();
        when(configuration.getKeys()).thenReturn(keys.iterator());
        when(configuration.getProperty("key1")).thenReturn("value1");
        when(configuration.getProperty("key2")).thenReturn("value2");

        Properties properties = configBuilder.getEffectiveProperties();

        assertThat(properties).hasSize(2);
        assertThat(properties.get("key1")).isEqualTo("value1");
        assertThat(properties.get("key2")).isEqualTo("value2");
    }

    @Test
    public void testLogConfigs() {
        Properties properties = new Properties();
        properties.setProperty("java.class.path", "xyz");
        properties.setProperty("key1", "value1");
        properties.setProperty("key2", "value2");

        StringBuilder sb = configBuilder.logConfigs(properties);
        String expected = String.join(System.lineSeparator(), "key1=value1",
                "key2=value2", "");

        assertThat(sb.toString()).isEqualTo(expected);
    }

}
