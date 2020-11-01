package org.codetab.scoopi.step.webdriver;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDrivers {

    @Inject
    private Configs configs;

    public void explicitlyWaitForDomReady(final WebDriver webDriver)
            throws ConfigNotFoundException {
        String waitType = configs.getConfig("scoopi.webDriver.waitType");
        if (waitType.equalsIgnoreCase("explicit")) {
            String timeout =
                    configs.getConfig("scoopi.webDriver.timeout.explicitWait");
            new WebDriverWait(webDriver, Integer.parseInt(timeout)).until(
                    (ExpectedCondition<Boolean>) wd -> ((JavascriptExecutor) wd)
                            .executeScript("return document.readyState")
                            .equals("complete"));
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
