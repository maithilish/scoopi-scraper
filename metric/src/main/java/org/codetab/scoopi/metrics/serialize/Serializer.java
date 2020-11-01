package org.codetab.scoopi.metrics.serialize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * On scheduled intervals, converts MetricsRegistry measurements to json and
 * pass it on to a Consumer.
 */
public class Serializer extends ScheduledReporter {

    // CHECKSTYLE:OFF: checkstyle:ParameterNumber
    // CHECKSTYLE:OFF: checkstyle:HiddenField

    /**
     * Returns a new {@link Builder} for {@link Serializer}.
     *
     * @param registry
     *            the registry to report
     * @return a {@link Builder} instance for a {@link Serializer}
     */
    public static Builder forRegistry(final MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link Serializer} instances. Defaults to using the default
     * locale and time zone, writing to {@code System.out}, converting rates to
     * events/second, converting durations to milliseconds, and not filtering
     * metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Consumer<byte[]> consumer;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(final MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            disabledMetricAttributes = Collections.emptySet();
        }

        /**
         * Specifies whether or not, the executor (used for reporting) will be
         * stopped with same time with reporter. Default value is true. Setting
         * this parameter to false, has the sense in combining with providing
         * external managed executor via
         * {@link #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop
         *            if true, then executor will be stopped in same time with
         *            this reporter
         * @return {@code this}
         */
        public Builder shutdownExecutorOnStop(
                final boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics.
         * Default value is null. Null value leads to executor will be auto
         * created on start.
         *
         * @param executor
         *            the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        public Builder scheduleOn(final ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Consumer to handle the json output
         *
         * @param consumer
         *            a {@link Consumer<T>} instance.
         * @return {@code this}
         */
        public Builder consumer(final Consumer<byte[]> consumer) {
            this.consumer = consumer;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(final TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(final TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter
         *            a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(final MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Don't report the passed metric attributes for all metrics (e.g.
         * "p999", "stddev" or "m15"). See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes
         *            a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(
                final Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        /**
         * Builds a {@link Serializer} with the given properties.
         *
         * @return a {@link Serializer}
         */
        public Serializer build() {
            return new Serializer(registry, consumer, rateUnit, durationUnit,
                    filter, executor, shutdownExecutorOnStop,
                    disabledMetricAttributes);
        }
    }

    private final Consumer<byte[]> consumer;
    private ObjectMapper mapper;

    private Serializer(final MetricRegistry registry,
            final Consumer<byte[]> consumer, final TimeUnit rateUnit,
            final TimeUnit durationUnit, final MetricFilter filter,
            final ScheduledExecutorService executor,
            final boolean shutdownExecutorOnStop,
            final Set<MetricAttribute> disabledMetricAttributes) {
        super(registry, "json-reporter", filter, rateUnit, durationUnit,
                executor, shutdownExecutorOnStop, disabledMetricAttributes);
        this.consumer = consumer;

        MetricsModule metricsModule =
                new MetricsModule(rateUnit, durationUnit, false, filter);
        mapper = new ObjectMapper().registerModule(metricsModule);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void report(
            @SuppressWarnings("rawtypes") final SortedMap<String, Gauge> gauges,
            final SortedMap<String, Counter> counters,
            final SortedMap<String, Histogram> histograms,
            final SortedMap<String, Meter> meters,
            final SortedMap<String, Timer> timers) {

        Map<String, Map<String, ?>> metrics = new HashMap<>();
        metrics.put("gauges", gauges);
        metrics.put("counters", counters);
        metrics.put("histograms", histograms);
        metrics.put("meters", meters);
        metrics.put("timers", timers);

        try {
            byte[] json = mapper.writeValueAsBytes(metrics);
            consumer.accept(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CHECKSTYLE:ON: checkstyle:ParameterNumber
    // CHECKSTYLE:ON: checkstyle:HiddenField
}
