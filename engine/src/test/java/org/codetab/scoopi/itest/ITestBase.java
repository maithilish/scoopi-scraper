package org.codetab.scoopi.itest;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.config.BootConfigs;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.engine.ScoopiEngine;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.metrics.MetricsHelper;

public class ITestBase {

    protected DInjector di;
    protected MetricsHelper metricsHelper;
    protected Configs configs;

    protected Properties getCommonProperties(final String defDir) {
        Properties properties = new Properties();
        properties.put("scoopi.defs.dir", defDir);
        properties.put("scoopi.propertyFile", "scoopi-test.properties");
        properties.put("scoopi.defs.defaultSteps", "jsoupDefault");
        properties.put("scoopi.cluster.enable", "false");
        properties.put("scoopi.dateTimePattern", "dd-MM-yyyy HH:mm:ss");

        // FIXME - itestfix remove this
        properties.put("scoopi.fact.notFound.replaceWith", "-");

        return properties;
    }

    protected void deleteOutputDir(final String outputDir) {
        File dir = new File(outputDir);
        FileUtils.deleteQuietly(dir);
    }

    protected List<String> getOutputData(final String outputDir)
            throws IOException, FileNotFoundException {
        List<String> list = new ArrayList<>();
        Collection<File> dataFiles = FileUtils.listFiles(new File(outputDir),
                new String[] {"txt"}, true);
        for (File dataFile : dataFiles) {
            list.addAll(
                    IOUtils.readLines(new FileInputStream(dataFile), "UTF-8"));
        }
        return list;
    }

    protected void bootstrap() {
        BootConfigs bootConfigs = new BootConfigs();
        bootConfigs.configureLogPath();
        Bootstrap bootstrap = new Bootstrap(bootConfigs);
        bootstrap.bootDi();

        bootstrap.bootCluster();
        bootstrap.waitForQuorum();

        // setup config and defs
        bootstrap.setup();

        di = bootstrap.getdInjector();
        metricsHelper = new MetricsHelper();
        metricsHelper.clearGuages();
    }

    protected void runEngine() {
        ScoopiEngine scoopiEngine = di.instance(ScoopiEngine.class);
        try {
            scoopiEngine.initSystem();
            scoopiEngine.runJobs();
            scoopiEngine.waitForShutdown();
        } catch (Exception e) {
            // ignore, error logged in scoopiEngine
        } finally {
            scoopiEngine.shutdown();
        }
    }

    protected List<String> getExpectedList(final String expectedFile) {
        List<String> expectedList = readFileAsList(expectedFile);
        try {
            expectedList = substituteVariables(expectedList, null);
        } catch (ConfigNotFoundException e) {
            // date is null, no parse
        }
        return expectedList;
    }

    protected List<String> getExpectedList(final String expectedFile,
            final String runDateTimeText) throws ConfigNotFoundException {
        List<String> expectedList = readFileAsList(expectedFile);
        expectedList = substituteVariables(expectedList, runDateTimeText);
        return expectedList;
    }

    private List<String> substituteVariables(final List<String> strings,
            final String runDateTimeText) throws ConfigNotFoundException {

        Map<String, String> map = new HashMap<>();

        if (isNull(runDateTimeText)) {
            configs = di.instance(Configs.class);
            ZonedDateTime runDateTime = configs.getRunDateTime();
            // FIXME - itestfix correct this
            ZonedDateTime documentFromDate = runDateTime;

            DateTimeFormatter formatter = getDateTimeFormatter(
                    configs.getConfig("scoopi.dateTimePattern"));
            map.put("runDateTime", runDateTime.format(formatter));
            map.put("documentFromDate", documentFromDate.format(formatter));
        } else {
            map.put("runDateTime", runDateTimeText);
            map.put("documentFromDate", runDateTimeText);
        }

        StringSubstitutor ss = new StringSubstitutor(map);
        ss.setVariablePrefix("%{"); //$NON-NLS-1$
        ss.setVariableSuffix("}"); //$NON-NLS-1$
        ss.setEscapeChar('%');

        List<String> list = new ArrayList<>();
        for (String str : strings) {
            list.add(ss.replace(str));
        }
        return list;
    }

    private DateTimeFormatter getDateTimeFormatter(final String pattern) {
        DateTimeFormatter formatter;
        if (isNull(pattern)) {
            formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        } else {
            formatter = DateTimeFormatter.ofPattern(pattern);

            // FIXME - datefix, what is outcome
            if (isNull(formatter.getZone())) {
                formatter = formatter.withZone(ZoneId.systemDefault());
            }
        }
        return formatter;
    }

    public String getExampleDir(final String exName, final String exBase,
            final String cat) {
        return String.join("/", exBase, cat, exName);
    }

    public String getExpectedFile(final String exName, final String exBase,
            final String cat) {
        return String.join("/", exBase, cat, exName, "expected.txt");
    }

    private static List<String> readFileAsList(final String fileName) {
        try {
            InputStream is = ITestBase.class.getResourceAsStream(fileName);
            return IOUtils.readLines(is, "UTF-8");
        } catch (IOException e) {
            return new ArrayList<String>();
        }
    }
}
