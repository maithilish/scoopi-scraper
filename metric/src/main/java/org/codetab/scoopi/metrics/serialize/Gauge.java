package org.codetab.scoopi.metrics.serialize;

import static org.codetab.scoopi.util.Util.spaceit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;

public class Gauge implements Metric {

    private Map<String, Object> value;

    public Map<String, Object> getValue() {
        return value;
    }

    public void setValue(final Map<String, Object> value) {
        this.value = value;
    }

    @Override
    public void aggregate(final Metric other) {
        Validate.isInstanceOf(Gauge.class, other);

        for (Entry<String, Object> othEntry : ((Gauge) other).getValue()
                .entrySet()) {
            String key = othEntry.getKey();
            if (value.containsKey(key)) {
                value.put(key,
                        aggregate(key, value.get(key), othEntry.getValue()));
            } else {
                value.put(key, othEntry.getValue());
            }
        }

    }

    private Object aggregate(final String key, final Object val1,
            final Object val2) {
        Object result;
        switch (key) {
        case "activeCount":
            result = (int) val1 + (int) val2;
            break;
        case "poolSize":
            result = (int) val1 + (int) val2;
            break;
        case "completedTaskCount":
            result = (int) val1 + (int) val2;
            break;
        case "taskCount":
            result = (int) val1 + (int) val2;
            break;
        case "uptime":
            long t1;
            try {
                t1 = convertTime(val1);
                long t2 = convertTime(val2);
                result = val1;
                if (t2 > t1) {
                    result = val2;
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("aggregate uptime", e);
            }
            break;
        case "systemLoad":
            result = ((double) val1 + (double) val2) / 2;
            break;
        case "freeMemory":
            result = (int) val1 + (int) val2;
            break;
        case "totalMemory":
            result = (int) val1 + (int) val2;
            break;
        case "maxMemory":
            result = (int) val1 + (int) val2;
            break;
        default:
            throw new IllegalStateException(
                    spaceit("aggregate op not defined for key:", key));
        }
        return result;
    }

    private long convertTime(final Object timeStr) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date reference = dateFormat.parse("00:00:00");
        Date date = dateFormat.parse((String) timeStr);
        return date.getTime() - reference.getTime();
    }

}
