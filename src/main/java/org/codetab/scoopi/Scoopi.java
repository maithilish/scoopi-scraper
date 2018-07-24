package org.codetab.scoopi;

import javax.inject.Inject;

import org.codetab.scoopi.di.DInjector;

public final class Scoopi {

    @Inject
    private ScoopiEngine engine;

    public static void main(final String[] args) {
        // create DInjector singleton
        DInjector dInjector = new DInjector().instance(DInjector.class);
        Scoopi scoopi = dInjector.instance(Scoopi.class);
        scoopi.start();
    }

    public void start() {
        engine.start();
    }
}
