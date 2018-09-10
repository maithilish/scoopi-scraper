package org.codetab.scoopi.step.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.defs.yml.DataDefDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.helper.FilterHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataFilterTest {

    @Mock
    private FilterHelper filterHelper;
    @Mock
    private DataDefDefs dataDefDefs;
    @Mock
    private Data data;

    @InjectMocks
    private DataFilter dataFilter;

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
    public void testProcess() throws DataDefNotFoundException {
        dataFilter.setPayload(getTestPayload());

        DataDef dataDef = factory.createDataDef("price");
        Map<AxisName, List<Filter>> filterMap = new HashMap<>();
        List<Member> filterMembers = new ArrayList<>();

        given(dataDefDefs.getDataDef(dataDef.getName())).willReturn(dataDef);
        given(filterHelper.getFilterMap(dataDef)).willReturn(filterMap);
        given(filterHelper.getFilterMembers(data.getMembers(), filterMap))
                .willReturn(filterMembers);

        boolean actual = dataFilter.process();

        assertThat(actual).isTrue();
        assertThat(dataFilter.getOutput()).isEqualTo(data);
        assertThat(dataFilter.isConsistent()).isTrue();
        verify(filterHelper).filter(data.getMembers(), filterMembers);
    }

    @Test
    public void testProcessShouldThrowException()
            throws DataDefNotFoundException {
        dataFilter.setPayload(getTestPayload());

        given(dataDefDefs.getDataDef("price"))
                .willThrow(DataDefNotFoundException.class);

        testRule.expect(StepRunException.class);
        dataFilter.process();
    }

    public Payload getTestPayload() {
        JobInfo jobInfo =
                factory.createJobInfo(0, "acme", "quote", "price", "price");
        return factory.createPayload(jobInfo, null, null);
    }
}
