package org.codetab.scoopi.metrics.serialize;

import org.apache.commons.lang3.Validate;

public class Counter implements Metric {

    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(final long count) {
        this.count = count;
    }

    @Override
    public void aggregate(final Metric other) {
        Validate.isInstanceOf(Counter.class, other);
        count += ((Counter) other).getCount();
    }

}
