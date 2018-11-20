package org.codetab.scoopi.model.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.codetab.scoopi.defs.mig.IAxisDefs;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.system.ErrorLogger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class PayloadFactoryTest {

    @Mock
    private IAxisDefs axisDefs;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ErrorLogger errorLogger;

    @InjectMocks
    private PayloadFactory payloadFactory;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateSeedPayloads() {
        String clzName = "clz";
        String undefined = "undefined";
        String stepName = "seeder";

        String group1 = "group1";
        LocatorGroup locatorGroup1 = factory.createLocatorGroup(group1);
        String group2 = "group2";
        LocatorGroup locatorGroup2 = factory.createLocatorGroup(group2);

        ArrayList<LocatorGroup> locatorGroups =
                Lists.newArrayList(locatorGroup1, locatorGroup2);

        StepInfo stepInfo1 =
                factory.createStepInfo(stepName, undefined, undefined, clzName);
        JobInfo jobInfo1 = factory.createJobInfo(0, undefined, group1,
                undefined, undefined, undefined);
        Payload payload1 = factory.createPayload(jobInfo1, stepInfo1, "data1");

        StepInfo stepInfo2 =
                factory.createStepInfo(stepName, undefined, undefined, clzName);
        JobInfo jobInfo2 = factory.createJobInfo(0, undefined, group2,
                undefined, undefined, undefined);
        Payload payload2 = factory.createPayload(jobInfo2, stepInfo2, "data2");

        given(objectFactory.createStepInfo(stepName, undefined, undefined,
                clzName)).willReturn(stepInfo1);
        given(objectFactory.createJobInfo(0, undefined, group1, undefined,
                undefined, undefined)).willReturn(jobInfo1);
        given(objectFactory.createPayload(jobInfo1, stepInfo1, locatorGroup1))
                .willReturn(payload1);

        given(objectFactory.createStepInfo(stepName, undefined, undefined,
                clzName)).willReturn(stepInfo2);
        given(objectFactory.createJobInfo(0, undefined, group2, undefined,
                undefined, undefined)).willReturn(jobInfo2);
        given(objectFactory.createPayload(jobInfo2, stepInfo2, locatorGroup2))
                .willReturn(payload2);

        List<Payload> actual = payloadFactory.createSeedPayloads(locatorGroups,
                stepName, clzName);

        assertThat(actual.size()).isEqualTo(2);

        assertThat(actual).containsExactly(payload1, payload2);
    }

}
