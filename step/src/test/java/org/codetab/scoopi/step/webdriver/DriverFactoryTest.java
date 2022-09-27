package org.codetab.scoopi.step.webdriver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.pool2.PooledObject;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverFactoryTest {
    @InjectMocks
    private DriverFactory driverFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateFireFoxOptions() {

        FirefoxOptions actual = driverFactory.createFireFoxOptions();
        @SuppressWarnings("rawtypes")
        Object args =
                ((Map) actual.asMap().get("moz:firefoxOptions")).get("args");
        List<String> expected = Arrays.asList("--headless", "--log warn");

        assertEquals(expected, args);
    }

    /**
     * Test needs geckodriver.
     *
     * Download and place geckodriver in $HOME/.gecko/geckodriver
     *
     * @throws ConfigNotFoundException
     */
    @Test
    public void testCreateFirefoxDriver() throws ConfigNotFoundException {

        String driverPath = ".gecko/geckodriver";
        if (!Paths.get(driverPath).isAbsolute()) {
            driverPath = String.join(File.separator,
                    System.getProperty("user.home"), driverPath);
        }
        System.setProperty("webdriver.gecko.driver", driverPath);

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.setLogLevel(FirefoxDriverLogLevel.FATAL);

        try {
            WebDriver actual = driverFactory.createFirefoxDriver(options);
            assertTrue(actual instanceof WebDriver);
        } catch (Exception e) {
            if (e.getMessage()
                    .startsWith("The driver executable does not exist")) {
                String msg = String.join(" ",
                        "The geckodrive is missing! Download and place it in $HOME/.gecko folder.",
                        e.getMessage());
                throw new CriticalException(msg, e);
            } else {
                throw e;
            }
        }
    }

    @Test
    public void testCreateDefaultPooledObject() {
        WebDriver webDriver = Mockito.mock(WebDriver.class);

        PooledObject<WebDriver> actual =
                driverFactory.createDefaultPooledObject(webDriver);

        assertSame(webDriver, actual.getObject());
    }

    @Test
    public void testCreateWebDriverWait() throws Exception {
        WebDriver webDriver = Mockito.mock(WebDriver.class);
        String timeout = "500";

        WebDriverWait actual =
                driverFactory.createWebDriverWait(webDriver, timeout);

        assertTrue(actual instanceof WebDriverWait);
    }

    @Test
    public void testCreateWaitFunction() {

        TestWebDriver testWebDriver = Mockito.mock(TestWebDriver.class);

        Function<WebDriver, Boolean> actual =
                driverFactory.createWaitFunction();

        when(testWebDriver.executeScript("return document.readyState"))
                .thenReturn("complete");

        assertTrue(actual.apply(testWebDriver));
    }

    /**
     * Test web driver to cast between WebDriver and JavascriptExecutor
     * @author m
     *
     */
    interface TestWebDriver extends WebDriver, JavascriptExecutor {

    }
}
