package org.codetab.scoopi.defs.yml.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.defs.yml.AxisDefs;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.util.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DataDefHelperTest {

    @Mock
    private ConfigService configService;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private YamlHelper yamlHelper;
    @Mock
    private AxisDefs axisDefs;

    @InjectMocks
    private DataDefHelper dataDefHelper;

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
    public void testCreateDataDefs() throws IOException {
        DataDef dataDef1 = factory.createDataDef("def1", new Date(), new Date(),
                "{body: test1}");
        DataDef dataDef2 = factory.createDataDef("def2", new Date(), new Date(),
                "{body: test2}");

        String json = TestUtils.parseJson(
                "{ def1 : { body : test1 }, def2 : { body : test2} }");
        JsonNode defs = mapper.readTree(json);

        JsonNode def1 = defs.at("/def1");
        JsonNode def2 = defs.at("/def2");

        String defJson1 = "{ body: test1 }";
        String defJson2 = "{ body: test2 }";

        Date runDate = new Date();
        Date highDate = DateUtils.addYears(runDate, 10);

        given(yamlHelper.toJson(def1)).willReturn(defJson1);
        given(yamlHelper.toJson(def2)).willReturn(defJson2);

        given(configService.getRunDateTime()).willReturn(runDate);
        given(configService.getHighDate()).willReturn(highDate);

        given(objectFactory.createDataDef("def1", runDate, highDate, defJson1))
                .willReturn(dataDef1);
        given(objectFactory.createDataDef("def2", runDate, highDate, defJson2))
                .willReturn(dataDef2);

        List<DataDef> actual = dataDefHelper.createDataDefs(defs);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).containsExactly(dataDef1, dataDef2);
    }

    @Test
    public void testMarkForUpdationNoExistingDataDef() {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 10);

        DataDef dataDef1 =
                factory.createDataDef("def1", fromDate, toDate, "json1");
        List<DataDef> oldDataDefs = Lists.newArrayList(dataDef1);

        DataDef dataDef2 =
                factory.createDataDef("def2", fromDate, toDate, "json2");
        DataDef dataDef3 =
                factory.createDataDef("def3", fromDate, toDate, "json3");
        List<DataDef> newDataDefs = Lists.newArrayList(dataDef2, dataDef3);

        boolean actual =
                dataDefHelper.markForUpdation(newDataDefs, oldDataDefs);

        assertThat(actual).isTrue();

        assertThat(oldDataDefs).containsExactly(dataDef1, dataDef2, dataDef3);

        assertThat(dataDef1.getFromDate()).isEqualTo(fromDate);
        assertThat(dataDef2.getFromDate()).isEqualTo(fromDate);
        assertThat(dataDef3.getFromDate()).isEqualTo(fromDate);

        assertThat(dataDef1.getToDate()).isEqualTo(toDate);
        assertThat(dataDef2.getToDate()).isEqualTo(toDate);
        assertThat(dataDef3.getToDate()).isEqualTo(toDate);
    }

    @Test
    public void testMarkForUpdationHasDataDefButNoChanges() {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 10);

        DataDef dataDef1 =
                factory.createDataDef("def1", fromDate, toDate, "json1");
        DataDef dataDef2 =
                factory.createDataDef("def2", fromDate, toDate, "json2");
        List<DataDef> oldDataDefs = Lists.newArrayList(dataDef1, dataDef2);

        DataDef dataDef3 =
                factory.createDataDef("def1", fromDate, toDate, "json1");
        List<DataDef> newDataDefs = Lists.newArrayList(dataDef3);

        boolean actual =
                dataDefHelper.markForUpdation(newDataDefs, oldDataDefs);

        assertThat(actual).isFalse();

        assertThat(oldDataDefs).containsExactly(dataDef1, dataDef2);

        assertThat(dataDef1.getFromDate()).isEqualTo(fromDate);
        assertThat(dataDef2.getFromDate()).isEqualTo(fromDate);

        assertThat(dataDef1.getToDate()).isEqualTo(toDate);
        assertThat(dataDef2.getToDate()).isEqualTo(toDate);
    }

    @Test
    public void testMarkForUpdationHasChanges() {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 10);

        DataDef dataDef1 =
                factory.createDataDef("def1", fromDate, toDate, "json1");
        DataDef dataDef2 =
                factory.createDataDef("def2", fromDate, toDate, "json2");
        List<DataDef> oldDataDefs = Lists.newArrayList(dataDef1, dataDef2);

        DataDef dataDef3 =
                factory.createDataDef("def1", runDate, toDate, "json changed");
        List<DataDef> newDataDefs = Lists.newArrayList(dataDef3);

        given(configService.getRunDateTime()).willReturn(runDate);

        boolean actual =
                dataDefHelper.markForUpdation(newDataDefs, oldDataDefs);

        assertThat(actual).isTrue();

        assertThat(oldDataDefs).containsExactly(dataDef1, dataDef2, dataDef3);

        assertThat(dataDef1.getFromDate()).isEqualTo(fromDate);
        assertThat(dataDef2.getFromDate()).isEqualTo(fromDate);
        assertThat(dataDef3.getFromDate()).isEqualTo(runDate);

        assertThat(dataDef1.getToDate())
                .isEqualTo(DateUtils.addSeconds(runDate, -1));
        assertThat(dataDef2.getToDate()).isEqualTo(toDate);
        assertThat(dataDef2.getToDate()).isEqualTo(toDate);
    }

    @Test
    public void testSetDefs() throws IOException {
        String defJson1 = "{ body: test1 }";
        String defJson2 = "{ body: test2 }";

        DataDef dataDef1 =
                factory.createDataDef("def1", new Date(), new Date(), defJson1);
        DataDef dataDef2 =
                factory.createDataDef("def2", new Date(), new Date(), defJson2);

        String json = TestUtils.parseJson(
                "{ def1 : { body : test1 }, def2 : { body : test2} }");
        JsonNode defs = mapper.readTree(json);

        JsonNode def1 = defs.at("/def1");
        JsonNode def2 = defs.at("/def2");

        given(yamlHelper.toJsonNode(defJson1)).willReturn(def1);
        given(yamlHelper.toJsonNode(defJson2)).willReturn(def2);

        ArrayList<DataDef> dataDefs = Lists.newArrayList(dataDef1, dataDef2);
        dataDefHelper.setDefs(dataDefs);

        assertThat(dataDefs.get(0).getDef()).isEqualTo(def1);
        assertThat(dataDefs.get(1).getDef()).isEqualTo(def2);
    }

    @Test
    public void testGetAxisSets() throws IOException {
        DataDef dataDef = getTestDataDef();
        Map<String, Axis> axes = getTestAxes();

        Axis fact = axes.get("fact");
        given(objectFactory.createAxis(AxisName.FACT, "fact")).willReturn(fact);

        Axis col = axes.get("col");
        given(objectFactory.createAxis(AxisName.COL, "date")).willReturn(col);

        Axis row1 = axes.get("row1");
        given(objectFactory.createAxis(AxisName.ROW, "Price")).willReturn(row1);

        Axis row2 = axes.get("row2");
        given(objectFactory.createAxis(AxisName.ROW, "High")).willReturn(row2);

        Axis expectedFact =
                factory.createAxis(AxisName.FACT, "fact", null, null, 0, 10);

        Axis expectedCol =
                factory.createAxis(AxisName.COL, "date", null, null, 1, 11);

        Axis expectedRow1 =
                factory.createAxis(AxisName.ROW, "Price", "Price", null, 2, 12);

        Axis expectedRow2 = factory.createAxis(AxisName.ROW, "High");
        expectedRow2.setMatch("High"); // all other are null

        List<Set<Axis>> actual = dataDefHelper.getAxisSets(dataDef);

        assertThat(actual.size()).isEqualTo(3);

        assertThat(actual.get(0)).containsOnly(expectedFact);
        assertThat(actual.get(1)).containsOnly(expectedCol);
        assertThat(actual.get(2)).containsOnly(expectedRow1, expectedRow2);
    }

    @Test
    public void testGetData() throws IOException {
        DataDef dataDef = getTestDataDef();

        Data data = factory.createData(dataDef.getName());
        Item item1 = factory.createItem();
        Item item2 = factory.createItem();

        Map<String, Axis> axes = getTestAxes();
        Axis fact = axes.get("fact");
        Axis col = axes.get("col");
        Axis row1 = axes.get("row1");
        Axis row2 = axes.get("row2");

        HashSet<Axis> expectedAxes1 = Sets.newHashSet(fact, col, row1);
        HashSet<Axis> expectedAxes2 = Sets.newHashSet(fact, col, row2);

        // axis set as given by datadef
        List<Set<Axis>> axisSets = new ArrayList<>();
        Set<Axis> axisSet = Sets.newHashSet(fact);
        axisSets.add(axisSet);
        axisSet = Sets.newHashSet(col);
        axisSets.add(axisSet);
        axisSet = Sets.newHashSet(row1, row2);
        axisSets.add(axisSet);

        given(objectFactory.createData(dataDef.getName())).willReturn(data);
        given(objectFactory.createItem()).willReturn(item1, item2);

        Data actual = dataDefHelper.getData(dataDef, axisSets);

        assertThat(actual.getDataDef()).isEqualTo(dataDef.getName());
        assertThat(actual.getItems().size()).isEqualTo(2);

        Set<Axis> actualAxes1 = actual.getItems().get(0).getAxes();
        Set<Axis> actualAxes2 = actual.getItems().get(1).getAxes();
        // set order may change so use isIn
        assertThat(actualAxes1).isIn(expectedAxes1, expectedAxes2);
        assertThat(actualAxes2).isIn(expectedAxes1, expectedAxes2);
        assertThat(actualAxes1).isNotEqualTo(actualAxes2);
    }

    @Test
    public void testToMap() {
        DataDef dataDef1 = factory.createDataDef("def1", new Date(), new Date(),
                "defJson1");
        DataDef dataDef2 = factory.createDataDef("def2", new Date(), new Date(),
                "defJson2");
        ArrayList<DataDef> dataDefs = Lists.newArrayList(dataDef1, dataDef2);

        Map<String, DataDef> actual = dataDefHelper.toMap(dataDefs);

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get("def1")).isSameAs(dataDef1);
        assertThat(actual.get("def2")).isSameAs(dataDef2);
    }

    private DataDef getTestDataDef() throws IOException {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        String defJson = new StringBuilder().append("{axis:")
                .append("{fact:{query:{region:queryregion,field:queryfield},")
                .append("items:[{item:{name:fact,index:0,order:10}}]},")
                .append("col:{query:{script:colscript},")
                .append("items:[{item:{name:date,index:1,order:11}}]},")
                .append("row:{query:{noQuery: ignored},")
                .append("items:[{item:{name:Price,value:Price,index:2,order:12}},")
                .append("{item:{name:High,match:High}}").append("]} }}")
                .toString();
        JsonNode def = mapper.readTree(TestUtils.parseJson(defJson));
        DataDef dataDef =
                factory.createDataDef("price", fromDate, toDate, defJson);
        dataDef.setDef(def);
        return dataDef;
    }

    private Map<String, Axis> getTestAxes() {

        Axis fact =
                factory.createAxis(AxisName.FACT, "fact", null, null, 0, 10);

        Axis col = factory.createAxis(AxisName.COL, "date", null, null, 1, 11);

        Axis row1 =
                factory.createAxis(AxisName.ROW, "Price", "Price", null, 2, 12);

        Axis row2 = factory.createAxis(AxisName.ROW, "High");
        row2.setMatch("High");

        Map<String, Axis> axisMap = new HashMap<>();
        axisMap.put("fact", fact);
        axisMap.put("col", col);
        axisMap.put("row1", row1);
        axisMap.put("row2", row2);
        return axisMap;
    }
}
