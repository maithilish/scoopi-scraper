package org.codetab.scoopi.misc;

import java.util.TimerTask;

import javax.inject.Inject;

import org.codetab.scoopi.shared.StatService;

/**
 * <p>
 * Task to collect memory stats.
 * @author Maithilish
 *
 */
public class MemoryTask extends TimerTask {

    /**
     * activity service.
     */
    @Inject
    private StatService activityService;

    @Override
    public void run() {
        activityService.collectMemoryStat();
    }
}
