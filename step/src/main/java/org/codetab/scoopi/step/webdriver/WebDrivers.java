package org.codetab.scoopi.step.webdriver;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.openqa.selenium.WebDriver;

public class WebDrivers {

    @Inject
    private Configs configs;
    @Inject
    private DriverFactory driverFactory;

    public void explicitlyWaitForDomReady(final WebDriver webDriver)
            throws ConfigNotFoundException {
        String waitType = configs.getConfig("scoopi.webDriver.waitType");
        if (waitType.equalsIgnoreCase("explicit")) {
            String timeout =
                    configs.getConfig("scoopi.webDriver.timeout.explicitWait");
            Function<WebDriver, Boolean> waitFunction =
                    driverFactory.createWaitFunction();
            driverFactory.createWebDriverWait(webDriver, timeout)
                    .until(waitFunction);
        }
    }

    public void setImplicitTimeout(final WebDriver webDriver)
            throws ConfigNotFoundException {
        String waitType = configs.getConfig("scoopi.webDriver.waitType");
        if (waitType.equalsIgnoreCase("implicit")) {
            String timeout =
                    configs.getConfig("scoopi.webDriver.timeout.implicitWait");
            webDriver.manage().timeouts()
                    .implicitlyWait(Integer.valueOf(timeout), TimeUnit.SECONDS);
        }
    }
}
