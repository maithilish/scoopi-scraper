package org.codetab.scoopi.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codetab.scoopi.metrics.MetricsHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;

public class ParserCacheTest {

    @Mock
    private MetricsHelper metricsHelper;

    @InjectMocks
    private ParserCache cache;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGet() {
        Counter miss = new Counter();
        Counter hit = new Counter();

        given(metricsHelper.getCounter(cache, "parser", "cache", "miss"))
                .willReturn(miss);
        given(metricsHelper.getCounter(cache, "parser", "cache", "hit"))
                .willReturn(hit);

        String value = "test";
        int key = 1;
        cache.put(key, value);

        assertThat(cache.get(key)).isEqualTo(value);
        assertThat(hit.getCount()).isEqualTo(1);
        assertThat(miss.getCount()).isEqualTo(0);

        assertThat(cache.get(10)).isNull();
        assertThat(hit.getCount()).isEqualTo(1);
        assertThat(miss.getCount()).isEqualTo(1);
    }

    @Test
    public void testPut() {
        Counter miss = new Counter();
        Counter hit = new Counter();

        given(metricsHelper.getCounter(cache, "parser", "cache", "miss"))
                .willReturn(miss);
        given(metricsHelper.getCounter(cache, "parser", "cache", "hit"))
                .willReturn(hit);

        String value = null;
        int key = 1;
        cache.put(key, value);

        assertThat(cache.get(key)).isNull();

        value = "test";
        cache.put(key, value);
        assertThat(cache.get(key)).isEqualTo(value);
    }

    @Test
    public void testGetKey() {
        Map<String, String> map = new HashMap<>();
        map.put("region", "rq1");
        map.put("field", "fq1");

        int expected = Arrays.hashCode(map.values().toArray());

        int actual = cache.getKey(map);

        assertThat(actual).isEqualTo(expected);
    }

}
