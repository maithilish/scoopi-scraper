package org.codetab.scoopi.defs.yml.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.util.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryHelperTest {

    @InjectMocks
    private QueryHelper queryHelper;

    private static ObjectFactory factory;
    private static ObjectMapper mapper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        mapper = new ObjectMapper();
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetQuery() throws IOException {
        DataDef dataDef = getTestDataDef();

        assertThat(queryHelper.getQuery(dataDef, AxisName.FACT, "region"))
                .isEqualTo("queryregion");
        assertThat(queryHelper.getQuery(dataDef, AxisName.FACT, "field"))
                .isEqualTo("queryfield");
        assertThat(queryHelper.getQuery(dataDef, AxisName.COL, "script"))
                .isEqualTo("colscript");
    }

    @Test
    public void testGetQueryNoQuery() throws IOException {
        DataDef dataDef = getTestDataDef();

        testRule.expect(NoSuchElementException.class);
        queryHelper.getQuery(dataDef, AxisName.ROW, "region");
    }

    private DataDef getTestDataDef() throws IOException {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        String defJson = new StringBuilder().append("{price:{axis:")
                .append("{fact:{query:{region:queryregion,field:queryfield},")
                .append("members:[{member:{name:fact,index:0,order:10}}]},")
                .append("col:{query:{script:colscript},")
                .append("members:[{member:{name:date,index:1,order:11}}]},")
                .append("row:{query:{noQuery: ignored},")
                .append("members:[{member:{name:Price,value:Price,index:2,order:12}},")
                .append("{member:{name:High,match:High}}").append("]} }}}")
                .toString();
        JsonNode def = mapper.readTree(TestUtils.parseJson(defJson));
        DataDef dataDef =
                factory.createDataDef("price", fromDate, toDate, defJson);
        dataDef.setDef(def);
        return dataDef;
    }

}
