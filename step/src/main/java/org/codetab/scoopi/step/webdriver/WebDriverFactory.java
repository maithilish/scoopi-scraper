package org.codetab.scoopi.step.webdriver;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.codetab.scoopi.config.Configs;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

class WebDriverFactory extends BasePooledObjectFactory<WebDriver> {

    @Inject
    private Configs configs;
    @Inject
    private WebDrivers webDrivers;
    @Inject
    private DriverFactory driverFactory;

    @Override
    public WebDriver create() throws Exception {
        String driverPath = configs.getConfig("scoopi.webDriver.driverPath");
        if (!Paths.get(driverPath).isAbsolute()) {
            driverPath = String.join(File.separator,
                    System.getProperty("user.home"), driverPath);
        }

        String driverLogFile = String.join(File.separator,
                System.getProperty("java.io.tmpdir"),
                configs.getConfig("scoopi.webDriver.log"));

        Logger l = Logger
                .getLogger("org.openqa.selenium.remote.ProtocolHandshake");
        l.setLevel(Level.WARNING);
        System.setProperty("webdriver.gecko.driver", driverPath);
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,
                "true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,
                driverLogFile);
        FirefoxOptions options = driverFactory.createFireFoxOptions();
        WebDriver webDriver = driverFactory.createFirefoxDriver(options);
        webDrivers.setImplicitTimeout(webDriver);
        webDriver.manage().window().maximize();
        return webDriver;
    }

    @Override
    public PooledObject<WebDriver> wrap(final WebDriver webDriver) {
        return driverFactory.createDefaultPooledObject(webDriver);
    }

    @Override
    public void passivateObject(final PooledObject<WebDriver> p)
            throws Exception {
        WebDriver webDriver = p.getObject();
        // close active window except the last one
        if (webDriver.getWindowHandles().size() > 1) {
            webDriver.close();
        }
    }

    @Override
    public void destroyObject(final PooledObject<WebDriver> p)
            throws Exception {
        // close web driver
        p.getObject().quit();
    }
}
