package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

public class PropertyFilesTest {

    @Test
    public void testGetPropertyFileName() throws IOException {
        String propFile = "scoopi.properties";
        String devPropFile = "scoopi-dev.properties";

        PropertyFiles pp = new PropertyFiles();

        System.setProperty("scoopi.propertyFile", propFile);
        String actual = pp.getFileName();
        assertThat(actual).isEqualTo(propFile);

        System.clearProperty("scoopi.propertyFile");
        System.setProperty("scoopi.mode", "dev");
        actual = pp.getFileName();
        assertThat(actual).isEqualTo(devPropFile);

        // can't test env - skipped
        System.clearProperty("scoopi.mode");
        actual = pp.getFileName();
        assertThat(actual).isEqualTo(propFile);
    }

}
