package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.Axis;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class QueryVarSubstitutorTest {
    @InjectMocks
    private QueryVarSubstitutor queryVarSubstitutor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReplaceVariables() throws Exception {
        Map<String, String> queries = new HashMap<>();
        Map<String, String> varValueMap = new HashMap<>();
        String key = "Foo";
        String query = "Bar %{match}";
        queries.put(key, query);

        varValueMap.put("match", "Qux");

        queryVarSubstitutor.replaceVariables(queries, varValueMap);

        assertEquals(queries.get(key), "Bar Qux");

    }

    @Test
    public void testGetVarValueMapIf() throws Exception {
        Map<String, String> queries = new HashMap<>();
        List<Axis> axisList = new ArrayList<>();
        Axis ownAxis = Mockito.mock(Axis.class);

        String query = "Foo %{match}";
        String placeHolder = "match";
        String match = "Qux";

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put(placeHolder, match);

        when(ownAxis.getMatch()).thenReturn(match);

        queries.put(placeHolder, query);

        Map<String, String> actual =
                queryVarSubstitutor.getVarValueMap(queries, axisList, ownAxis);

        assertEquals(valueMap, actual);
    }

    @Test
    public void testGetVarValueMapNoPlaceholder() throws Exception {
        Map<String, String> queries = new HashMap<>();
        List<Axis> axisList = new ArrayList<>();
        Axis ownAxis = Mockito.mock(Axis.class);

        String query = "Foo";
        String placeHolder = "match";
        String match = "Qux";

        when(ownAxis.getMatch()).thenReturn(match);

        queries.put(placeHolder, query);

        Map<String, String> actual =
                queryVarSubstitutor.getVarValueMap(queries, axisList, ownAxis);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetVarValueMapTwoPartSwitch() throws Exception {
        Map<String, String> queries = new HashMap<>();
        List<Axis> axisList = new ArrayList<>();
        Axis itemAxis = Mockito.mock(Axis.class);
        axisList.add(itemAxis);

        String property = "match";
        String match = "Qux";
        String axisName = "item";

        String placeholder = String.join(".", axisName, property);
        String query = "Foo %{" + placeholder + "}";

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put(placeholder, match);

        when(itemAxis.getAxisName()).thenReturn(axisName);
        when(itemAxis.getMatch()).thenReturn(match);

        queries.put(match, query);

        Map<String, String> actual =
                queryVarSubstitutor.getVarValueMap(queries, axisList, itemAxis);

        assertEquals(valueMap, actual);
    }

    @Test
    public void testGetVarValueMapThreePartSwitch() throws Exception {
        Map<String, String> queries = new HashMap<>();
        List<Axis> axisList = new ArrayList<>();
        Axis itemAxis = Mockito.mock(Axis.class);
        axisList.add(itemAxis);

        String property = "match";
        String match = "Qux";
        String axisName = "item";
        String itemName = "Price";

        String placeholder = String.join(".", axisName, itemName, property);
        String query = "Foo %{" + placeholder + "}";

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put(placeholder, match);

        when(itemAxis.getAxisName()).thenReturn(axisName);
        when(itemAxis.getMatch()).thenReturn(match);
        when(itemAxis.getItemName()).thenReturn(itemName);

        queries.put(match, query);

        Map<String, String> actual =
                queryVarSubstitutor.getVarValueMap(queries, axisList, itemAxis);

        assertEquals(valueMap, actual);
    }

    @Test
    public void testGetVarValueMapThreePartSwitchAxisNameNotEqual()
            throws Exception {
        Map<String, String> queries = new HashMap<>();
        List<Axis> axisList = new ArrayList<>();
        Axis itemAxis = Mockito.mock(Axis.class);
        axisList.add(itemAxis);

        String property = "match";
        String match = "Qux";
        String axisName = "item";
        String itemName = "Price";

        String placeholder = String.join(".", axisName, itemName, property);
        String query = "Foo %{" + placeholder + "}";

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put(placeholder, match);

        when(itemAxis.getAxisName()).thenReturn("not equal");
        when(itemAxis.getMatch()).thenReturn(match);
        when(itemAxis.getItemName()).thenReturn(itemName);

        queries.put(match, query);

        assertThrows(IllegalArgumentException.class, () -> queryVarSubstitutor
                .getVarValueMap(queries, axisList, itemAxis));

    }

    @Test
    public void testGetVarValueMapThreePartSwitchItemNameNotEqual()
            throws Exception {
        Map<String, String> queries = new HashMap<>();
        List<Axis> axisList = new ArrayList<>();
        Axis itemAxis = Mockito.mock(Axis.class);
        axisList.add(itemAxis);

        String property = "match";
        String match = "Qux";
        String axisName = "item";
        String itemName = "Price";

        String placeholder = String.join(".", axisName, itemName, property);
        String query = "Foo %{" + placeholder + "}";

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put(placeholder, match);

        when(itemAxis.getAxisName()).thenReturn(axisName);
        when(itemAxis.getMatch()).thenReturn(match);
        when(itemAxis.getItemName()).thenReturn("not equal");

        queries.put(match, query);

        assertThrows(IllegalArgumentException.class, () -> queryVarSubstitutor
                .getVarValueMap(queries, axisList, itemAxis));

    }

    @Test
    public void testGetVarValueMapFourPartSwitch() throws Exception {
        Map<String, String> queries = new HashMap<>();
        List<Axis> axisList = new ArrayList<>();
        Axis itemAxis = Mockito.mock(Axis.class);
        axisList.add(itemAxis);

        String property = "match";
        String match = "Qux";
        String axisName = "item";
        String itemName = "Price";
        String fourthPart = "dummy";

        String placeholder =
                String.join(".", axisName, itemName, property, fourthPart);
        String query = "Foo %{" + placeholder + "}";

        Map<String, String> valueMap = new HashMap<>();
        valueMap.put(placeholder, match);

        when(itemAxis.getAxisName()).thenReturn(axisName);
        when(itemAxis.getMatch()).thenReturn(match);
        when(itemAxis.getItemName()).thenReturn("not equal");

        queries.put(match, query);

        assertThrows(IllegalArgumentException.class, () -> queryVarSubstitutor
                .getVarValueMap(queries, axisList, itemAxis));

    }
}
