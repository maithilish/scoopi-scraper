package org.codetab.scoopi.engine.module;

import javax.inject.Inject;

import org.codetab.scoopi.engine.ShutdownHook;

public class ShutdownModule {

    @Inject
    private Runtime runTime;
    @Inject
    private ShutdownHook shutdownHook;

    private boolean shutdownStarted = false;

    public boolean addShutdownHook() {
        runTime.addShutdownHook(shutdownHook);
        return true;
    }

    public void setCleanShutdown(final boolean cleanShutdown) {
        shutdownHook.setCleanShutdown(cleanShutdown);
    }

    public synchronized boolean hasShutdownStarted() {
        if (shutdownStarted) {
            return true;
        } else {
            shutdownStarted = true;
            return false;
        }
    }
}
