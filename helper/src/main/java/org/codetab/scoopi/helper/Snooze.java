package org.codetab.scoopi.helper;

import java.time.Duration;

import com.google.common.util.concurrent.Uninterruptibles;

public class Snooze {

    public void sleepUninterruptibly(final long millis) {
        Uninterruptibles.sleepUninterruptibly(Duration.ofMillis(millis));
    }
}
