package org.codetab.scoopi.pool;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDrivers {

    @Inject
    private ConfigService configService;

    public void explicitlyWaitForDomReady(final WebDriver webDriver)
            throws ConfigNotFoundException {
        String waitType = configService.getConfig("scoopi.webDriver.waitType");
        if (waitType.equalsIgnoreCase("explicit")) {
            String timeout = configService
                    .getConfig("scoopi.webDriver.timeout.explicitWait");
            new WebDriverWait(webDriver, Integer.parseInt(timeout)).until(
                    (ExpectedCondition<Boolean>) wd -> ((JavascriptExecutor) wd)
                            .executeScript("return document.readyState")
                            .equals("complete"));
        }
    }

    public void setImplicitTimeout(final WebDriver webDriver)
            throws ConfigNotFoundException {
        String waitType = configService.getConfig("scoopi.webDriver.waitType");
        if (waitType.equalsIgnoreCase("implicit")) {
            String timeout = configService
                    .getConfig("scoopi.webDriver.timeout.implicitWait");
            webDriver.manage().timeouts()
                    .implicitlyWait(Integer.valueOf(timeout), TimeUnit.SECONDS);
        }
    }
}
