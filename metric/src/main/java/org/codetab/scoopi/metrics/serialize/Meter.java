package org.codetab.scoopi.metrics.serialize;

import org.apache.commons.lang3.Validate;

public class Meter implements Metric {

    // CHECKSTYLE:OFF:
    public long count;
    public double m15_rate;
    public double m1_rate;
    public double m5_rate;
    public double mean_rate;
    public String units;
    // CHECKSTYLE:ON:

    @Override
    public void aggregate(final Metric other) {
        Validate.isInstanceOf(Meter.class, other);

        Meter oth = (Meter) other;
        count += oth.count;
        m15_rate = (m15_rate + oth.m15_rate) / 2;
        m1_rate = (m1_rate + oth.m1_rate) / 2;
        m5_rate = (m5_rate + oth.m5_rate) / 2;
        mean_rate = (mean_rate + oth.mean_rate) / 2;
    }
}
