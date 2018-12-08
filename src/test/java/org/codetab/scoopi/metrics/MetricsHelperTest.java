package org.codetab.scoopi.metrics;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;

public class MetricsHelperTest {

    private MetricsHelper helper;
    private MetricRegistry metrics;

    @Before
    public void setUp() throws Exception {
        helper = new MetricsHelper();
        metrics = SharedMetricRegistries.getOrCreate("scoopi");
        metrics.getNames().stream().forEach(metrics::remove);
    }

    @Test
    public void testGetTimer() {
        String obj = "xyz";
        String metricName = "test1";
        Timer expected =
                metrics.timer(name(obj.getClass().getSimpleName(), metricName));

        Timer actual = helper.getTimer(obj, metricName);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetMeter() {
        String obj = "xyz";
        String metricName = "test2";
        Meter expected =
                metrics.meter(name(obj.getClass().getSimpleName(), metricName));

        Meter actual = helper.getMeter(obj, metricName);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetCounter() {
        String obj = "xyz";
        String metricName = "test3";
        Counter expected = metrics
                .counter(name(obj.getClass().getSimpleName(), metricName));

        Counter actual = helper.getCounter(obj, metricName);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testRegisterGuage() {
        String obj = "xyz";
        String metricName = "test4";

        String name = name(obj.getClass().getSimpleName(), metricName);
        helper.registerGuage(Date.class, obj, metricName);

        assertThat(metrics.getGauges().containsKey(name)).isTrue();

        @SuppressWarnings("unchecked")
        Gauge<Date> guage = metrics.getGauges().get(name);
        Object result = guage.getValue();
        assertThat(result).isInstanceOf(Class.class);

        helper.clearGuages();
        assertThat(metrics.getGauges().containsKey(name)).isFalse();

    }

    @Test
    public void testInitMetrics() {
        assertThat(metrics.getNames().size()).isEqualTo(0);

        helper.initMetrics();
        SortedMap<String, Counter> counters = metrics.getCounters();
        assertThat(counters.size()).isEqualTo(4);

        assertThat(counters.containsKey("ParserCache.parser.cache.hit"))
                .isTrue();
        assertThat(counters.containsKey("ParserCache.parser.cache.miss"))
                .isTrue();
        assertThat(counters.containsKey("PageLoader.fetch.web")).isTrue();
        assertThat(counters.containsKey("Task.system.error")).isTrue();
    }
}
