package org.codetab.scoopi.defs.yml;

import org.apache.commons.lang3.Range;

class IndexRangeFactory {

    public Range<Integer> createRange(final String rangeSpec) {
        Range<Integer> range;
        if (rangeSpec.contains("-")) {
            String[] r = rangeSpec.split("-");
            if (r.length > 1) {
                Integer min = Integer.valueOf(r[0]);
                Integer max = Integer.valueOf(r[1]);
                range = Range.between(min, max);
            } else if (r.length == 1) {
                Integer min = Integer.valueOf(r[0]);
                Integer max = Integer.MAX_VALUE;
                range = Range.between(min, max);
            } else {
                range = Range.is(0);
            }
        } else {
            Integer v = Integer.valueOf(rangeSpec);
            range = Range.is(v);
        }
        return range;
    }
}
