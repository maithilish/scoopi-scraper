package org.codetab.scoopi.status;

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
    private ScoopiStatus scoopiStatus;

    @Override
    public void run() {
        scoopiStatus.collectMemStats();
    }
}
