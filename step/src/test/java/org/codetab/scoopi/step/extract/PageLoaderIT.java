package org.codetab.scoopi.step.extract;

import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class PageLoaderIT {

    @InjectMocks
    private PageLoader pageLoader;
    private static ObjectFactory of;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        of = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Payload payload = createPayload();
        pageLoader.setPayload(payload);
        pageLoader.initialize();
    }

    @Test
    public void test() {

    }

    private Payload createPayload() {
        String name = "acme";
        String group = "foo";
        String url = "example.com";
        Locator locator = of.createLocator(name, group, url);
        JobInfo jobInfo = of.createJobInfo(name, group, "", "", "");
        StepInfo stepInfo = of.createStepInfo("", "", "", "");
        return of.createPayload(jobInfo, stepInfo, locator);
    }
}
