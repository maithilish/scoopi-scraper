package org.codetab.scoopi.step.webdriver;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openqa.selenium.WebDriver;

@Singleton
public class WebDriverPool extends GenericObjectPool<WebDriver> {

    @Inject
    public WebDriverPool(final WebDriverFactory factory) {
        super(factory);
    }
}
