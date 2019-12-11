package org.codetab.scoopi.stat;

import java.util.TimerTask;

import javax.inject.Inject;

/**
 * <p>
 * Task to collect memory stats.
 * @author Maithilish
 *
 */
public class MemoryTask extends TimerTask {

    @Inject
    private Stats stats;

    @Override
    public void run() {
        stats.collectMemStats();
    }
}
