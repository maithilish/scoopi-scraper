package org.codetab.scoopi.itest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class Ex2IT extends ITestBase {

    private String exName = "ex-2";
    private String exBase = "/defs/examples";

    @Test
    public void jsoupTest() throws FileNotFoundException, IOException {
        String cat = "jsoup";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        System.setProperty("scoopi.defs.defaultSteps", "jsoupDefault");

        List<String> actual = runScoopi(exDir);

        List<String> expected = getExpectedList(expectedFile);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void htmlunitTest() throws FileNotFoundException, IOException {
        String cat = "htmlunit";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        System.setProperty("scoopi.defs.defaultSteps", "htmlUnitDefault");

        List<String> actual = runScoopi(exDir);

        List<String> expected = getExpectedList(expectedFile);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

}
