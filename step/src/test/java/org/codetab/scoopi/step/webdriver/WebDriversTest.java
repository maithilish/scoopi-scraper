package org.codetab.scoopi.step.webdriver;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.codetab.scoopi.config.Configs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriversTest {
    @InjectMocks
    private WebDrivers webDrivers;

    @Mock
    private Configs configs;
    @Mock
    private DriverFactory driverFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExplicitlyWaitForDomReadyIfWaitTypeEqualsIgnoreCase()
            throws Exception {
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        String waitType = "explicit";
        String timeout = "500";
        @SuppressWarnings("unchecked")
        Function<WebDriver, Boolean> waitFunction =
                Mockito.mock(Function.class);
        WebDriverWait webDriverWait = Mockito.mock(WebDriverWait.class);

        when(configs.getConfig("scoopi.webDriver.waitType"))
                .thenReturn(waitType);
        when(configs.getConfig("scoopi.webDriver.timeout.explicitWait"))
                .thenReturn(timeout);
        when(driverFactory.createWaitFunction()).thenReturn(waitFunction);
        when(driverFactory.createWebDriverWait(webDriver, timeout))
                .thenReturn(webDriverWait);
        webDrivers.explicitlyWaitForDomReady(webDriver);

        verify(webDriverWait).until(waitFunction);
    }

    @Test
    public void testExplicitlyWaitForDomReadyElseWaitTypeEqualsIgnoreCase()
            throws Exception {
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        String waitType = "Foo"; // not explicit
        String timeout = "500";
        @SuppressWarnings("unchecked")
        Function<WebDriver, Boolean> waitFunction =
                Mockito.mock(Function.class);
        WebDriverWait webDriverWait = Mockito.mock(WebDriverWait.class);

        when(configs.getConfig("scoopi.webDriver.waitType"))
                .thenReturn(waitType);
        webDrivers.explicitlyWaitForDomReady(webDriver);

        verify(configs, never())
                .getConfig("scoopi.webDriver.timeout.explicitWait");
        verify(driverFactory, never()).createWaitFunction();
        verify(driverFactory, never()).createWebDriverWait(webDriver, timeout);
        verify(webDriverWait, never()).until(waitFunction);
    }

    @Test
    public void testSetImplicitTimeoutIfWaitTypeEqualsIgnoreCase()
            throws Exception {
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        String waitType = "implicit";
        String timeout = "500";
        Options options = Mockito.mock(Options.class);
        Timeouts timeouts = Mockito.mock(Timeouts.class);

        when(configs.getConfig("scoopi.webDriver.waitType"))
                .thenReturn(waitType);
        when(configs.getConfig("scoopi.webDriver.timeout.implicitWait"))
                .thenReturn(timeout);
        when(webDriver.manage()).thenReturn(options);
        when(options.timeouts()).thenReturn(timeouts);
        webDrivers.setImplicitTimeout(webDriver);

        verify(timeouts).implicitlyWait(Integer.valueOf(timeout),
                TimeUnit.SECONDS);
    }

    @Test
    public void testSetImplicitTimeoutElseWaitTypeEqualsIgnoreCase()
            throws Exception {
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        String waitType = "Foo";
        Options options = Mockito.mock(Options.class);
        Timeouts timeouts = Mockito.mock(Timeouts.class);

        when(configs.getConfig("scoopi.webDriver.waitType"))
                .thenReturn(waitType);
        webDrivers.setImplicitTimeout(webDriver);

        verify(configs, never())
                .getConfig("scoopi.webDriver.timeout.implicitWait");
        verify(webDriver, never()).manage();
        verify(options, never()).timeouts();
        verify(timeouts, never()).implicitlyWait(500, TimeUnit.SECONDS);
    }
}
