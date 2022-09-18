package org.codetab.scoopi.step.webdriver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool2.PooledObject;
import org.codetab.scoopi.config.Configs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class WebDriverFactoryTest {
    @InjectMocks
    private WebDriverFactory webDriverFactory;

    @Mock
    private Configs configs;
    @Mock
    private WebDrivers webDrivers;
    @Mock
    private DriverFactory driverFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateIf() throws Exception {
        String driverPath = "Bar";
        String banana = "Foo";
        FirefoxOptions options = Mockito.mock(FirefoxOptions.class);
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        Options options2 = Mockito.mock(Options.class);
        Window window = Mockito.mock(Window.class);

        when(configs.getConfig("scoopi.webDriver.driverPath"))
                .thenReturn(driverPath);
        when(configs.getConfig("scoopi.webDriver.log")).thenReturn(banana);
        when(driverFactory.createFireFoxOptions()).thenReturn(options);
        when(driverFactory.createFirefoxDriver(options)).thenReturn(webDriver);
        when(webDriver.manage()).thenReturn(options2);
        when(options2.window()).thenReturn(window);

        WebDriver actual = webDriverFactory.create();

        assertSame(webDriver, actual);
        verify(webDrivers).setImplicitTimeout(webDriver);
        verify(window).maximize();

        Logger l = Logger
                .getLogger("org.openqa.selenium.remote.ProtocolHandshake");
        assertEquals(Level.WARNING, l.getLevel());

        String driverLogFile = String.join(File.separator,
                System.getProperty("java.io.tmpdir"), banana);
        String effectiveDriverPath = String.join(File.separator,
                System.getProperty("user.home"), driverPath);

        assertEquals(effectiveDriverPath,
                System.getProperty("webdriver.gecko.driver"));
        assertEquals("true", System.getProperty(
                FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE));
        assertEquals(driverLogFile, System
                .getProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE));
    }

    @Test
    public void testCreateElse() throws Exception {
        String driverPath = "/Bar";
        String banana = "Foo";
        FirefoxOptions options = Mockito.mock(FirefoxOptions.class);
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        Options options2 = Mockito.mock(Options.class);
        Window window = Mockito.mock(Window.class);

        when(configs.getConfig("scoopi.webDriver.driverPath"))
                .thenReturn(driverPath);
        when(configs.getConfig("scoopi.webDriver.log")).thenReturn(banana);
        when(driverFactory.createFireFoxOptions()).thenReturn(options);
        when(driverFactory.createFirefoxDriver(options)).thenReturn(webDriver);
        when(webDriver.manage()).thenReturn(options2);
        when(options2.window()).thenReturn(window);

        WebDriver actual = webDriverFactory.create();

        assertSame(webDriver, actual);
        verify(webDrivers).setImplicitTimeout(webDriver);
        verify(window).maximize();

        Logger l = Logger
                .getLogger("org.openqa.selenium.remote.ProtocolHandshake");
        assertEquals(Level.WARNING, l.getLevel());

        String driverLogFile = String.join(File.separator,
                System.getProperty("java.io.tmpdir"), banana);
        String effectiveDriverPath = driverPath; // absolute path

        assertEquals(effectiveDriverPath,
                System.getProperty("webdriver.gecko.driver"));
        assertEquals("true", System.getProperty(
                FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE));
        assertEquals(driverLogFile, System
                .getProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE));
    }

    @Test
    public void testWrap() {
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        @SuppressWarnings("unchecked")
        PooledObject<WebDriver> pooledObject = Mockito.mock(PooledObject.class);

        when(driverFactory.createDefaultPooledObject(webDriver))
                .thenReturn(pooledObject);

        PooledObject<WebDriver> actual = webDriverFactory.wrap(webDriver);

        assertSame(pooledObject, actual);
    }

    @Test
    public void testPassivateObjectIfWebDriverGetWindowHandlesSize()
            throws Exception {
        @SuppressWarnings("unchecked")
        PooledObject<WebDriver> p = Mockito.mock(PooledObject.class);
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        Set<String> set = new HashSet<>();
        set.add("foo");
        set.add("bar");

        when(p.getObject()).thenReturn(webDriver);
        when(webDriver.getWindowHandles()).thenReturn(set);
        webDriverFactory.passivateObject(p);

        verify(webDriver).close();
    }

    @Test
    public void testPassivateObjectElseWebDriverGetWindowHandlesSize()
            throws Exception {
        @SuppressWarnings("unchecked")
        PooledObject<WebDriver> p = Mockito.mock(PooledObject.class);
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        Set<String> set = new HashSet<>();

        when(p.getObject()).thenReturn(webDriver);
        when(webDriver.getWindowHandles()).thenReturn(set);
        webDriverFactory.passivateObject(p);

        verify(webDriver, never()).close();
    }

    @Test
    public void testDestroyObject() throws Exception {
        @SuppressWarnings("unchecked")
        PooledObject<WebDriver> p = Mockito.mock(PooledObject.class);
        WebDriver webDriver = Mockito.mock(WebDriver.class);

        when(p.getObject()).thenReturn(webDriver);
        webDriverFactory.destroyObject(p);

        verify(webDriver).quit();
    }
}
