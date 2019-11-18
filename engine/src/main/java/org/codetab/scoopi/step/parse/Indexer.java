package org.codetab.scoopi.step.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Range;

public class Indexer implements Iterator<Map<String, Integer>> {

    private List<String> names = new ArrayList<>();
    private Map<String, Range<Integer>> ranges = new HashMap<>();
    private int[] indexes;
    private boolean finished;

    public void addIndex(final String name, final Range<Integer> range) {
        names.add(name);
        ranges.put(name, range);
    }

    public void markBreakAfter(final String name) {
        int i = names.indexOf(name);
        int index = indexes[i];

        Range<Integer> range = ranges.get(name);
        if (range.getMaximum() == Integer.MAX_VALUE) {
            Integer newMin = range.getMinimum();
            Integer newMax = index;
            Range<Integer> newRange = Range.between(newMin, newMax);
            ranges.put(name, newRange);
        }
    }

    public void init() {
        finished = false;
        indexes = new int[names.size()];
        for (int i = 0; i < indexes.length; i++) {
            String name = names.get(i);
            Range<Integer> range = ranges.get(name);
            indexes[i] = range.getMinimum();
        }
    }

    @Override
    public boolean hasNext() {
        return !finished;
    }

    @Override
    public Map<String, Integer> next() {
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Integer index = indexes[i];
            indexMap.put(name, index);
        }
        for (int i = names.size() - 1; i >= 0; i--) {
            Range<Integer> range = ranges.get(names.get(i));
            int nextIndex = indexes[i] + 1;
            if (!range.contains(nextIndex)) {
                indexes[i] = range.getMinimum();
                if (i == 0) {
                    finished = true;
                }
            } else {
                indexes[i] = nextIndex;
                break;
            }
        }
        return indexMap;
    }
}
