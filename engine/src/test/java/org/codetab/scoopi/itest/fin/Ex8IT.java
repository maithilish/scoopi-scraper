package org.codetab.scoopi.itest.fin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.codetab.scoopi.itest.ITestSoloBase;
import org.junit.Test;

public class Ex8IT extends ITestSoloBase {

    private String exName = "ex-8";
    private String exBase = "/defs/examples/fin";

    @Test
    public void jsoupTest() throws FileNotFoundException, IOException {
        String cat = "jsoup";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        Properties properties = getCommonProperties(exDir);

        List<String> actual = runScoopiSolo(properties);

        List<String> expected = getExpectedList(expectedFile);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void htmlunitTest() throws FileNotFoundException, IOException {
        String cat = "htmlunit";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        Properties properties = getCommonProperties(exDir);
        properties.setProperty("scoopi.defs.defaultSteps", "htmlUnitDefault");

        List<String> actual = runScoopiSolo(properties);

        List<String> expected = getExpectedList(expectedFile);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

}
