package org.codetab.scoopi.itest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class ITestSoloBase extends ITestBase {

    @Override
    protected Properties getCommonProperties(final String defDir) {
        Properties properties = super.getCommonProperties(defDir);
        properties.put("scoopi.cluster.enable", "false");
        return properties;
    }

    protected List<String> runScoopiSolo(final Properties properties)
            throws FileNotFoundException, IOException {

        for (Object key : properties.keySet()) {
            System.setProperty((String) key, (String) properties.get(key));
        }

        String outputDir = "output";
        deleteOutputDir(outputDir);

        bootstrap();
        runEngine();

        return getOutputData(outputDir);
    }
}
