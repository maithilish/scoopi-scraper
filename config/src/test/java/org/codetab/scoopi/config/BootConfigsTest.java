package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class BootConfigsTest {

    private BootConfigs bootConfigs;

    @Before
    public void setUp() throws Exception {
        bootConfigs = new BootConfigs();
    }

    @Test
    public void testBootConfigInitFileNotFound() {
        System.setProperty("scoopi.propertyFile", "xyz.properties");
        bootConfigs = new BootConfigs();
        System.clearProperty("scoopi.propertyFile");
        assertThat(bootConfigs.getConfig("testKey", "testValue"))
                .isEqualTo("testValue");
        bootConfigs.setUserDefinedProperty("testKey", "userDefinedValue");
        assertThat(bootConfigs.getConfig("testKey", "testValue"))
                .isEqualTo("userDefinedValue");
    }

    @Test
    public void testConfigureLogPathDefault() {
        String key = "scoopi.log.dir";
        bootConfigs.configureLogPath();
        assertThat(bootConfigs.getConfig(key, "xyz")).isEqualTo("logs");
    }

    @Test
    public void testConfigureLogPathUserDefined() {
        String key = "scoopi.log.dir";
        String value = "today/logs";

        bootConfigs.setUserDefinedProperty(key, value);
        bootConfigs.configureLogPath();

        String actual = bootConfigs.getConfig(key, "xyz");
        System.clearProperty(key);
        assertThat(actual).isEqualTo(value);
    }

    @Test
    public void testConfigureLogPathClusterNoSplit() {
        String key = "scoopi.log.dir";

        bootConfigs.setUserDefinedProperty("scoopi.cluster.log.path.suffixUid",
                "false");
        bootConfigs.setUserDefinedProperty("scoopi.cluster.enable", "true");
        bootConfigs.configureLogPath();

        String actual = bootConfigs.getConfig(key, "xyz");
        System.clearProperty(key);
        assertThat(actual).isEqualTo("logs");
    }

    @Test
    public void testConfigureLogPathClusterSplitLogs() {
        String key = "scoopi.log.dir";

        bootConfigs.setUserDefinedProperty("scoopi.cluster.enable", "true");
        bootConfigs.configureLogPath();

        String actual = bootConfigs.getConfig(key, "xyz");
        System.clearProperty(key);
        // expected logs/xxxxxxxx
        assertThat(actual).startsWith("logs/");
        assertThat(actual).hasSize(13);
    }

    @Test
    public void testGetConfigFromSystemProperty() {
        String key = "scoopi.test.key";
        String value = "test.value";

        System.setProperty(key, value);
        String actual = bootConfigs.getConfig(key, "xyz");
        System.clearProperty(key);

        assertThat(actual).isEqualTo(value);
    }

    @Test
    public void testGetConfigFromUserDefinedConfig() {
        String key = "scoopi.test.key";
        String value = "test.value";

        bootConfigs.setUserDefinedProperty(key, value);
        String actual = bootConfigs.getConfig(key, "xyz");

        assertThat(actual).isEqualTo(value);
    }

    @Test
    public void testIsSoloFromSystemProperty() {
        System.setProperty("scoopi.cluster.enable", "true");
        assertThat(bootConfigs.isSolo()).isFalse();
        System.setProperty("scoopi.cluster.enable", "false");
        assertThat(bootConfigs.isSolo()).isTrue();
        System.clearProperty("scoopi.cluster.enable");
    }

    @Test
    public void testIsSoloFromUserDefinedConfig() {
        bootConfigs.setUserDefinedProperty("scoopi.cluster.enable", "true");
        assertThat(bootConfigs.isSolo()).isFalse();
        bootConfigs.setUserDefinedProperty("scoopi.cluster.enable", "false");
        assertThat(bootConfigs.isSolo()).isTrue();
    }
}
