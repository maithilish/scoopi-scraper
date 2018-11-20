package org.codetab.scoopi.system;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import net.jcip.annotations.GuardedBy;

@Singleton
public class Sequence {

    @GuardedBy("this")
    private Map<String, AtomicInteger> seqs = new HashMap<>();

    public int getSequence(final String key) {
        return getCounter(key).incrementAndGet();
    }

    private synchronized AtomicInteger getCounter(final String key) {
        AtomicInteger counter = seqs.get(key);
        if (counter == null) {
            counter = new AtomicInteger();
            seqs.put(key, counter);
        }
        return counter;
    }
}
