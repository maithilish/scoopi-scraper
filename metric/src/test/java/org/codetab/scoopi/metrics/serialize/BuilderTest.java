package org.codetab.scoopi.metrics.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class BuilderTest {
    // @InjectMocks
    // private Builder builder;
    //
    // @Mock
    // private MetricRegistry registry;
    // @Mock
    // private Consumer<byte[]> consumer;
    // @Mock
    // private TimeUnit rateUnit;
    // @Mock
    // private TimeUnit durationUnit;
    // @Mock
    // private MetricFilter filter;
    // @Mock
    // private ScheduledExecutorService executor;
    // @Mock
    // private Set<MetricAttribute> disabledMetricAttributes;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuild() throws IllegalAccessException {
        MetricRegistry registry = Mockito.mock(MetricRegistry.class);
        @SuppressWarnings("unchecked")
        Consumer<byte[]> consumer = Mockito.mock(Consumer.class);
        Set<MetricAttribute> dma = new HashSet<>();
        MetricFilter filter = MetricFilter.contains("x");
        ScheduledExecutorService ses =
                Mockito.mock(ScheduledExecutorService.class);

        Serializer actual = Serializer.forRegistry(registry).consumer(consumer)
                .convertDurationsTo(TimeUnit.HOURS)
                .convertRatesTo(TimeUnit.DAYS).disabledMetricAttributes(dma)
                .filter(filter).scheduleOn(ses).shutdownExecutorOnStop(false)
                .build();

        assertSame(registry, readField(actual, "registry", true));
        assertSame(consumer, readField(actual, "consumer", true));
        assertEquals("day", readField(actual, "rateUnit", true));
        assertEquals("hours", readField(actual, "durationUnit", true));
        assertSame(filter, readField(actual, "filter", true));
        assertSame(ses, readField(actual, "executor", true));
        assertFalse(
                (Boolean) readField(actual, "shutdownExecutorOnStop", true));
        assertSame(dma, readField(actual, "disabledMetricAttributes", true));
    }

    private Object readField(final Object target, final String fieldName,
            final boolean forceAccess) throws IllegalAccessException {
        return FieldUtils.readField(target, fieldName, forceAccess);
    }
}
