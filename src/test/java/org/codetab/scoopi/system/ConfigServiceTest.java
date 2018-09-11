package org.codetab.scoopi.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.dao.ORM;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.system.ConfigService.ConfigIndex;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigServiceTest {

    private static final int DEFAULT_CONFIGS_COUNT = 29;

    private static final int USER_CONFIGS_COUNT = 2;

    @Mock
    private CompositeConfiguration configs;

    @InjectMocks
    private ConfigService configService;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConfigIndex() {
        // for test coverage of enum, we need to run both values and valueOf
        assertThat(ConfigIndex.SYSTEM).isEqualTo(ConfigIndex.values()[0]);
        assertThat(ConfigIndex.PROVIDED).isEqualTo(ConfigIndex.values()[1]);
        assertThat(ConfigIndex.DEFAULTS).isEqualTo(ConfigIndex.values()[2]);
        assertThat(ConfigIndex.SYSTEM).isEqualTo(ConfigIndex.valueOf("SYSTEM"));
        assertThat(ConfigIndex.PROVIDED)
                .isEqualTo(ConfigIndex.valueOf("PROVIDED"));
        assertThat(ConfigIndex.DEFAULTS)
                .isEqualTo(ConfigIndex.valueOf("DEFAULTS"));
    }

    @Test
    public void testSingleton() {
        DInjector dInjector = new DInjector().instance(DInjector.class);

        ConfigService instanceA = dInjector.instance(ConfigService.class);
        ConfigService instanceB = dInjector.instance(ConfigService.class);

        assertThat(instanceA).isNotNull();
        assertThat(instanceA).isSameAs(instanceB);
    }

    @Test
    public void testGetConfig() throws ConfigNotFoundException {
        given(configs.getString("xyz")).willReturn("xxx");

        configService.getConfig("xyz");
        verify(configs).getString("xyz");
    }

    @Test
    public void testGetConfigNull() throws ConfigNotFoundException {
        given(configs.getString("xyz")).willReturn(null);

        testRule.expect(ConfigNotFoundException.class);
        configService.getConfig("xyz"); // sut
    }

    @Test
    public void testGetConfigArray() throws ConfigNotFoundException {
        String[] array = {"x", "y"};
        given(configs.getStringArray("xyz")).willReturn(array);

        configService.getConfigArray("xyz");

        verify(configs).getStringArray("xyz");
    }

    @Test
    public void testGetConfigArrayNull() throws ConfigNotFoundException {
        String[] array = {}; // zero length
        given(configs.getStringArray("xyz")).willReturn(array);

        testRule.expect(ConfigNotFoundException.class);
        configService.getConfigArray("xyz"); // sut
    }

    @Test
    public void testConfigsInvalidFiles() {
        testRule.expect(CriticalException.class);
        configService.init("xyz", "xyz");
    }

    @Test
    public void testConfigsInvalidUserProvidedFile() {
        configService.init("xyz", "scoopi-default.xml");
        Configuration defaults =
                configService.getConfiguration(ConfigIndex.DEFAULTS);
        assertThat(defaults.size()).isEqualTo(DEFAULT_CONFIGS_COUNT);

        Configuration userProvided =
                configService.getConfiguration(ConfigIndex.PROVIDED);
        assertThat(userProvided.size()).isEqualTo(0);
    }

    @Test
    public void testAddRunDateAndTimeAlreadySetAsSystemProperty()
            throws ConfigNotFoundException, ParseException {
        String runDate = "1980-01-01";
        String runDateTime = "1980-01-01 03:02:01";
        System.setProperty("scoopi.runDate", runDate);
        System.setProperty("scoopi.runDateTime", runDateTime);

        configService.init("xyz", "scoopi-default.xml");

        String actual = configService.getConfig("scoopi.runDate");
        assertThat(actual).isEqualTo(runDate);

        actual = configService.getConfig("scoopi.runDateTime");
        assertThat(actual).isEqualTo(runDateTime);

        System.clearProperty("scoopi.runDateTime");
        System.clearProperty("scoopi.runDate");
    }

    @Test
    public void testGetRunDate()
            throws ConfigNotFoundException, ParseException {
        configService.init("xyz", "scoopi-default.xml");
        String runDate = configService.getConfig("scoopi.runDate");
        String[] patterns =
                configService.getConfigArray("scoopi.dateParsePattern");
        Date expected = DateUtils.parseDate(runDate, patterns);

        Date actual = configService.getRunDate();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateInvalidPattern() throws Exception {
        configService.init("xyz", "scoopi-default.xml");
        Configuration configuration =
                configService.getConfiguration(ConfigIndex.DEFAULTS);
        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("scoopi.dateParsePattern", invalidPattern);

        testRule.expect(CriticalException.class);
        configService.getRunDate();
    }

    @Test
    public void testGetRunDateTime() throws Exception {
        configService.init("xyz", "scoopi-default.xml");
        String runDate =
                configService.getConfigs().getString("scoopi.runDateTime");
        String[] patterns = configService.getConfigs()
                .getStringArray("scoopi.dateTimeParsePattern");
        Date expected = DateUtils.parseDate(runDate, patterns);

        Date actual = configService.getRunDateTime();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetRunDateTimeInvalidPattern() throws Exception {
        configService.init("xyz", "scoopi-default.xml");
        Configuration configuration =
                configService.getConfiguration(ConfigIndex.DEFAULTS);

        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("scoopi.dateTimeParsePattern",
                invalidPattern);

        testRule.expect(CriticalException.class);
        configService.getRunDateTime();
    }

    @Test
    public void testHighDate() throws Exception {
        configService.init("xyz", "scoopi-default.xml");
        String runDate =
                configService.getConfigs().getString("scoopi.highDate");
        String[] patterns = configService.getConfigs()
                .getStringArray("scoopi.dateTimeParsePattern");
        Date expected = DateUtils.parseDate(runDate, patterns);

        Date actual = configService.getHighDate();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetOrmType() throws Exception {
        given(configs.getString("scoopi.datastore.orm")).willReturn("jdo")
                .willReturn("jDo").willReturn("jpa").willReturn("jPa")
                .willReturn(null);

        assertThat(configService.getOrmType()).isEqualTo(ORM.JDO);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JDO);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JPA);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JPA);
        assertThat(configService.getOrmType()).isEqualTo(ORM.JDO);
    }

    @Test
    public void testGetHighDateInvalidPattern() throws Exception {
        configService.init("xyz", "scoopi-default.xml");
        Configuration configuration =
                configService.getConfiguration(ConfigIndex.DEFAULTS);
        String[] invalidPattern = {"ddMMYYYY"};
        configuration.setProperty("scoopi.dateTimeParsePattern",
                invalidPattern);

        testRule.expect(CriticalException.class);
        configService.getHighDate();
    }

    @Test
    public void testIsTestMode() {
        assertThat(configService.isTestMode()).isTrue();
    }

    @Test
    public void testIsDevMode() {
        configService.init("xyz", "scoopi-default.xml");
        Configuration configuration =
                configService.getConfiguration(ConfigIndex.DEFAULTS);
        assertThat(configService.isDevMode()).isFalse();
        configuration.setProperty("scoopi.mode", "dev");

        boolean devMode = configService.isDevMode();

        assertThat(devMode).isTrue();
    }

    @Test
    public void testIsPersist() {
        configService.init("xyz", "scoopi-default.xml");
        Configuration configuration =
                configService.getConfiguration(ConfigIndex.DEFAULTS);

        configuration.setProperty("scoopi.persist.locator", "true");
        assertThat(configService.isPersist("scoopi.persist.locator")).isTrue();

        configuration.setProperty("scoopi.persist.locator", "false");
        assertThat(configService.isPersist("scoopi.persist.locator")).isFalse();
    }

    @Test
    public void testUseDataStore() {
        configService.init("xyz", "scoopi-default.xml");

        assertThat(configService.useDataStore()).isTrue();
    }

    @Test
    public void testDefaultConfigs() throws Exception {
        String userProvidedFile = "xyz";
        String defaultsFile = "scoopi-default.xml";
        configService.init(userProvidedFile, defaultsFile);

        Configuration defaultConfigs =
                configService.getConfiguration(ConfigIndex.DEFAULTS);

        assertThat(defaultConfigs.size()).isEqualTo(DEFAULT_CONFIGS_COUNT);

        assertThat(defaultConfigs.getString("scoopi.propertyPattern"))
                .isEqualTo("scoopi/properties/property");

        assertThat(defaultConfigs.getString("scoopi.defs.dir"))
                .isEqualTo("/defs/examples/jsoup/ex-1");
        assertThat(defaultConfigs.getString("scoopi.defs.defaultSteps"))
                .isEqualTo("/steps-default.yml");
        assertThat(defaultConfigs.getString("scoopi.defs.definedSchema"))
                .isEqualTo("/schema/defs-defined.json");
        assertThat(defaultConfigs.getString("scoopi.defs.effectiveSchema"))
                .isEqualTo("/schema/defs-effective.json");
        assertThat(defaultConfigs.getString("scoopi.seederClass"))
                .isEqualTo("org.codetab.scoopi.step.extract.LocatorSeeder");

        assertThat(defaultConfigs.getString("scoopi.poolsize.seeder"))
                .isEqualTo("6");
        assertThat(defaultConfigs.getString("scoopi.poolsize.loader"))
                .isEqualTo("4");
        assertThat(defaultConfigs.getString("scoopi.poolsize.parser"))
                .isEqualTo("4");
        assertThat(defaultConfigs.getString("scoopi.poolsize.process"))
                .isEqualTo("4");
        assertThat(defaultConfigs.getString("scoopi.poolsize.converter"))
                .isEqualTo("4");
        assertThat(defaultConfigs.getString("scoopi.poolsize.appender"))
                .isEqualTo("2");
        assertThat(defaultConfigs.getString("scoopi.appender.queuesize"))
                .isEqualTo("4096");

        assertThat(defaultConfigs.getString("scoopi.webClient.timeout"))
                .isEqualTo("120000");
        assertThat(defaultConfigs.getString("scoopi.webClient.userAgent"))
                .isEqualTo(
                        "Mozilla/5.0 (X11; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0");
        assertThat(defaultConfigs.getString("scoopi.highDate"))
                .isEqualTo("31-12-2037 23:59:59.999");

        assertThat(defaultConfigs.getString("scoopi.waitForHeapDump"))
                .isEqualTo("false");
        assertThat(defaultConfigs.getString("scoopi.fork.locator"))
                .isEqualTo("0");

        assertThat(defaultConfigs.getString("scoopi.useDatastore"))
                .isEqualTo("true");
        assertThat(defaultConfigs.getString("scoopi.datastore.name"))
                .isEqualTo("datastore");
        assertThat(defaultConfigs.getString("scoopi.datastore.orm"))
                .isEqualTo("jdo");
        assertThat(defaultConfigs.getString("scoopi.datastore.configFile"))
                .isEqualTo("jdoconfig.properties");
        assertThat(defaultConfigs.getString("scoopi.persist.dataDef"))
                .isEqualTo("true");
        assertThat(defaultConfigs.getString("scoopi.persist.locator"))
                .isEqualTo("true");
        assertThat(defaultConfigs.getString("scoopi.persist.data"))
                .isEqualTo("true");

        assertThat(defaultConfigs.getString("scoopi.metrics.server.enable"))
                .isEqualTo("true");
        assertThat(defaultConfigs.getString("scoopi.metrics.server.port"))
                .isEqualTo("9010");

        String[] dateTimePatterns =
                {"dd-MM-yyyy HH:mm:ss.SSS", "dd/MM/yyyy HH:mm:ss.SSS"};
        assertArrayEquals(
                defaultConfigs.getStringArray("scoopi.dateTimeParsePattern"),
                dateTimePatterns);
        String[] datePatterns = {"dd-MM-yyyy", "dd/MM/yyyy"};
        assertArrayEquals(
                defaultConfigs.getStringArray("scoopi.dateParsePattern"),
                datePatterns);
    }

    /*
     * test stock scoopi.properties that is distributed
     */
    @Test
    public void testUserProvidedConfigs() {
        configService.init("scoopi.properties", "scoopi-default.xml");

        Configuration userConfigs =
                configService.getConfiguration(ConfigIndex.PROVIDED);

        assertThat(userConfigs.size()).isEqualTo(USER_CONFIGS_COUNT);

        assertThat(userConfigs.getString("scoopi.defs.dir"))
                .isEqualTo("/defs/examples/jsoup/ex-1");
        assertThat(userConfigs.getString("scoopi.useDatastore"))
                .isEqualTo("false");
    }
}
