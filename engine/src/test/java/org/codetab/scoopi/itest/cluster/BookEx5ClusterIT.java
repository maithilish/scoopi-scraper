package org.codetab.scoopi.itest.cluster;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.itest.ITestClusterBase;
import org.junit.Test;

public class BookEx5ClusterIT extends ITestClusterBase {

    private String exName = "ex-5";
    private String exBase = "/defs/examples/book";
    private String outputDir = "output";
    private String runDateTimeText = "01-07-2020 10:59:59";

    @Test
    public void jsoupTest()
            throws FileNotFoundException, IOException, ConfigNotFoundException {
        String cat = "jsoup";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        deleteOutputDir(outputDir);

        Properties properties = getCommonProperties(exDir);
        properties.put("scoopi.runDateTimeText", runDateTimeText);

        int timeout = 60;
        Map<String, Integer> nodesMap = new HashMap<>();
        nodesMap.put("cluster-a", timeout);
        nodesMap.put("cluster-b", timeout);
        nodesMap.put("cluster-c", timeout);

        runScoopiCluster(nodesMap, properties);

        List<String> actual = getOutputData(outputDir);
        List<String> expected = getExpectedList(expectedFile, runDateTimeText);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }

    @Test
    public void htmlunitTest()
            throws FileNotFoundException, IOException, ConfigNotFoundException {
        String cat = "htmlunit";
        String exDir = getExampleDir(exName, exBase, cat);
        String expectedFile = getExpectedFile(exName, exBase, cat);

        deleteOutputDir(outputDir);

        Properties properties = getCommonProperties(exDir);
        properties.put("scoopi.runDateTimeText", runDateTimeText);
        properties.put("scoopi.defs.defaultSteps", "htmlUnitDefault");

        int timeout = 60;
        Map<String, Integer> nodesMap = new HashMap<>();
        nodesMap.put("cluster-a", timeout);
        nodesMap.put("cluster-b", timeout);
        nodesMap.put("cluster-c", timeout);

        runScoopiCluster(nodesMap, properties);

        List<String> actual = getOutputData(outputDir);
        List<String> expected = getExpectedList(expectedFile, runDateTimeText);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsAll(expected);
    }
}
