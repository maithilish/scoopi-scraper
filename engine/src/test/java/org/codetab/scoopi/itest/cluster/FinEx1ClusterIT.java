package org.codetab.scoopi.itest.cluster;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codetab.scoopi.itest.ITestClusterBase;
import org.junit.Test;

public class FinEx1ClusterIT extends ITestClusterBase {

    private String exName = "ex-1";
    private String exBase = "/defs/examples/fin";
    private String outputDir = "output";

    @Test
    public void jsoupTest()
            throws FileNotFoundException, IOException, ParseException {
        String cat = "jsoup";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        deleteOutputDir(outputDir);

        Properties properties = getCommonProperties(exDir);

        String runDateTimeString = "01-07-2020 10:59:59.999";
        properties.put("scoopi.runDateTimeString", runDateTimeString);

        int timeout = 60;
        Map<String, Integer> nodesMap = new HashMap<>();
        nodesMap.put("cluster-a", timeout);
        nodesMap.put("cluster-b", timeout);
        nodesMap.put("cluster-c", timeout);

        runScoopiCluster(nodesMap, properties);

        List<String> actual = getOutputData(outputDir);
        List<String> expected =
                getExpectedList(expectedFile, runDateTimeString);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void htmlunitTest()
            throws FileNotFoundException, IOException, ParseException {
        String cat = "htmlunit";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        deleteOutputDir(outputDir);

        Properties properties = getCommonProperties(exDir);

        String runDateTimeString = "01-07-2020 10:59:59.999";
        properties.put("scoopi.runDateTimeString", runDateTimeString);
        properties.put("scoopi.defs.defaultSteps", "htmlUnitDefault");

        int timeout = 60;
        Map<String, Integer> nodesMap = new HashMap<>();
        nodesMap.put("cluster-a", timeout);
        nodesMap.put("cluster-b", timeout);
        nodesMap.put("cluster-c", timeout);

        runScoopiCluster(nodesMap, properties);

        List<String> actual = getOutputData(outputDir);
        List<String> expected =
                getExpectedList(expectedFile, runDateTimeString);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
