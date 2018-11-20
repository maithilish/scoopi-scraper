package org.codetab.scoopi.defs.mig.yml.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.defs.mig.yml.helper.JsonNodeHelper;
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
import org.mockito.Spy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ItemDefsHelperTest {

    @Spy
    private JsonNodeHelper jsonNodeHelper;
    @Spy
    private ObjectFactory objectFactory;

    @InjectMocks
    private ItemDefs itemDefs;

    private static ObjectFactory factory;
    private static ObjectMapper mapper;
    private DataDef dataDef;

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
        dataDef = getTestDataDef();
    }

    @Test
    public void testGetRegionQuery() throws IOException {
        String actual = itemDefs.getRegionQuery(dataDef, "items1");
        assertThat(actual).isEqualTo("region query");
    }

    @Test
    public void testGetRegionQueryNotDefined() throws IOException {
        testRule.expect(NoSuchElementException.class);
        itemDefs.getRegionQuery(dataDef, "items2");
    }

    @Test
    public void testGetItemQuery() throws IOException {
        String actual = itemDefs.getItemQuery(dataDef, "items1", "item1");
        assertThat(actual).isEqualTo("item1 query");

        actual = itemDefs.getItemQuery(dataDef, "items1", "item2");
        assertThat(actual).isEqualTo("item2 query");
    }

    @Test
    public void testGetItemQueryNotDefined() throws IOException {
        testRule.expect(NoSuchElementException.class);
        itemDefs.getItemQuery(dataDef, "items1", "item3");
    }

    @Test
    public void testGetItemNames() {
        List<String> actual = itemDefs.getItemNames(dataDef, "items1");
        assertThat(actual).containsExactly("item1", "item2");
    }

    @Test
    public void testGetItemNamesNotDefined() {
        testRule.expect(NoSuchElementException.class);
        itemDefs.getItemNames(dataDef, "item2");
    }

    // @Test
    // public void testGetBreakAfters() {
    // Optional<List<String>> actual =
    // itemDefsHelper.getBreakAfters(dataDef, "item1");
    // assertThat(actual).isPresent();
    // assertThat(actual.get()).containsExactly("break1", "break2");
    // }
    //
    // @Test
    // public void testGetBreakAftersNotDefined() {
    // Optional<List<String>> actual =
    // itemDefsHelper.getBreakAfters(dataDef, "item2");
    // assertThat(actual).isNotPresent();
    //
    // actual = itemDefsHelper.getBreakAfters(dataDef, "item3");
    // assertThat(actual).isNotPresent();
    // }
    //
    // @Test
    // public void testGetIndexRange() {
    // Range<Integer> expected = Range.between(1, 20);
    //
    // Optional<Range<Integer>> actual =
    // itemDefsHelper.getIndexRange(dataDef, "item1");
    // assertThat(actual).isPresent();
    // assertThat(actual.get()).isEqualTo(expected);
    // }
    //
    // @Test
    // public void testGetIndexRangeNotDefined() {
    // Optional<Range<Integer>> actual =
    // itemDefsHelper.getIndexRange(dataDef, "item2");
    // assertThat(actual).isNotPresent();
    //
    // actual = itemDefsHelper.getIndexRange(dataDef, "item3");
    // assertThat(actual).isNotPresent();
    // }

    private DataDef getTestDataDef() throws IOException {
        Date runDate = new Date();
        Date fromDate = DateUtils.addDays(runDate, -1);
        Date toDate = DateUtils.addDays(runDate, 1);
        String defJson = new StringBuilder().append("{ items:")
                .append("{ items1:")
                .append("{ indexRange: 1-20, breakAfter: ['break1','break2'], ")
                .append("region: 'region query', ").append("items: [ ")
                .append("{ item: {name: 'item1', selector: 'item1 query'} },")
                .append("{ item: {name: 'item2', selector: 'item2 query'} } ] },")
                .append("items2: { } }}").toString();
        JsonNode def = mapper.readTree(TestUtils.parseJson(defJson));
        DataDef testDataDef =
                factory.createDataDef("price", fromDate, toDate, defJson);
        testDataDef.setDef(def);
        return testDataDef;
    }
}
