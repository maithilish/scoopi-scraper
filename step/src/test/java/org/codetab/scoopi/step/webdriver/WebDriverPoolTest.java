package org.codetab.scoopi.step.webdriver;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.apache.commons.pool2.impl.DefaultPooledObjectInfo;
import org.junit.Test;

public class WebDriverPoolTest {

    @Test
    public void testConstructor() throws Exception {
        WebDriverFactory factory = new WebDriverFactory();

        try (WebDriverPool webDriverPool = new WebDriverPool(factory)) {
            Set<DefaultPooledObjectInfo> set = webDriverPool.listAllObjects();
            assertEquals(0, set.size());
        }
    }

}
