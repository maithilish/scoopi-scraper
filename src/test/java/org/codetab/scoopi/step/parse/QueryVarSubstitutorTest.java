package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

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
        Map<String, String> queries = getTestQueries();
        Map<String, Axis> axisMap = getAxisMap();

        substitutor.replaceVariables(queries, axisMap);

        assertThat(queries.get("q1")).isEqualTo("q1 col-value col-match 1");
        assertThat(queries.get("q2")).isEqualTo("q2 11 row-match row-value");
        assertThat(queries.get("q3")).isEqualTo("q3 11 1");
        assertThat(queries.get("q4")).isEqualTo("q4 col-match row-value");
        assertThat(queries.get("q5")).isEqualTo("q5 no substituion");
    }

    private Map<String, String> getTestQueries() {
        Map<String, String> queries = new HashMap<>();
        queries.put("q1", "q1 %{col.value} %{col.match} %{col.index}");
        queries.put("q2", "q2 %{row.index} %{row.match} %{row.value}");
        queries.put("q3", "q3 %{row.index} %{col.index}");
        queries.put("q4", "q4 %{col.match} %{row.value}");
        queries.put("q5", "q5 no substituion");
        return queries;
    }

    private Map<String, Axis> getAxisMap() {
        Axis col = factory.createAxis(AxisName.COL, "col", "col-value",
                "col-match", 1, 2);
        Axis row = factory.createAxis(AxisName.ROW, "row", "row-value",
                "row-match", 11, 12);

        Map<String, Axis> axisMap = new HashMap<>();
        axisMap.put("COL", col);
        axisMap.put("ROW", row);

        return axisMap;
    }
}
