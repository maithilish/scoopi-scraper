package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Properties;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigPropertiesTest {

    private Properties properties;
    private ConfigProperties configProperties;

    @Mock
    private Properties mockProps;
    @InjectMocks
    private ConfigProperties mockConfigProps;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        configProperties = new ConfigProperties(properties);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetConfig() throws ConfigNotFoundException {
        properties.setProperty("testKey", "testValue");
        assertThat(configProperties.getConfig("testKey"))
                .isEqualTo("testValue");
    }

    @Test
    public void testGetConfigConfigNotFound() throws ConfigNotFoundException {
        assertThrows(ConfigNotFoundException.class,
                () -> configProperties.getConfig("undefinedKey"));
    }

    @Test
    public void testGetStringArray() throws ConfigNotFoundException {
        properties.setProperty("testKey", "foo ; bar ; baz");
        String[] actuals = configProperties.getStringArray("testKey");
        assertThat(actuals).containsExactly("foo", "bar", "baz");
    }

    @Test
    public void testGetStringArrayNull() throws ConfigNotFoundException {
        when(mockProps.getProperty("testKey")).thenReturn(null);

        assertThrows(ConfigNotFoundException.class,
                () -> mockConfigProps.getStringArray("testKey"));
    }

    @Test
    public void testGetStringArrayIsEmpty() throws ConfigNotFoundException {
        properties.setProperty("testKey", "");

        assertThrows(ConfigNotFoundException.class,
                () -> configProperties.getStringArray("testKey"));
    }

    @Test
    public void testGetBoolean() {
        properties.setProperty("testKey", "true");
        assertThat(configProperties.getBoolean("testKey", false)).isTrue();
    }

    @Test
    public void testGetBooleanNull() throws ConfigNotFoundException {
        when(mockProps.getProperty("testKey")).thenReturn(null);

        assertThat(configProperties.getBoolean("testKey", false)).isFalse();
        assertThat(configProperties.getBoolean("testKey", true)).isTrue();
    }

    @Test
    public void testGetBooleanNotABoolean() {
        properties.setProperty("testKey", "xyz");
        assertThat(configProperties.getBoolean("testKey", false)).isFalse();
        assertThat(configProperties.getBoolean("testKey", true)).isTrue();
    }

    @Test
    public void testGetInt() {
        properties.setProperty("testKey", "5");
        assertThat(configProperties.getInt("testKey", 10)).isEqualTo(5);
    }

    @Test
    public void testGetIntNull() throws ConfigNotFoundException {
        when(mockProps.getProperty("testKey")).thenReturn(null);

        assertThat(configProperties.getInt("testKey", 99)).isEqualTo(99);
    }

    @Test
    public void testGetIntNotInt() {
        properties.setProperty("testKey", "xyz");
        assertThat(configProperties.getInt("testKey", 99)).isEqualTo(99);
    }

    @Test
    public void testGet() {
        Date date = new Date();
        configProperties.put("testKey", date);
        assertThat(configProperties.get("testKey")).isEqualTo(date);
    }
}
