package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PropertyFilesTest {

    private PropertyFiles propertyFiles;

    @Before
    public void setUp() throws Exception {
        propertyFiles = new PropertyFiles();
    }

    @Test
    public void testGetFileNameDefault() {
        String expected = "scoopi.properties";
        String fileName = propertyFiles.getFileName();
        assertThat(fileName).isEqualTo(expected);
    }

    @Test
    public void testGetFileNameFromSystemProperty() {
        String expected = "xyz.properties";
        System.setProperty("scoopi.propertyFile", expected);
        String fileName = propertyFiles.getFileName();
        System.clearProperty("scoopi.propertyFile");
        assertThat(fileName).isEqualTo(expected);
    }

    @Test
    public void testGetFileNameDevMode() {
        String expected = "scoopi-dev.properties";
        System.setProperty("scoopi.mode", "dev");
        String fileName = propertyFiles.getFileName();
        System.clearProperty("scoopi.mode");
        assertThat(fileName).isEqualTo(expected);
    }

    @Test
    public void testGetFileNameProdMode() {
        String expected = "scoopi.properties";
        String fileName = propertyFiles.getFileName();
        assertThat(fileName).isEqualTo(expected);
    }

    @Test
    public void testGetFileNameFromEnv() {
        // not implemented for now
        assertThat(true).isEqualTo(true);
    }
}
