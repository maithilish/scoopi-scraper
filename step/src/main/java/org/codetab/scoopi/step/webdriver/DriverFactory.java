package org.codetab.scoopi.step.webdriver;

import java.util.function.Function;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverFactory {

    public FirefoxOptions createFireFoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--log warn");
        return options;
    }

    public WebDriver createFirefoxDriver(final FirefoxOptions options) {
        return new FirefoxDriver(options);
    }

    public PooledObject<WebDriver> createDefaultPooledObject(
            final WebDriver webDriver) {
        return new DefaultPooledObject<WebDriver>(webDriver);
    }

    public WebDriverWait createWebDriverWait(final WebDriver webDriver,
            final String timeout) {
        return new WebDriverWait(webDriver, Integer.parseInt(timeout));
    }

    public Function<WebDriver, Boolean> createWaitFunction() {
        Function<WebDriver, Boolean> function =
                (ExpectedCondition<Boolean>) wd -> ((JavascriptExecutor) wd)
                        .executeScript("return document.readyState")
                        .equals("complete");
        return function;
    }
}
