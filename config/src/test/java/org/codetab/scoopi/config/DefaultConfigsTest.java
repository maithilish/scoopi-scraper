package org.codetab.scoopi.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultConfigsTest {

    private static int totalConfigs = 56;

    private static XMLConfiguration defaults;
    private static int counter;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        counter = totalConfigs;
        Configurations configurations = new Configurations();
        URL url = DefaultConfigsTest.class.getResource("/scoopi-default.xml");
        defaults = configurations.xml(url);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // assert that all configs are accounted
        assertThat(counter).isEqualTo(0);
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSize() {
        assertThat(defaults.size()).isEqualTo(totalConfigs);
    }

    @Test
    public void testConfgs() {
        // top level direct configs
        int configCount = 5;
        assertThat(defaults.getString("scoopi.propertyPattern"))
                .isEqualTo("scoopi/properties/property");
        assertThat(defaults.getString("scoopi.highDate"))
                .isEqualTo("2037-12-31T23:59:59.999");
        assertThat(defaults.getString("scoopi.dateTimePattern"))
                .isEqualTo("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        assertThat(defaults.getString("scoopi.outputDirTimestampPattern"))
                .isEqualTo("yyyyMMMdd-HHmmss");
        assertThat(defaults.getBoolean("scoopi.wait")).isFalse();
        counter -= configCount;
    }

    @Test
    public void testDefs() {
        ImmutableConfiguration confs = defaults.immutableSubset("scoopi.defs");

        int configCount = 5;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getString("dir"))
                .isEqualTo("/defs/examples/fin/jsoup/quickstart");
        assertThat(confs.getString("defaultStepsFile"))
                .isEqualTo("/steps-default.yml");
        assertThat(confs.getString("defaultSteps")).isEqualTo("jsoupDefault");
        assertThat(confs.getString("definedSchema"))
                .isEqualTo("/schema/defs-defined.json");
        assertThat(confs.getString("effectiveSchema"))
                .isEqualTo("/schema/defs-effective.json");
        counter -= configCount;
    }

    @Test
    public void testFacts() {
        ImmutableConfiguration confs = defaults.immutableSubset("scoopi.fact");

        int configCount = 3;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getBoolean("blank.replace")).isTrue();
        assertThat(confs.getString("blank.replaceWith")).isEqualTo("-");
        assertThat(confs.getString("notFound.replaceWith"))
                .isEqualTo("not found");
        counter -= configCount;
    }

    @Test
    public void testSeeder() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.seeder");

        int configCount = 2;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getString("class"))
                .isEqualTo("org.codetab.scoopi.step.extract.LocatorSeeder");
        assertThat(confs.getInt("seedRetryTimes")).isEqualTo(3);
        counter -= configCount;
    }

    @Test
    public void testPoolsize() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.poolsize");

        int configCount = 7;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("start")).isEqualTo(4);
        assertThat(confs.getInt("seeder")).isEqualTo(6);
        assertThat(confs.getInt("loader")).isEqualTo(4);
        assertThat(confs.getInt("parser")).isEqualTo(4);
        assertThat(confs.getInt("process")).isEqualTo(4);
        assertThat(confs.getInt("filter")).isEqualTo(4);
        assertThat(confs.getInt("appender")).isEqualTo(2);
        counter -= configCount;
    }

    @Test
    public void testAppender() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.appender");

        int configCount = 2;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("queueSize")).isEqualTo(4096);
        assertThat(confs.getString("file.baseDir")).isEqualTo("");
        counter -= configCount;
    }

    @Test
    public void testDatastore() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.datastore");

        int configCount = 3;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getBoolean("enable")).isTrue();
        assertThat(confs.getString("type")).isEqualTo("jar:file:");
        assertThat(confs.getString("path")).isEqualTo("data");
        counter -= configCount;
    }

    @Test
    public void testPersist() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.persist");

        int configCount = 2;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getBoolean("locator")).isTrue();
        assertThat(confs.getBoolean("data")).isTrue();
        counter -= configCount;
    }

    @Test
    public void testCluster() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.cluster");

        int configCount = 7;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getBoolean("enable")).isFalse();
        assertThat(confs.getInt("tx.timeout")).isEqualTo(10);
        assertThat(confs.getString("tx.timeoutUnit")).isEqualTo("SECONDS");
        assertThat(confs.getString("tx.type")).isEqualTo("TWO_PHASE");
        assertThat(confs.getInt("shutdown.timeout")).isEqualTo(60);
        assertThat(confs.getString("shutdown.timeoutUnit"))
                .isEqualTo("SECONDS");
        assertThat(confs.getInt("startCrashCleaner.minThreshold"))
                .isEqualTo(10);
        counter -= configCount;
    }

    @Test
    public void testJob() {
        ImmutableConfiguration confs = defaults.immutableSubset("scoopi.job");

        int configCount = 3;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("takeLimit")).isEqualTo(4);
        assertThat(confs.getInt("takeTimeout")).isEqualTo(1000);
        assertThat(confs.getInt("takeRetryDelay")).isEqualTo(50);
        counter -= configCount;
    }

    @Test
    public void testTask() {
        ImmutableConfiguration confs = defaults.immutableSubset("scoopi.task");

        int configCount = 1;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("takeTimeout")).isEqualTo(500);
        counter -= configCount;
    }

    @Test
    public void testMonitor() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.monitor");

        int configCount = 1;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("timerPeriod")).isEqualTo(1000);
        counter -= configCount;
    }

    @Test
    public void testLoader() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.loader");

        int configCount = 2;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("fetch.parallelism")).isEqualTo(1);
        assertThat(confs.getInt("fetch.delay")).isEqualTo(1000);
        counter -= configCount;
    }

    @Test
    public void testMetrics() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.metrics");

        int configCount = 3;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getBoolean("server.enable")).isTrue();
        assertThat(confs.getInt("server.port")).isEqualTo(9010);
        assertThat(confs.getInt("serializer.period")).isEqualTo(5);
        counter -= configCount;
    }

    @Test
    public void testShutdown() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.shutdown");

        int configCount = 2;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("timeout")).isEqualTo(10);
        assertThat(confs.getString("timeoutUnit")).isEqualTo("SECONDS");
        counter -= configCount;
    }

    @Test
    public void testWebclient() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.webClient");

        int configCount = 2;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("timeout")).isEqualTo(120000);
        assertThat(confs.getString("userAgent")).isEqualTo(
                "Mozilla/5.0 (X11\\; Linux x86_64\\; rv:50.0) Gecko/20100101 Firefox/50.0");
        counter -= configCount;
    }

    @Test
    public void testWebdriver() {
        ImmutableConfiguration confs =
                defaults.immutableSubset("scoopi.webDriver");

        int configCount = 5;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getString("driverPath"))
                .isEqualTo(".gecko/geckodriver");
        assertThat(confs.getString("log")).isEqualTo("geckodriver.log");
        assertThat(confs.getString("waitType")).isEqualTo("explicit");
        assertThat(confs.getInt("timeout.explicitWait")).isEqualTo(10);
        assertThat(confs.getInt("timeout.implicitWait")).isEqualTo(10);
        counter -= configCount;
    }

    @Test
    public void testFork() {
        ImmutableConfiguration confs = defaults.immutableSubset("scoopi.fork");

        int configCount = 1;
        assertThat(confs.size()).isEqualTo(configCount);
        assertThat(confs.getInt("locator")).isEqualTo(0);
        counter -= configCount;
    }

}
