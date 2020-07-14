package org.codetab.scoopi.engine.module;

import javax.inject.Inject;

import org.codetab.scoopi.stat.ShutdownHook;

public class ShutdownModule {

    @Inject
    private Runtime runTime;
    @Inject
    private ShutdownHook shutdownHook;

    public boolean addShutdownHook() {
        runTime.addShutdownHook(shutdownHook);
        return true;
    }

    public void setCleanShutdown() {
        shutdownHook.setCleanShutdown(true);
    }

}
