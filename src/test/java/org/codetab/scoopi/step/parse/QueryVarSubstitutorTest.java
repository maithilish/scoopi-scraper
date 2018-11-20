package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class QueryVarSubstitutorTest {

    @InjectMocks
    private QueryVarSubstitutor substitutor;

    private ObjectFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        factory = new ObjectFactory();
    }

    @Test
    public void testReplaceVariables() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        List<Axis> axisList = getAxisList();

        Axis ownAxis = axisList.get(0); // item1
        Map<String, String> queries = getTestQueries();
        Map<String, String> varValueMap =
                substitutor.getVarValueMap(queries, axisList, ownAxis);

        substitutor.replaceVariables(queries, varValueMap);

        assertThat(queries.get("q1")).isEqualTo("q1 i1-value i1-match 1");
        assertThat(queries.get("q2")).isEqualTo("q2 1 i1-match i1-value");
        assertThat(queries.get("q3")).isEqualTo("q3 1 i1-match i1-value");
        assertThat(queries.get("q4")).isEqualTo("q4 11 i2-match i2-value");
        assertThat(queries.get("q5")).isEqualTo("q5 21 d1-match d1-value");
        assertThat(queries.get("q6")).isEqualTo("q6 no substituion");

        ownAxis = axisList.get(1); // item2
        queries = getTestQueries();
        varValueMap = substitutor.getVarValueMap(queries, axisList, ownAxis);

        substitutor.replaceVariables(queries, varValueMap);

        assertThat(queries.get("q1")).isEqualTo("q1 i2-value i2-match 11");
        assertThat(queries.get("q2")).isEqualTo("q2 11 i2-match i2-value");
        assertThat(queries.get("q3")).isEqualTo("q3 1 i1-match i1-value");
        assertThat(queries.get("q4")).isEqualTo("q4 11 i2-match i2-value");
        assertThat(queries.get("q5")).isEqualTo("q5 21 d1-match d1-value");
        assertThat(queries.get("q6")).isEqualTo("q6 no substituion");
    }

    @Test
    public void testGetVarValMap() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        List<Axis> axisList = getAxisList();
        Map<String, String> queries = getTestQueries();

        Axis ownAxis = axisList.get(1); // item 2

        Map<String, String> actual =
                substitutor.getVarValueMap(queries, axisList, ownAxis);

        assertThat(actual.size()).isEqualTo(15);
        assertThat(actual.get("value")).isEqualTo("i2-value");
        assertThat(actual.get("match")).isEqualTo("i2-match");
        assertThat(actual.get("index")).isEqualTo("11");

        assertThat(actual.get("item.value")).isEqualTo("i2-value");
        assertThat(actual.get("item.match")).isEqualTo("i2-match");
        assertThat(actual.get("item.index")).isEqualTo("11");

        assertThat(actual.get("item.i1.value")).isEqualTo("i1-value");
        assertThat(actual.get("item.i1.match")).isEqualTo("i1-match");
        assertThat(actual.get("item.i1.index")).isEqualTo("1");

        assertThat(actual.get("item.i2.value")).isEqualTo("i2-value");
        assertThat(actual.get("item.i2.match")).isEqualTo("i2-match");
        assertThat(actual.get("item.i2.index")).isEqualTo("11");

        assertThat(actual.get("dim.d1.value")).isEqualTo("d1-value");
        assertThat(actual.get("dim.d1.match")).isEqualTo("d1-match");
        assertThat(actual.get("dim.d1.index")).isEqualTo("21");
    }

    private Map<String, String> getTestQueries() {
        Map<String, String> queries = new HashMap<>();
        queries.put("q1", "q1 %{value} %{match} %{index}");
        queries.put("q2", "q2 %{item.index} %{item.match} %{item.value}");
        queries.put("q3",
                "q3 %{item.i1.index} %{item.i1.match} %{item.i1.value}");
        queries.put("q4",
                "q4 %{item.i2.index} %{item.i2.match} %{item.i2.value}");
        queries.put("q5", "q5 %{dim.d1.index} %{dim.d1.match} %{dim.d1.value}");
        queries.put("q6", "q6 no substituion");
        return queries;
    }

    private List<Axis> getAxisList() {
        Axis item1 =
                factory.createAxis("item", "i1", "i1-value", "i1-match", 1, 2);
        Axis item2 = factory.createAxis("item", "i2", "i2-value", "i2-match",
                11, 12);
        Axis dim =
                factory.createAxis("dim", "d1", "d1-value", "d1-match", 21, 22);

        List<Axis> list = Lists.newArrayList(item1, item2, dim);
        return list;
    }
}
